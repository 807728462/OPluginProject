package com.oyf.pluginlibs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 10:37
 * @描述 专门处理绕过AMS检测，让LoginActivity可以正常通过
 **/
public class AMSCheckEngine {

    /**
     *  为了自行设置代理的activity的包名跟类名
     * @param context
     * @param proxyPackage
     * @param proxyClassName
     */
    public static void hookAMS(final Context context, String proxyPackage, String proxyClassName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Parameter.PROXY_ACTIVITY_PACKAGE = proxyPackage;
        Parameter.PROXY_ACTIVITY_CLASS_NAME = proxyClassName;
        hookAMS(context);
    }

    /**
     * 此方法 适用于 21以下的版本 以及 21_22_23_24_25  26_27_28 等系统版本
     *
     * @param mContext
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static void hookAMS(final Context mContext) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        // 公共区域
        Object mIActivityManagerSingleton = null; // 公共区域 适用于 21以下的版本 以及 21_22_23_24_25  26_27_28 等系统版本
        Object mIActivityManager = null; // 公共区域 适用于 21以下的版本 以及 21_22_23_24_25  26_27_28 等系统版本

        if (AndroidSdkVersion.isAndroidOS_26_27_28()) {
            // @3 的获取    系统的 IActivityManager.aidl
            Class mActivityManagerClass = Class.forName("android.app.ActivityManager");
            mIActivityManager = mActivityManagerClass.getMethod("getService").invoke(null);


            // @1 的获取    IActivityManagerSingleton
            Field mIActivityManagerSingletonField = mActivityManagerClass.getDeclaredField("IActivityManagerSingleton");
            mIActivityManagerSingletonField.setAccessible(true);
            mIActivityManagerSingleton = mIActivityManagerSingletonField.get(null);

        } else if (AndroidSdkVersion.isAndroidOS_21_22_23_24_25()) {
            // @3 的获取
            Class mActivityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = mActivityManagerClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            mIActivityManager = getDefaultMethod.invoke(null);

            // @1 的获取 gDefault
            Field gDefaultField = mActivityManagerClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            mIActivityManagerSingleton = gDefaultField.get(null);
        }

        // @2 的获取    动态代理
        Class mIActivityManagerClass = Class.forName("android.app.IActivityManager");
        final Object finalMIActivityManager = mIActivityManager;
        Object mIActivityManagerProxy = Proxy.newProxyInstance(mContext.getClassLoader(),
                new Class[]{mIActivityManagerClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("startActivity".equals(method.getName())) {
                            // 把LoginActivity 换成 ProxyActivity
                            // 狸猫换太子，把不能经过检测的LoginActivity 替换 成能够经过检测的ProxyActivity
                            Intent proxyIntent = new Intent();
                            proxyIntent.setClassName(Parameter.PROXY_ACTIVITY_PACKAGE, Parameter.PROXY_ACTIVITY_CLASS_NAME);
                            // 把目标的LoginActivity 取出来 携带过去
                            Intent target = (Intent) args[2];
                            Log.d("hook", "proxyIntent1:" + target);
                            proxyIntent.putExtra(Parameter.TARGET_INTENT_KEY, target);
                            args[2] = proxyIntent;

                            Log.d("hook", "proxyIntent2:" + proxyIntent);
                        }
                        // @3
                        return method.invoke(finalMIActivityManager, args);
                    }
                });

        if (mIActivityManagerSingleton == null || mIActivityManagerProxy == null) {
            throw new IllegalStateException("实在是没有检测到这种系统，需要对这种系统单独处理..."); // 10.0
        }

        Class mSingletonClass = Class.forName("android.util.Singleton");

        Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // 把系统里面的 IActivityManager 换成 我们自己写的动态代理 【第一步】
        mInstanceField.set(mIActivityManagerSingleton, mIActivityManagerProxy);
    }
}