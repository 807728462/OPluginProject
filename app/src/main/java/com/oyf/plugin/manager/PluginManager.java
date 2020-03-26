package com.oyf.plugin.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.oyf.plugininterface.OPathUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 11:28
 * @描述
 **/
public class PluginManager {
    private final static String TAG = PluginManager.class.getSimpleName();

    private static PluginManager instance;

    private PluginManager() {
        mDexClassLoaderMap = new HashMap<>();
        mResourcesMap = new HashMap<>();
    }

    public static PluginManager getInstance() {
        if (null == instance) {
            instance = new PluginManager();
        }
        return instance;
    }

    private Map<String, DexClassLoader> mDexClassLoaderMap;
    private Map<String, Resources> mResourcesMap;
    private Resources mResources;
    private AssetManager assetManager;


    public void loadApk(Context context, String apk) {
        if (mDexClassLoaderMap.containsKey(apk)) {
            Log.d(TAG, apk + "已加载");
            return;
        }

        File apkFile = new File(OPathUtils.getRootDir() + File.separator + apk);
        if (null == apkFile || !apkFile.exists()) {
            Log.d(TAG, "加载的" + apk + "文件不存在");
            return;
        }
        creatClassLoader(context, apk, apkFile.getAbsolutePath());
        creatResoure(context, apk, apkFile.getAbsolutePath());
        registerBroadcast(context, apk);
        Log.d(TAG, "加载的" + apk + "成功");
    }

    /**
     * 创建classloader
     *
     * @param context
     */
    private void creatClassLoader(Context context, String apkName, String apkPath) {
        DexClassLoader dexClassLoader = mDexClassLoaderMap.get(apkName);
        if (null == dexClassLoader) {
            dexClassLoader = new DexClassLoader(apkPath, OPathUtils.getOptimizedDirectory(context), null, context.getClassLoader());
            mDexClassLoaderMap.put(apkName, dexClassLoader);
        }
    }

    /**
     * 创建资源
     *
     * @param context
     * @param apkPath
     */
    private void creatResoure(Context context, String apk, String apkPath) {
        //AssetManager assetManager = context.getAssets();
        try {
            Resources resources = mResourcesMap.get(apk);
            if (null == resources) {
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
                addAssetPathMethod.invoke(assetManager, apkPath);
                Resources resource = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
                mResourcesMap.put(apk, resource);
            }
        } catch (Exception e) {
            Log.d(TAG, "加载的" + apkPath + "的Resources失败文件不存在");
        }
    }

    /**
     * 静态注册插件包里面的广播
     *
     * @param context
     * @param apk
     */
    private void registerBroadcast(Context context, String apk) {
        //通过自己使用packageParse去解析apk文件，然后获取到广播后进行注册
        try {
            File apkFile = new File(OPathUtils.getRootDir() + File.separator + apk);
            //1.先通过反射创建packageParse
            Class packageParserClass = Class.forName("android.content.pm.PackageParser");
            Object packageParser = packageParserClass.newInstance();
            //获取parsePackage方法
            Method parsePackageMethod = packageParser.getClass().getDeclaredMethod("parsePackage", File.class, int.class);
            //执行packageParse.parsePackage(file,flag)返回值是PackageParser.Package
            Object packageParserPackage = parsePackageMethod.invoke(packageParser, apkFile, PackageManager.GET_ACTIVITIES);
            //获取中PackageParser.Package 的public final ArrayList<Activity> receivers,就是所有广播的集合
            Field receiversField = packageParserPackage.getClass().getField("receivers");
            ArrayList receiverArrays = (ArrayList) receiversField.get(packageParserPackage);
            //循环所有的广播进行注册,1.获取类路径 2.获取intentFilter
            for (Object receiver : receiverArrays) {
                //先获取ActivityInfo info，存放着类路径,获取name字段就ok
                Field activityInfoField = receiver.getClass().getDeclaredField("info");
                activityInfoField.setAccessible(true);
                Object activityInfo = activityInfoField.get(receiver);
                Field nameField = activityInfo.getClass().getField("name");
                String className = (String) nameField.get(activityInfo);
                //创建广播,注意使用apk的classLoder，不可以使用class.forName
                Class<?> pluginBroadcastClass = getDexClassLoader(apk).loadClass(className);
                Object pluginBroadcast = pluginBroadcastClass.newInstance();
                //获取广播过滤器的集合，因为广播可以有多个过滤器 public final ArrayList<II> intents;
                Field intentsField = receiver.getClass().getField("intents");
                intentsField.setAccessible(true);
                ArrayList intents = (ArrayList) intentsField.get(receiver);
                for (Object intent : intents) {
                    context.registerReceiver((BroadcastReceiver) pluginBroadcast, (IntentFilter) intent);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "添加静态广播失败");
        }
    }

    public DexClassLoader getDexClassLoader(String apk) {
        return mDexClassLoaderMap.get(apk);
    }

    public Resources getResources(String apk) {
        return mResourcesMap.get(apk);
    }
}
