package com.oyf.plugininterface.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oyf.plugininterface.core.BroadcastInterface;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 9:41
 * @描述
 **/
public class BasePluginBroadcastReceiver extends BroadcastReceiver implements BroadcastInterface {
    private static final String TAG = BasePluginBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BaseBroadcastReceiver。onReceive");
    }
}
