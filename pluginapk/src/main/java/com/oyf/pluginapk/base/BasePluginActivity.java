package com.oyf.pluginapk.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.oyf.plugininterface.core.ActivityInterface;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 11:18
 * @描述
 **/
public abstract class BasePluginActivity extends Activity implements ActivityInterface {
    private static final String TAG = BasePluginActivity.class.getSimpleName();
    protected Activity mActivity;

    @Override
    public void initContext(Activity activity) {
        mActivity = activity;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onStart() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "BasePluginActivity.onCreate");
        setContentView(getLayoutId());
        initView(savedInstanceState);
        initData(savedInstanceState);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onResume() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onPause() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRestart() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onStop() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onDestroy() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void setContentView(int resId) {
        Log.d(TAG, "BasePluginActivity.setContentView");
        if (null != mActivity) {
            mActivity.setContentView(resId);
        }
    }

    /**************************************处理activity**************************************************************************/

    @SuppressLint("MissingSuperCall")
    @Override
    public void startActivity(Intent intent) {
        Log.d(TAG, "BasePluginActivity.startActivity=" + intent.getComponent().toString());
        if (null != mActivity) {
            mActivity.startActivity(intent);
        }
    }

    /**************************************处理广播**************************************************************************/
    @SuppressLint("MissingSuperCall")
    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        mActivity.unregisterReceiver(receiver);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mActivity.sendBroadcast(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public Intent registerReceiver(
            BroadcastReceiver receiver, IntentFilter filter) {
        return mActivity.registerReceiver(receiver, filter);
    }

    /**************************************处理服务*************************************************************************/
    @Override
    public ComponentName startService(Intent service) {
        return mActivity.startService(service);
    }

    @Override
    public boolean stopService(Intent name) {
        return mActivity.stopService(name);
    }

    public void initView(Bundle savedInstanceState) {

    }

    public void initData(Bundle savedInstanceState) {

    }

    public View findViewById(int id) {
        Log.d(TAG, "BasePluginActivity.findViewById");
        if (null != mActivity) {
            return mActivity.findViewById(id);
        }
        return null;
    }

    public void toast(String text) {
        Log.d(TAG, "BasePluginActivity.toast");
        if (null != mActivity)
            Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }

    public abstract int getLayoutId();
}
