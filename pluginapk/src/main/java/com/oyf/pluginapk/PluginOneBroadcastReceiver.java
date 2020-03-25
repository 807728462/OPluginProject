package com.oyf.pluginapk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oyf.pluginapk.base.BasePluginBroadcastReceiver;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 9:43
 * @描述
 **/
public class PluginOneBroadcastReceiver extends BasePluginBroadcastReceiver {

    private static final String TAG = PluginOneBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String one_data = intent.getStringExtra("one_data");
        Log.d(TAG, "PluginOneBroadcastReceiver.onReceive=" + one_data);
    }
}
