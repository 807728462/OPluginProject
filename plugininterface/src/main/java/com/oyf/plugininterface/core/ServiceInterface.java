package com.oyf.plugininterface.core;

import android.app.Service;
import android.content.Intent;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 14:15
 * @描述 定义service的标准规范
 **/
public interface ServiceInterface {

    void initService(Service service);
    
    void onCreate();

    int onStartCommand(Intent intent, int flags, int startId);

    void onDestroy();

}
