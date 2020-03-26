package com.oyf.login;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.oyf.login.base.BaseLoginPluginActivity;
import com.oyf.plugininterface.base.BasePluginBroadcastReceiver;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseLoginPluginActivity {

    private static final String ACTION_LOGIN_ONE = "ACTION_LOGIN_ONE";
    private static final String ACTION_LOGIN_TWO = "ACTION_LOGIN_TWO";

    private Map<String, BasePluginBroadcastReceiver> mBroadcastInterfaceMap = new HashMap<>();
    private Intent mOneService = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        /****************第一个广播*********************/
        findViewById(R.id.bt_login_receiver_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasePluginBroadcastReceiver pluginOneBroadcastReceiver = mBroadcastInterfaceMap.get(ACTION_LOGIN_ONE);
                if (null == pluginOneBroadcastReceiver) {
                    pluginOneBroadcastReceiver = new PluginLoginBroadcastReceiver();
                    mBroadcastInterfaceMap.put(ACTION_LOGIN_ONE, pluginOneBroadcastReceiver);
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_LOGIN_ONE);
                registerReceiver(pluginOneBroadcastReceiver, intentFilter);
            }
        });
        findViewById(R.id.bt_login_unreceiver_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasePluginBroadcastReceiver pluginOneBroadcastReceiver = mBroadcastInterfaceMap.get(ACTION_LOGIN_ONE);
                if (null != pluginOneBroadcastReceiver) {
                    unregisterReceiver(pluginOneBroadcastReceiver);
                    mBroadcastInterfaceMap.remove(ACTION_LOGIN_ONE);
                }
            }
        });
        findViewById(R.id.bt_login_send_receiver_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(ACTION_LOGIN_ONE);
                intent.putExtra(PluginLoginBroadcastReceiver.KEY_LOGIN_DATA, "我是login的data");
                sendBroadcast(intent);
            }
        });

        /****************第一个服务*********************/
        findViewById(R.id.bt_login_start_service_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOneService = new Intent(mActivity, PluginLoginService.class);
                mOneService.putExtra(PluginLoginService.KEY_LOGIN_DATA, "我是login的service");
                startService(mOneService);
            }
        });
        findViewById(R.id.bt_login_pause_service_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOneService = new Intent(mActivity, PluginLoginService.class);
                mOneService.putExtra(PluginLoginService.KEY_LOGIN_DATA_PAUSE, true);
                startService(mOneService);
            }
        });
        findViewById(R.id.bt_login_stop_service_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOneService = new Intent(mActivity, PluginLoginService.class);
                stopService(mOneService);
            }
        });
    }
}
