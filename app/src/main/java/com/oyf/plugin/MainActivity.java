package com.oyf.plugin;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.oyf.plugin.proxy.ProxyActivity;
import com.oyf.plugin.proxy.ProxyService;
import com.oyf.plugininterface.utils.ArouterUtils;
import com.oyf.pluginlibs.OPathUtils;

import java.io.File;

import static com.oyf.plugininterface.utils.ArouterUtils.KEY_CLASS_NAME;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadPlugin(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void loadPlugin(View view) {
        //PluginManager.getInstance().loadApk(this, mPluginApkPath);
    }

    public void startPluginActivity(View view) {
        PackageManager packageManager = getPackageManager();
        File apkFile = new File(OPathUtils.getRootDir() + File.separator + APP.mPluginApkPath);
        PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(
                apkFile.getAbsolutePath(),
                PackageManager.GET_ACTIVITIES);
        ActivityInfo activityInfo = packageArchiveInfo.activities[0];
        Intent intent = new Intent(this, ProxyActivity.class);
        intent.putExtra(ArouterUtils.KEY_APK_NAME, APP.mPluginApkPath);
        intent.putExtra(KEY_CLASS_NAME, activityInfo.name);
        startActivity(intent);
    }

    public void startServiceClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ProxyService.class);
        intent.putExtra(ArouterUtils.KEY_APK_NAME, APP.mPluginApkPath);
        startService(intent);
    }

    public void startTest(View view) {
        Intent intent = new Intent(this, ProxyActivity.class);
        startActivity(intent);
    }

    public void startPluginTest(View view) {
        Intent intent = new Intent();
        intent.setClassName("com.oyf.pluginapk", "com.oyf.pluginapk.PluginTestActivity");
        startActivity(intent);
    }

    public void startPluginLoadedapkTest(View view) {
        Intent intent = new Intent();
        intent.setClassName("com.oyf.pluginapk", "com.oyf.pluginapk.PluginLoadedApkActivity");
        startActivity(intent);
    }
}
