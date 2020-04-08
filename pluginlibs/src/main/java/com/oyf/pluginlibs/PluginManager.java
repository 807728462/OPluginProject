package com.oyf.pluginlibs;


import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import static com.oyf.pluginlibs.PluginTypeEnum.HOOK_PLUGIN;
import static com.oyf.pluginlibs.PluginTypeEnum.LOADEDAPK_PLUGIN;
import static com.oyf.pluginlibs.PluginTypeEnum.PROXY_PLUGIN;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 10:50
 * @描述
 **/
public class PluginManager {
    private static PluginManager instance;

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        if (null == instance) {
            instance = new PluginManager();
        }
        return instance;
    }

    private Context mContext;
    private PluginTypeEnum mTypeEnum;
    private String mApkName;

    public void init(Context context, PluginTypeEnum typeEnum) {
        init(context, typeEnum, null, null);
    }

    public void init(Context context, PluginTypeEnum typeEnum, String packageName, String className) {
        mContext = context;
        mTypeEnum = typeEnum;
        mApkName = Parameter.PLUGIN_FILE_NAME;
        switch (typeEnum) {
            case PROXY_PLUGIN:
                initProxyType();
                break;
            case LOADEDAPK_PLUGIN:
                initLoadedAPKType();
                break;
            case HOOK_PLUGIN:
                initHookType();
                break;
        }
    }

    private void initProxyType() {
        PluginProxyManager.getInstance().loadApk(mContext, Parameter.PLUGIN_FILE_NAME);
    }

    private void initHookType() {
        try {
            AMSCheckEngine.hookAMS(mContext);
            ActivityThreadmHRestore.mActivityThreadmHAction(mContext);
            DexElementFuse.getInstance().mainPluginFuse(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLoadedAPKType() {
        try {
            AMSCheckEngine.hookAMS(mContext);
            PackageCheck.getInstance().hookHandler();
            LoadedApkEngine.getInstance().hookLoadedApk(mContext, Parameter.PLUGIN_FILE_NAME);
            DexElementFuse.getInstance().loadResource(mContext, Parameter.PLUGIN_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 代理式获取classloader
     *
     * @param mApkName
     * @return
     */
    public ClassLoader getDexClassLoader(String mApkName) {
        if (mTypeEnum == PROXY_PLUGIN) {
            return PluginProxyManager.getInstance().getDexClassLoader(mApkName);
        } else {
            return null;
        }
    }


    /**
     * hook式获取到resource
     *
     * @return
     */
    public Resources getResources() {
        if (mTypeEnum == HOOK_PLUGIN || mTypeEnum == LOADEDAPK_PLUGIN) {
            return DexElementFuse.getInstance().getResources();
        } else if (mTypeEnum == PROXY_PLUGIN) {
            return PluginProxyManager.getInstance().getResources(mApkName);
        } else {
            return null;
        }
    }

    /**
     * 只需要 让插件去那 宿主的getResources 就好了，不需要让插件去那AssetManager了
     * 因为宿主和插件进行了融合，插件只要拿到宿主中的Resources，就等于拿到了 AssetManager了，因为AssetManager属于单利的哦
     *
     * @return
     */
    public AssetManager getAssetManager() {
        if (mTypeEnum == HOOK_PLUGIN) {
            return DexElementFuse.getInstance().getAssetManager();
        } else {
            return null;
        }
    }
}
