package com.oyf.pluginapk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oyf.pluginapk.base.BasePluginBroadcastReceiver;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 11:08
 * @描述
 **/
public class PluginTwoBroadcastReceiver extends BasePluginBroadcastReceiver {

    private static final String TAG = PluginTwoBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String two_data = intent.getStringExtra("two_data");
        Log.d(TAG, "PluginTwoBroadcastReceiver.onReceive=" + two_data);
    }
}
