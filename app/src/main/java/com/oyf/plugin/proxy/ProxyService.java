package com.oyf.plugin.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.oyf.plugin.manager.PluginManager;
import com.oyf.plugin.utils.ArouterUtils;
import com.oyf.plugininterface.core.ServiceInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 14:15
 * @描述 代理service，可以同时控制多个service
 **/
public class ProxyService extends Service {

    public final static String TAG = "ProxyService";
    public final static String KEY_STOP_SERVICE = "stop_service";
    private ProxyBinder mProxyBinder = new ProxyBinder();

    /**
     * 缓存多个service
     */
    private Map<String, ServiceInterface> mServiceInterfaceMap;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mProxyBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceInterfaceMap = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String className = intent.getStringExtra(ArouterUtils.KEY_CLASS_NAME);
        //先从缓存中取是否有，因为一个service可以start多次，只有在第一次的时候调用onCreate
        ServiceInterface serviceInterface = mServiceInterfaceMap.get(className);
        if (null == serviceInterface) {
            serviceInterface = createServiceInterface(className);
            if (null != serviceInterface) {
                serviceInterface.onCreate();
                serviceInterface.onStartCommand(intent, flags, startId);
                mServiceInterfaceMap.put(className, serviceInterface);
            } else {
                Log.d(TAG, "创建失败serviceInterface");
            }
        } else {
            //是否是停止service，当同时启动了多个service的时候，stopService只是停止其中的某一个
            boolean isStop = intent.getBooleanExtra(KEY_STOP_SERVICE, false);
            if (isStop) {
                serviceInterface.onDestroy();
                mServiceInterfaceMap.remove(className);
            } else {
                serviceInterface.onStartCommand(intent, flags, startId);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (null != mServiceInterfaceMap) {
            for (String className : mServiceInterfaceMap.keySet()) {
                mServiceInterfaceMap.get(className).onDestroy();
            }
            mServiceInterfaceMap.clear();
        }
        super.onDestroy();
    }

    /**
     * 创建一个ServiceInterface    代理service
     *
     * @param className
     * @return
     */
    private ServiceInterface createServiceInterface(String className) {
        ServiceInterface serviceInterface = null;
        try {
            Class<?> pluginClass = PluginManager.getInstance().getDexClassLoader().loadClass(className);
            serviceInterface = (ServiceInterface) pluginClass.newInstance();
        } catch (Exception e) {

        }
        return serviceInterface;
    }

    public class ProxyBinder extends Binder {

    }
}
