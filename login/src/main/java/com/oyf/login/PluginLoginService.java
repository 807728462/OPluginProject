package com.oyf.login;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.oyf.plugininterface.base.BasePluginService;

/**
 * @创建者 oyf
 * @创建时间 2020/3/25 14:19
 * @描述
 **/
public class PluginLoginService extends BasePluginService {

    private static final String TAG = PluginLoginService.class.getSimpleName();
    public static final String KEY_LOGIN_DATA = "KEY_LOGIN_DATA";
    public static final String KEY_LOGIN_DATA_PAUSE = "KEY_LOGIN_DATA_PAUSE";
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
                    Log.d(TAG, "LoginService.start+" + str);
                }
            }
        }).start();
        Log.d(TAG, "LoginService.start");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String one_data = intent.getStringExtra(KEY_LOGIN_DATA);
        Log.d(TAG, "LoginService.onStartCommand=" + one_data);
        boolean one_data_pause = intent.getBooleanExtra(KEY_LOGIN_DATA_PAUSE, false);
        str = one_data_pause + "";
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        mPlaying = false;
        super.onDestroy();
    }
}
