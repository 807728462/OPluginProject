package com.oyf.plugin.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import android.util.Log;

import com.oyf.plugininterface.OPathUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @创建者 oyf
 * @创建时间 2020/4/1 9:34
 * @描述 loadedApk式
 **/
public class LoadedApkManager {
    private final static String TAG = LoadedApkManager.class.getSimpleName();

    private static LoadedApkManager instance;

    private LoadedApkManager() {
    }

    public static LoadedApkManager getInstance() {
        if (null == instance) {
            instance = new LoadedApkManager();
        }
        return instance;
    }

    public void hookLoadedApk(Context context, String apkName) throws Exception {
        File apkFile = new File(OPathUtils.getRootDir() + File.separator + apkName);
        if (!apkFile.exists()) {
            Log.d(TAG, apkName + "文件不存在");
            return;
        }
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

        // 最终的目标 mPackages.put(插件的包名，插件的LoadedApk);
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
        Method parsePackageMethod = packageClass.getMethod("parsePackage", File.class, int.class);

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
