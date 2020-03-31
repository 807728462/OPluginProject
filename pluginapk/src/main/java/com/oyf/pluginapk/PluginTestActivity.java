package com.oyf.pluginapk;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @创建者 oyf
 * @创建时间 2020/3/31 11:57
 * @描述
 **/
public class PluginTestActivity extends AppCompatActivity {
    @Override
    public Resources getResources() {
        if (getApplicationContext() != null && getApplicationContext().getResources() != null) {
            return getApplicationContext().getResources();
        }
        return super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        if (getApplicationContext() != null && getApplicationContext().getAssets() != null) {
            return getApplicationContext().getAssets();
        }
        return super.getAssets();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_activity_test);
        Toast.makeText(this, "woshichajianhua", Toast.LENGTH_SHORT).show();
        Log.d("PluginTestActivity", "打印string==" + getResources().getString(R.string.app_name));
    }
}
