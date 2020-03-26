package com.oyf.pluginapk;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.oyf.plugininterface.base.BasePluginService;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 14:19
 * @描述
 **/
public class PluginTwoService extends BasePluginService {

    private static final String TAG = PluginTwoService.class.getSimpleName();
    public static final String KEY_TWO_DATA = "KEY_TWO_DATA";
    public static final String KEY_TWO_DATA_PAUSE = "KEY_TWO_DATA_PAUSE";
    private boolean mPlaying = false;
    private String str = "";

    public void onCreate() {
        mPlaying = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mPlaying) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                    }
                    Log.d(TAG, "Two-PluginTwoService.start=====" + str);
                }
            }
        }).start();
        Log.d(TAG, "PluginTwoService.start");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String two_data = intent.getStringExtra(KEY_TWO_DATA);
        Log.d(TAG, "PluginTwoService.start=" + two_data);
        boolean two_data_pause = intent.getBooleanExtra(KEY_TWO_DATA_PAUSE, false);
        str = two_data_pause + "";
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        mPlaying = false;
        super.onDestroy();
    }
}
