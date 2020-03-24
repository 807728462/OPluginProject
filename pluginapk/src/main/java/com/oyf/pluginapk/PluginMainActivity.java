package com.oyf.pluginapk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.oyf.pluginapk.base.BasePluginActivity;

public class PluginMainActivity extends BasePluginActivity {
    private static final String TAG = BasePluginActivity.class.getSimpleName();

    @Override
    public int getLayoutId() {
        Log.d(TAG, "PluginMainActivity.getLayoutId");
        return R.layout.plugin_activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        Log.d(TAG, "PluginMainActivity.initData(");
        super.initData(savedInstanceState);
        toast("我是插件toast");
        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, PluginTwoActivity.class);
                startActivity(intent);
            }
        });
    }
}
