package com.oyf.pluginapk;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @创建者 oyf
 * @创建时间 2020/4/1 10:46
 * @描述
 **/
public class PluginLoadedApkActivity extends AppCompatActivity {
    @Override
    public Resources getResources() {
        if (getApplicationContext() != null && getApplicationContext().getResources() != null) {
            return getApplicationContext().getResources();
        }
        return super.getResources();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_activity_loadedapk);
        Toast.makeText(this, "loadedapk", Toast.LENGTH_SHORT).show();
        Log.d("PluginLoadedApkActivity", "打印string==" + getResources().getString(R.string.app_name));
    }
}
