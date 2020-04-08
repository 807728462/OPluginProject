package com.oyf.plugin.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.oyf.plugin.proxy.ProxyActivity;
import com.oyf.pluginlibs.OPathUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import dalvik.system.DexClassLoader;

/**
 * @创建者 oyf
 * @创建时间 2020/4/1 9:34
 * @描述 loadedApk式
 **/
public class LoadedApkManager {
    private final static String TAG = LoadedApkManager.class.getSimpleName();
    private static final int LAUNCH_ACTIVITY = 100;

    private static LoadedApkManager instance;
    private static final String KEY_CLASSNAME = "class_name";

    private LoadedApkManager() {
    }

    public static LoadedApkManager getInstance() {
        if (null == instance) {
            instance = new LoadedApkManager();
        }
        return instance;
    }

    public void hookAMS(final Context context) {
        try {
            //拿到我们需要hook的接口
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            //拿到需要hook的对象，因为是静态的，可以直接获取
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerNativeClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            //在代理方法中需要真正执行的 activitymanager
            final Object iActivityManager = getDefaultMethod.invoke(null);

            Object iActivityManagerProxy = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{iActivityManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("startActivity".equals(method.getName())) {
                        Intent oldIntent = (Intent) args[2];
                        Intent newIntent = new Intent(context, ProxyActivity.class);
                        newIntent.putExtra(KEY_CLASSNAME, oldIntent.getComponent());
                        args[2] = newIntent;
                    }
                    Log.d(TAG, "拦截到了IActivityManager里面的方法" + method.getName());

                    // 让系统继续正常往下执行
                    return method.invoke(iActivityManager, args);
                }
            });
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            //获取IActivityManagerSingleton
            Field gDefaultField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            gDefaultField.setAccessible(true);
            Object gDefault = gDefaultField.get(null);

            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            mInstanceField.set(gDefault, iActivityManagerProxy);
        } catch (Exception e) {
            Log.d(TAG, "hook  ams  失败");
        }
    }

    public void hookLoadedApk(Context context, String apkName) throws Exception {
        File apkFile = new File(OPathUtils.getRootDir() + File.separator + apkName);
        if (!apkFile.exists()) {
            Log.d(TAG, apkName + "文件不存在");
            return;
        }
        //hook住系统开activity回调之后
        //hookHandler();


        //1.先获取ActivityThread的mPackages
        //ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<>();
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object activityThread = currentActivityThreadMethod.invoke(null);
        Field mPackagesField = activityThreadClass.getDeclaredField("mPackages");
        mPackagesField.setAccessible(true);
        //获取到的mPackages
        ArrayMap mPackages = (ArrayMap) mPackagesField.get(activityThread);

        //2.创建自己的LoadedApk
        // ActivityThread的getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo)创建一个loadedApk
        Class compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
        Field default_compatibility_infoField = compatibilityInfoClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        Object compatibilityInfo = default_compatibility_infoField.get(null);
        ApplicationInfo applicationInfo = getPluginApplicationInfo(apkFile);
        Method getPackageInfoNoCheckMethod = activityThreadClass.getDeclaredMethod("getPackageInfoNoCheck", ApplicationInfo.class, compatibilityInfoClass);
        Object loadedApk = getPackageInfoNoCheckMethod.invoke(activityThread, applicationInfo, compatibilityInfo);

        //创建插件的classLoader
        DexClassLoader pluginClassLoader = new DexClassLoader(
                apkFile.getAbsolutePath(),
                OPathUtils.getOptimizedDirectory(context),
                null,
                context.getClassLoader());

        Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
        mClassLoaderField.setAccessible(true);
        mClassLoaderField.set(loadedApk, pluginClassLoader);
        // 最终的目标 mPackages.put(插件的包名，插件的LoadedApk);
        //在8.0会自动加入mpackages中去
        //mPackages.put(applicationInfo.packageName, new WeakReference<>(loadedApk));

    }

    /**
     * 根据插件apk文件创建ApplicationInfo
     *
     * @param apkFile
     * @return
     * @throws Exception
     */
    private ApplicationInfo getPluginApplicationInfo(File apkFile) throws Exception {
        // 执行此public static ApplicationInfo generateApplicationInfo方法，拿到ApplicationInfo
        Class mPackageParserClass = Class.forName("android.content.pm.PackageParser");
        Object packageParser = mPackageParserClass.newInstance();

        Class packageClass = Class.forName("android.content.pm.PackageParser$Package");
        //Package parsePackage(File packageFile, int flags)
        Method parsePackageMethod = mPackageParserClass.getMethod("parsePackage", File.class, int.class);

        Object pluginPackage = parsePackageMethod.invoke(packageParser, apkFile, PackageManager.GET_ACTIVITIES);

        Class packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        // ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state)
        Method generateApplicationInfoMethod = mPackageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageClass,
                int.class,
                packageUserStateClass);
        ApplicationInfo applicationInfo = (ApplicationInfo) generateApplicationInfoMethod.invoke(
                packageParser,
                pluginPackage,
                0,
                packageUserStateClass.newInstance());
        // 获得的 ApplicationInfo 就是插件的 ApplicationInfo
        // 我们这里获取的 ApplicationInfo
        // applicationInfo.publicSourceDir = 插件的路径；
        // applicationInfo.sourceDir = 插件的路径；
        applicationInfo.publicSourceDir = apkFile.getAbsolutePath();
        applicationInfo.sourceDir = apkFile.getAbsolutePath();
        return applicationInfo;
    }

    /**
     * hook住startactivity返回后的handler，在handler中处理packagename，
     * 还有创建activity之后，绕过pms的检测，会根据packagename检测是否已经安装app
     */
    public void hookHandler() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            //获取handler中的handler属性
            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            //用于获取activithThread
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            final Object activityThread = currentActivityThreadMethod.invoke(null);
            Object mH = mHField.get(activityThread);

            //获取handler中的mCallBack属性，用于我们自己创建一个，然后去自行处理返回过来的Intent
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            //自己设置一个callBack进入，返回值为true时需要自行调用handlerMessage,false则不需要处理
            mCallbackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    //从msg中获取到系统返回过来的intent
                    if (msg.what == LAUNCH_ACTIVITY) {
                        try {
                            Object obj = msg.obj;
                            // 我们要获取之前Hook携带过来的 TestActivity
                            Field intentField = obj.getClass().getDeclaredField("intent");
                            intentField.setAccessible(true);
                            // 获取 intent 对象，才能取出携带过来的 actionIntent
                            Intent intent = (Intent) intentField.get(obj);
                            ComponentName componentName = intent.getParcelableExtra(KEY_CLASSNAME);
                            if (null != componentName) {
                                intent.setComponent(componentName);

                                Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
                                activityInfoField.setAccessible(true);
                                ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);

                                //如果是插件的activity的话  activityInfo.getpackage为空
                                if (intent.getPackage() == null) {
                                    activityInfo.applicationInfo.packageName = componentName.getPackageName();
                                    hookPackageCheck(activityThread);
                                } else {
                                    activityInfo.applicationInfo.packageName = intent.getPackage();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 使用动态代理监听系统的IPackageManager   会去检测包是否安装
     *
     * @param activityThread
     */
    private void hookPackageCheck(Object activityThread) {
        try {
            // sPackageManager 替换  我们自己的动态代理
            Field getPackageManagerField = activityThread.getClass().getDeclaredField("sPackageManager");
            getPackageManagerField.setAccessible(true);
            final Object iPackageManager = getPackageManagerField.get(activityThread);

            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");
            Object proxyPackageManager = Proxy.newProxyInstance(activityThread.getClass().getClassLoader(), new Class[]{iPackageManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    if ("getPackageInfo".equals(method.getName())) {
                        // 如何才能绕过 PMS, 欺骗系统
                        // pi != null

                        Log.d(TAG, "getPackageInfo绕过检测");
                        return new PackageInfo(); // 成功绕过 PMS检测
                    }
                    Log.d(TAG, "getPackageInfo正常执行");
                    // 让系统正常继续执行下去
                    return method.invoke(iPackageManager, objects);
                }
            });
            getPackageManagerField.set(activityThread, proxyPackageManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
