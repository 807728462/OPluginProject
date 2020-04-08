package com.oyf.plugin.proxy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.oyf.plugininterface.core.ActivityInterface;
import com.oyf.plugininterface.utils.ArouterUtils;
import com.oyf.pluginlibs.PluginManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 14:06
 * @描述 代理activity
 **/
public class ProxyActivity extends AppCompatActivity {

    private String mApkName = "";
    private String mClassName = "";
    private ActivityInterface mActivityInterface;

    /**
     * 用于保存一个页面多个广播
     */
    private Map<String, ProxyBroadcaseReceiver> mProxyBroadcaseReceiverMap;
    /**
     * 用于保存一个页面多个服务
     */
    private Set<String> mProxyServiceSet = new HashSet<>();

    @Override
    public ClassLoader getClassLoader() {
        return PluginManager.getInstance().getDexClassLoader(mApkName);
    }
/*
    @Override
    public Resources getResources() {
        return PluginManager.getInstance().getResources(getIntent().getStringExtra(ArouterUtils.KEY_APK_NAME));
    }*/

    /**
     * 根据不同包名去替换Resources
     */
    private void replaceResouces() {
        try {
            Class<?> contextThemeWrapperClass = Class.forName("android.view.ContextThemeWrapper");
            Field mResourcesField = contextThemeWrapperClass.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(this, PluginManager.getInstance().getResources());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApkName = getIntent().getStringExtra(ArouterUtils.KEY_APK_NAME);
        replaceResouces();
        mClassName = getIntent().getStringExtra(ArouterUtils.KEY_CLASS_NAME);
        if (TextUtils.isEmpty(mClassName) || TextUtils.isEmpty(mApkName)) {
            finish();
            return;
        }
        //初始化真正的activity
        initClass(savedInstanceState);
    }

    @Override
    protected void onPause() {
        if (null != mActivityInterface) {
            mActivityInterface.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (null != mActivityInterface) {
            mActivityInterface.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
        if (null != mActivityInterface) {
            mActivityInterface.onRestart();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (null != mActivityInterface) {
            mActivityInterface.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (null != mActivityInterface) {
            mActivityInterface.onDestroy();
        }
        super.onDestroy();
    }

    private void initClass(Bundle savedInstanceState) {
        try {
            //根据全路径加载activity的class
            Class<?> pluginActivityClass = getClassLoader().loadClass(mClassName);
            //强转换为activity的接口
            mActivityInterface = (ActivityInterface) pluginActivityClass.newInstance();
            //手动的控制activity的生命周期
            mActivityInterface.initContext(this);
            mActivityInterface.onCreate(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        //插件中的activity无法启动，只有重新跳转此代理activity，然后重新反射创建新的代理activity
        ComponentName component = intent.getComponent();
        intent.setComponent(this.getComponentName());
        intent.putExtra(ArouterUtils.KEY_CLASS_NAME, component.getClassName());
        super.startActivity(intent);
    }

    /**
     * @param receiver 是代理插件传过来的，是没有注册
     * @param filter
     * @return
     */
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (null == mProxyBroadcaseReceiverMap) {
            mProxyBroadcaseReceiverMap = new HashMap<>();
        }
        //根据类名存储所有的广播，并且同一个广播只能注册一次
        String receiverName = receiver.getClass().getName();
        ProxyBroadcaseReceiver proxyBroadcaseReceiver = mProxyBroadcaseReceiverMap.get(receiverName);
        if (null == proxyBroadcaseReceiver) {
            ProxyBroadcaseReceiver mProxyBroadcaseReceiver = new ProxyBroadcaseReceiver(mApkName, receiverName);
            mProxyBroadcaseReceiverMap.put(receiverName, mProxyBroadcaseReceiver);
            return super.registerReceiver(mProxyBroadcaseReceiver, filter);
        } else {
            return null;
        }
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (null == mProxyBroadcaseReceiverMap) {
            return;
        }
        //根据类名反注册的广播，并且移除广播缓存
        String receiverName = receiver.getClass().getName();
        ProxyBroadcaseReceiver proxyBroadcaseReceiver = mProxyBroadcaseReceiverMap.get(receiverName);
        if (null != proxyBroadcaseReceiver) {
            super.unregisterReceiver(proxyBroadcaseReceiver);
            mProxyBroadcaseReceiverMap.remove(receiverName);
        }
    }

    @Override
    public ComponentName startService(Intent service) {
        String className = service.getComponent().getClassName();
        mProxyServiceSet.add(className);
        service.putExtra(ArouterUtils.KEY_CLASS_NAME, className);
        service.setClass(this, ProxyService.class);
        return super.startService(service);
    }

    @Override
    public boolean stopService(Intent service) {
        String className = service.getComponent().getClassName();
        service.setClass(this, ProxyService.class);
        //如果开启的插件服务超过了一个，并且存在需要停止的服务，那么手动的调用destory
        if (mProxyServiceSet.size() > 1 && mProxyServiceSet.contains(className)) {
            service.putExtra(ArouterUtils.KEY_CLASS_NAME, className);
            service.putExtra(ProxyService.KEY_STOP_SERVICE, true);
            super.startService(service);
            mProxyServiceSet.remove(className);
            return true;
        } else if (mProxyServiceSet.size() == 1 && !mProxyServiceSet.contains(className)) {//当只剩下一个代理服务后，并且代理的不等于缓存的则忽略
            return false;
        } else {
            return super.stopService(service);
        }
    }
}
