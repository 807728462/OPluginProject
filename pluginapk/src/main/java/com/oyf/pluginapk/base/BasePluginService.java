package com.oyf.pluginapk.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.oyf.plugininterface.core.ServiceInterface;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 14:19
 * @描述
 **/
public abstract class BasePluginService extends Service implements ServiceInterface {

    protected Service mService;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void initService(Service service) {
        mService = service;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }

    @Override
    public void onDestroy() {

    }
}
