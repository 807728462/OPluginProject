package com.oyf.pluginapk;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.oyf.pluginapk.base.BasePluginActivity;
import com.oyf.pluginapk.base.BasePluginBroadcastReceiver;

import java.util.HashMap;
import java.util.Map;

public class PluginMainActivity extends BasePluginActivity {
    private static final String TAG = BasePluginActivity.class.getSimpleName();
    private static final String ACTION_ONE = "plugin_one_receiver";
    private static final String ACTION_TWO = "plugin_two_receiver";

    private Map<String, BasePluginBroadcastReceiver> mBroadcastInterfaceMap = new HashMap<>();

    private Intent mOneService;
    private Intent mTwoService;

    @Override
    public int getLayoutId() {
        return R.layout.plugin_activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        toast("我是插件toast");
        /****************打开activity*********************/
        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, PluginTwoActivity.class);
                startActivity(intent);
            }
        });
        /****************第一个广播*********************/
        findViewById(R.id.bt_receiver_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasePluginBroadcastReceiver pluginOneBroadcastReceiver = mBroadcastInterfaceMap.get(ACTION_ONE);
                if (null == pluginOneBroadcastReceiver) {
                    pluginOneBroadcastReceiver = new PluginOneBroadcastReceiver();
                    mBroadcastInterfaceMap.put(ACTION_ONE, pluginOneBroadcastReceiver);
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("plugin_one_receiver");
                registerReceiver(pluginOneBroadcastReceiver, intentFilter);
            }
        });
        findViewById(R.id.bt_unreceiver_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasePluginBroadcastReceiver pluginOneBroadcastReceiver = mBroadcastInterfaceMap.get(ACTION_ONE);
                if (null != pluginOneBroadcastReceiver) {
                    unregisterReceiver(pluginOneBroadcastReceiver);
                    mBroadcastInterfaceMap.remove(ACTION_ONE);
                }
            }
        });
        findViewById(R.id.bt_send_receiver_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(ACTION_ONE);
                intent.putExtra("one_data", "我是onedata");
                sendBroadcast(intent);
            }
        });
        /****************第二个广播*********************/
        findViewById(R.id.bt_receiver_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasePluginBroadcastReceiver broadcastReceiver = mBroadcastInterfaceMap.get(ACTION_TWO);
                if (null == broadcastReceiver) {
                    broadcastReceiver = new PluginTwoBroadcastReceiver();
                    mBroadcastInterfaceMap.put(ACTION_TWO, broadcastReceiver);
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_TWO);
                registerReceiver(broadcastReceiver, intentFilter);
            }
        });
        findViewById(R.id.bt_unreceiver_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasePluginBroadcastReceiver broadcastReceiver = mBroadcastInterfaceMap.get(ACTION_TWO);
                if (null != broadcastReceiver) {
                    unregisterReceiver(broadcastReceiver);
                    mBroadcastInterfaceMap.remove(ACTION_TWO);
                }
            }
        });
        findViewById(R.id.bt_send_receiver_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(ACTION_TWO);
                intent.putExtra("two_data", "我是twodata");
                sendBroadcast(intent);
            }
        });
        findViewById(R.id.bt_unreceiver_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllBroadcastReceiver();
            }
        });
        /****************第一个服务*********************/
        findViewById(R.id.bt_start_service_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOneService = new Intent(mActivity, PluginOneService.class);
                mOneService.putExtra(PluginOneService.KEY_ONE_DATA, "我是oneservice");
                startService(mOneService);
            }
        });
        findViewById(R.id.bt_pause_service_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOneService = new Intent(mActivity, PluginOneService.class);
                mOneService.putExtra(PluginOneService.KEY_ONE_DATA_PAUSE, true);
                startService(mOneService);
            }
        });
        findViewById(R.id.bt_stop_service_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOneService = new Intent(mActivity, PluginOneService.class);
                stopService(mOneService);
            }
        });
        /****************第二个服务*********************/
        findViewById(R.id.bt_start_service_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTwoService = new Intent(mActivity, PluginTwoService.class);
                mTwoService.putExtra(PluginTwoService.KEY_TWO_DATA, "我是oneservice");
                startService(mTwoService);
            }
        });
        findViewById(R.id.bt_pause_service_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTwoService = new Intent(mActivity, PluginTwoService.class);
                mTwoService.putExtra(PluginTwoService.KEY_TWO_DATA_PAUSE, true);
                startService(mTwoService);
            }
        });
        findViewById(R.id.bt_stop_service_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTwoService = new Intent(mActivity, PluginTwoService.class);
                stopService(mTwoService);
            }
        });
    }

    @Override
    public void onDestroy() {
        clearAllBroadcastReceiver();
        super.onDestroy();
    }

    public void clearAllBroadcastReceiver() {
        for (String key : mBroadcastInterfaceMap.keySet()) {
            BasePluginBroadcastReceiver baseBroadcastReceiver = mBroadcastInterfaceMap.get(key);
            if (null != baseBroadcastReceiver) {
                unregisterReceiver(baseBroadcastReceiver);
            }
        }
    }
}
