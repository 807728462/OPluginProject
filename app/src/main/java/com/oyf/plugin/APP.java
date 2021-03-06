package com.oyf.plugin;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.oyf.plugin.manager.HookManager;
import com.oyf.plugin.manager.LoadedApkManager;
import com.oyf.pluginlibs.PluginManager;
import com.oyf.pluginlibs.PluginTypeEnum;

/**
 * @创建者 oyf
 * @创建时间 2020/3/31 11:52
 * @描述
 **/
public class APP extends Application {
    public static final String mPluginApkPath = "pluginapk-debug.apk";
    public static final String mLoginApkPath = "login-debug.apk";

    private Resources mResources;
    private AssetManager mAssetManager;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            //PluginManager.getInstance().loadApk(this, mPluginApkPath);
            //PluginManager.getInstance().loadApk(this, mLoginApkPath);

           /* HookManager.getInstance().hookAMS(this);
            HookManager.getInstance().hookActivityThread(this);
            HookManager.getInstance().loadPluginDex(this, mPluginApkPath);
            mAssetManager = HookManager.getInstance().loadPluginAssetManager(mPluginApkPath);
            mResources = HookManager.getInstance().loadPluginResource(this, mAssetManager);*/

           /* mResources = HookManager.getInstance().loadPluginResource(this, mAssetManager);
            LoadedApkManager.getInstance().hookAMS(this);
            LoadedApkManager.getInstance().hookHandler();
            LoadedApkManager.getInstance().hookLoadedApk(this, mPluginApkPath);*/

            PluginManager.getInstance().init(this, PluginTypeEnum.LOADEDAPK_PLUGIN);
            mAssetManager = PluginManager.getInstance().getAssetManager();
            mResources = PluginManager.getInstance().getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        return mResources == null ? super.getResources() : mResources;
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager == null ? super.getAssets() : mAssetManager;
    }
}
