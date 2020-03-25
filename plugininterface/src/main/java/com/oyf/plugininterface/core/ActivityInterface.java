package com.oyf.plugininterface.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 11:14
 * @描述 给插件activity定义规范
 **/
public interface ActivityInterface {

    void initContext(Activity activity);

    void onStart();

    void onCreate(Bundle savedInstanceState);

    void onResume();

    void onPause();

    void onRestart();

    void onStop();

    void onDestroy();

    void setContentView(int resId);

    void startActivity(Intent intent);

}
