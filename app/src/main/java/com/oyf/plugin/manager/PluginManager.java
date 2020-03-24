package com.oyf.plugin.manager;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.oyf.plugininterface.OPathUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 11:28
 * @描述
 **/
public class PluginManager {
    private final static String TAG = PluginManager.class.getSimpleName();

    private static PluginManager instance;

    private PluginManager() {
        mApkPaths = new ArrayList<>();
    }

    public static PluginManager getInstance() {
        if (null == instance) {
            instance = new PluginManager();
        }
        return instance;
    }

    private List<String> mApkPaths;
    private DexClassLoader mDexClassLoader;
    private Resources mResources;

    public void loadApk(Context context, String apk) {
        File apkFile = new File(OPathUtils.getRootDir() + File.separator + apk);
        if (mApkPaths.contains(apk)) {
            Log.d(TAG, apk + "已加载");
            return;
        }
        if (null == apkFile || !apkFile.exists()) {
            Log.d(TAG, "加载的" + apk + "文件不存在");
            return;
        }
        creatClassLoader(context, apkFile.getAbsolutePath());
        creatResoure(context, apkFile.getAbsolutePath());
        Log.d(TAG, "加载的" + apk + "成功");
    }


    /**
     * 创建classloader
     *
     * @param context
     * @param apk
     */
    private void creatClassLoader(Context context, String apk) {
        mDexClassLoader = new DexClassLoader(apk, OPathUtils.getOptimizedDirectory(context), null, context.getClassLoader());
    }

    private void creatResoure(Context context, String apk) {
        //AssetManager assetManager = context.getAssets();
        try {
            //使用反射获取
            AssetManager assetManager = AssetManager.class.newInstance();
            //将resoure文件加入assetManager
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apk);
            mResources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        } catch (Exception e) {
            Log.d(TAG, "加载的" + apk + "的Resources失败文件不存在");
        }
    }

    public DexClassLoader getDexClassLoader() {
        return mDexClassLoader;
    }

    public Resources getResources() {
        return mResources;
    }
}
