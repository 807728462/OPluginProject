package com.oyf.pluginlibs;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 11:52
 * @描述
 **/
public class PackageCheck {
    private final static String TAG = PackageCheck.class.getSimpleName();
    private static PackageCheck instance;

    private PackageCheck() {
    }

    public static PackageCheck getInstance() {
        if (null == instance) {
            instance = new PackageCheck();
        }
        return instance;
    }

    /**
     * hook住startactivity返回后的handler，在handler中处理packagename，
     * 还有创建activity之后，绕过pms的检测，会根据packagename检测是否已经安装app
     */
    public void hookHandler() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            //获取handler中的handler属性
            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            //用于获取activithThread
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            final Object activityThread = currentActivityThreadMethod.invoke(null);
            Object mH = mHField.get(activityThread);

            //获取handler中的mCallBack属性，用于我们自己创建一个，然后去自行处理返回过来的Intent
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            //自己设置一个callBack进入，返回值为true时需要自行调用handlerMessage,false则不需要处理
            mCallbackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    //从msg中获取到系统返回过来的intent
                    if (msg.what == Parameter.LAUNCH_ACTIVITY) {
                        try {
                            Object obj = msg.obj;
                            // 我们要获取之前Hook携带过来的 TestActivity
                            Field intentField = obj.getClass().getDeclaredField("intent");
                            intentField.setAccessible(true);
                            // 获取 intent 对象，才能取出携带过来的 actionIntent
                            Intent intent = (Intent) intentField.get(obj);
                            Intent targetIntent = intent.getParcelableExtra(Parameter.TARGET_INTENT_KEY);

                            if (null != targetIntent) {
                                intentField.set(obj, targetIntent);

                                Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
                                activityInfoField.setAccessible(true);
                                ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);

                                //如果是插件的activity的话  activityInfo.getpackage为空
                                if (targetIntent.getPackage() == null) {
                                    activityInfo.applicationInfo.packageName = targetIntent.getComponent().getPackageName();
                                    hookPackageCheck(activityThread);
                                } else {
                                    activityInfo.applicationInfo.packageName = targetIntent.getPackage();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 使用动态代理监听系统的IPackageManager   会去检测包是否安装
     *
     * @param activityThread
     */
    private void hookPackageCheck(Object activityThread) {
        try {
            // sPackageManager 替换  我们自己的动态代理
            Field getPackageManagerField = activityThread.getClass().getDeclaredField("sPackageManager");
            getPackageManagerField.setAccessible(true);
            final Object iPackageManager = getPackageManagerField.get(activityThread);

            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");
            Object proxyPackageManager = Proxy.newProxyInstance(activityThread.getClass().getClassLoader(), new Class[]{iPackageManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    if ("getPackageInfo".equals(method.getName())) {
                        // 如何才能绕过 PMS, 欺骗系统
                        // pi != null

                        Log.d(TAG, "getPackageInfo绕过检测");
                        return new PackageInfo(); // 成功绕过 PMS检测
                    }
                    Log.d(TAG, "getPackageInfo正常执行");
                    // 让系统正常继续执行下去
                    return method.invoke(iPackageManager, objects);
                }
            });
            getPackageManagerField.set(activityThread, proxyPackageManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
