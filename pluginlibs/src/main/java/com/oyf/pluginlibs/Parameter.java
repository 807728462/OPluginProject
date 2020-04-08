package com.oyf.pluginlibs;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 10:08
 * @描述
 **/
public class Parameter {
    public static String TARGET_INTENT_KEY = "targetIntent"; //  目标Activity
    public static String PROXY_ACTIVITY_PACKAGE = "com.oyf.plugin"; //  代理的activity包名
    public static String PROXY_ACTIVITY_CLASS_NAME = "com.oyf.plugin.TestActivity"; //  代理的activity类名

    public static int EXECUTE_TRANSACTION = 159; //  在ActivityThread中即将还要去实例化Activity 会经过此Handler标记  适用于高版本

    public static int LAUNCH_ACTIVITY = 100; //  在ActivityThread中即将还要去实例化Activity 会经过此Handler标记

    public static String PLUGIN_FILE_NAME = "pluginapk-debug.apk"; // T 插件名
}
