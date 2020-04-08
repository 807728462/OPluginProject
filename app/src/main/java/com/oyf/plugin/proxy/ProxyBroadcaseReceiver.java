package com.oyf.plugin.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oyf.pluginlibs.PluginManager;
import com.oyf.pluginlibs.PluginProxyManager;
import com.oyf.plugininterface.core.BroadcastInterface;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 9:37
 * @描述 定义BroadcastReceiver的标准规范
 **/
public class ProxyBroadcaseReceiver extends BroadcastReceiver {

    private String mApkName;
    private String mClassName;
    private String mAction;

    public ProxyBroadcaseReceiver(String apkName, String className) {
        mApkName = apkName;
        mClassName = className;
    }

    public ProxyBroadcaseReceiver(String apkName, String className, String action) {
        mApkName = apkName;
        mClassName = className;
        mAction = action;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BroadcastInterface broadcastInterface = creatPluginBroadcastReceiver(mClassName);
        if (null != broadcastInterface) {
            broadcastInterface.onReceive(context, intent);
        }
    }

    private BroadcastInterface creatPluginBroadcastReceiver(String className) {
        try {
            Class<?> pluginClass = PluginManager.getInstance().getDexClassLoader(mApkName).loadClass(className);
            return (BroadcastInterface) pluginClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
