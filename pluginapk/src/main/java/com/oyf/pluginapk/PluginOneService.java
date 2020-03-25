package com.oyf.pluginapk;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.oyf.pluginapk.base.BasePluginService;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 14:19
 * @描述
 **/
public class PluginOneService extends BasePluginService {

    private static final String TAG = PluginOneService.class.getSimpleName();
    public static final String KEY_ONE_DATA = "KEY_ONE_DATA";
    public static final String KEY_ONE_DATA_PAUSE = "KEY_ONE_DATA_PAUSE";
    private boolean mPlaying = false;
    private String str = "";

    public void onCreate() {
        mPlaying = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mPlaying) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    Log.d(TAG, "PluginOneService.start+" + str);
                }
            }
        }).start();
        Log.d(TAG, "PluginOneService.start");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String one_data = intent.getStringExtra(KEY_ONE_DATA);
        Log.d(TAG, "PluginOneService.start=" + one_data);
        boolean one_data_pause = intent.getBooleanExtra(KEY_ONE_DATA_PAUSE, false);
        str = one_data_pause + "";
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        mPlaying = false;
        super.onDestroy();
    }
}
