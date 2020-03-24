package com.oyf.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import com.oyf.plugin.manager.PluginManager;
import com.oyf.plugin.utils.ArouterUtils;
import com.oyf.plugininterface.core.ActivityInterface;

import java.lang.reflect.Constructor;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 14:06
 * @描述 代理activity
 **/
public class ProxyActivity extends Activity {

    private String className = "";
    private ActivityInterface mActivityInterface;

    @Override
    public ClassLoader getClassLoader() {
        return PluginManager.getInstance().getDexClassLoader();
    }

    @Override
    public Resources getResources() {
        return PluginManager.getInstance().getResources();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        className = getIntent().getStringExtra(ArouterUtils.KEY_CLASS_NAME);
        if (TextUtils.isEmpty(className)) {
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
            Class<?> pluginActivityClass = getClassLoader().loadClass(className);
            //强转换为activity的接口
            Constructor<?> constructor = pluginActivityClass.getConstructor(new Class[]{});
            mActivityInterface = (ActivityInterface) constructor.newInstance(null);
            //手动的控制activity的生命周期
            mActivityInterface.initContext(this);
            mActivityInterface.onCreate(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        ComponentName component = intent.getComponent();
        intent.setComponent(this.getComponentName());
        intent.putExtra(ArouterUtils.KEY_CLASS_NAME, component.getClassName());
        super.startActivity(intent);
    }
}
