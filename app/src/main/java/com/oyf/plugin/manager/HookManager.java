package com.oyf.plugin.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oyf.plugin.proxy.ProxyActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @创建者 oyf
 * @创建时间 2020/3/30 10:11
 * @描述 使用替换Intent式
 **/
public class HookManager {
    private final static String TAG = HookManager.class.getSimpleName();

    private static HookManager instance;

    private HookManager() {
    }

    public static HookManager getInstance() {
        if (null == instance) {
            instance = new HookManager();
        }
        return instance;
    }

    public void hookAMS(final Context context) {
        try {
            //拿到我们需要hook的接口
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            //拿到需要hook的对象，因为是静态的，可以直接获取
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerNativeClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            final Object iActivityManager = getDefaultMethod.invoke(null);

            Object iActivityManagerProxy = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{iActivityManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("startActivity".equals(method.getName())) {
                        // 用ProxyActivity 绕过了 AMS检查
                        Intent intent = new Intent(context, ProxyActivity.class);
                        intent.putExtra("actionIntent", ((Intent) args[2])); // 把之前TestActivity保存 携带过去
                        args[2] = intent;
                    }
                    Log.d("hook", "拦截到了IActivityManager里面的方法" + method.getName());

                    // 让系统继续正常往下执行
                    return method.invoke(iActivityManager, args);

                }
            });

            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object gDefault = gDefaultField.get(null);

            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            mInstanceField.set(gDefault, iActivityManagerProxy);
        } catch (Exception e) {
            Log.d(TAG, "hook  ams  失败");
        }
    }
}
