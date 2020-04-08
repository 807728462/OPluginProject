package com.oyf.plugin.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.oyf.plugin.proxy.ProxyActivity;
import com.oyf.pluginlibs.OPathUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @创建者 oyf
 * @创建时间 2020/3/30 10:11
 * @描述 使用替换Intent式
 **/
public class HookManager {
    private final static String TAG = HookManager.class.getSimpleName();

    private static final int LAUNCH_ACTIVITY = 100;
    private static final String KEY_CLASSNAME = "class_name";
    private static HookManager instance;

    private HookManager() {
    }

    public static HookManager getInstance() {
        if (null == instance) {
            instance = new HookManager();
        }
        return instance;
    }
    /****************************************Hook式我们的activity************************************************************************/

    /**
     * startActivity的时候，进行替换我们注册过的activity
     *
     * @param context
     */
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

    /**
     * hook经过底层处理过后的intent
     *
     * @param context
     */
    public void hookActivityThread(Context context) {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            //获取handler中的handler属性
            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            //用于获取activithThread
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);
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
     * 将自己插件的dex添加到basedexclassloader中的dexpathlist中去
     *
     * @param context
     */
    public void loadPluginDex(Context context, String apkName) {
        try {
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            //1.获取系统的pathlist
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            Field pathListField = pathClassLoader.getClass().getSuperclass().getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(context.getClassLoader());
            Object dexElements = dexElementsField.get(pathList);
            //2.加载插件中的dex，获取插件中的pathList
            String apkPath = OPathUtils.getRootDir() + File.separator + apkName;
            DexClassLoader pluginClassLoader = new DexClassLoader(apkPath,
                    OPathUtils.getOptimizedDirectory(context), null, context.getClassLoader());
            Object pathListPlugin = pathListField.get(pluginClassLoader);
            Object dexElementsPlugin = dexElementsField.get(pathListPlugin);
            int length = Array.getLength(dexElements);
            int pluginLength = Array.getLength(dexElementsPlugin);
            int sumDexLeng = length + pluginLength;
            //合并系统的跟插件的pathList
            Object allDexElements = Array.newInstance(dexElementsPlugin.getClass().getComponentType(), sumDexLeng);

            for (int i = 0; i < sumDexLeng; i++) {
                // 先融合宿主
                if (i < length) {
                    // 参数一：新要融合的容器 -- newDexElements
                    Array.set(allDexElements, i, Array.get(dexElements, i));
                } else { // 再融合插件的
                    Array.set(allDexElements, i, Array.get(dexElementsPlugin, i - length));
                }
            }

            dexElementsField.set(pathList, allDexElements);
            Log.d(TAG, "hook  loadPluginDex  成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将插件中的资源文件添加到系统中
     */
    public AssetManager loadPluginAssetManager(String apkName) {
        try {
            String apkPath = OPathUtils.getRootDir() + File.separator + apkName;
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, apkPath);

            // 实例化此方法 final StringBlock[] ensureStringBlocks()
         /*   Method ensureStringBlocksMethod = assetManager.getClass().getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocksMethod.setAccessible(true);
            ensureStringBlocksMethod.invoke(assetManager);// 执行了ensureStringBlocks  string.xml  color.xml   anim.xml 被初始化 */
            return assetManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * hook式，加载插件的资源文件
     *
     * @param context
     * @param assetManager
     * @return
     */
    public Resources loadPluginResource(Context context, AssetManager assetManager) {
        try {
            return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
