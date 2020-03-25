package com.oyf.plugininterface.core;

import android.content.Context;
import android.content.Intent;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 9:36
 * @描述
 **/
public interface BroadcastInterface {
    
    void onReceive(Context context, Intent intent);
}
