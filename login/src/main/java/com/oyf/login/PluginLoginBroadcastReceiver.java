package com.oyf.login;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oyf.plugininterface.base.BasePluginBroadcastReceiver;

/**
 * @创建者 oyf
 * @创建时间 2020/3/26 14:21
 * @描述
 **/
public class PluginLoginBroadcastReceiver extends BasePluginBroadcastReceiver {
    private static final String TAG = PluginLoginBroadcastReceiver.class.getSimpleName();
    public static final String KEY_LOGIN_DATA = "LOGIN_DATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String one_data = intent.getStringExtra(KEY_LOGIN_DATA);
        Log.d(TAG, "login.onReceiveBroadcastReceiver=" + one_data);
    }
}
