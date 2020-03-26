package com.oyf.pluginapk.base;

import com.oyf.pluginapk.BuildConfig;
import com.oyf.plugininterface.base.BasePluginActivity;

/**
 * @创建者 oyf
 * @创建时间 2020/3/26 14:03
 * @描述
 **/
public abstract class BaseApkPluginActivity extends BasePluginActivity {

    @Override
    public String getApkName() {
        return BuildConfig.apkName;
    }
}
