package com.oyf.pluginlibs;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 10:51
 * @描述 插件化框架类型
 **/
public enum PluginTypeEnum {
    PROXY_PLUGIN(1), LOADEDAPK_PLUGIN(2), HOOK_PLUGIN(3);

    PluginTypeEnum(int type) {
        this.type = type;
    }

    private int type;

    public int getValue() {
        return type;
    }

}
