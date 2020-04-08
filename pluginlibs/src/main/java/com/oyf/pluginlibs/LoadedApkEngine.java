package com.oyf.pluginlibs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 11:55
 * @描述
 **/
public class LoadedApkEngine {
    private final static String TAG = LoadedApkEngine.class.getSimpleName();
    private static LoadedApkEngine instance;

    private LoadedApkEngine() {
    }

    public static LoadedApkEngine getInstance() {
        if (null == instance) {
            instance = new LoadedApkEngine();
        }
        return instance;
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
        mPackages.put(applicationInfo.packageName, new WeakReference<>(loadedApk));

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

}
