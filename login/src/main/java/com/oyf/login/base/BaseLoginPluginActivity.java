package com.oyf.login.base;

import com.oyf.login.BuildConfig;
import com.oyf.plugininterface.base.BasePluginActivity;

/**
 * @创建者 oyf
 * @创建时间 2020/3/26 14:07
 * @描述
 **/
public abstract class BaseLoginPluginActivity extends BasePluginActivity {
    @Override
    public String getApkName() {
        return BuildConfig.apkName;
    }
}
