package com.oyf.pluginlibs;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * @创建者 oyf
 * @创建时间 2020/4/7 10:10
 * @描述 宿主和插件 DexElement进行合并 融合
 **/
public class DexElementFuse {

    private static DexElementFuse instance;

    private DexElementFuse() {
    }

    public static DexElementFuse getInstance() {
        if (null == instance) {
            instance = new DexElementFuse();
        }
        return instance;
    }

    private Resources resources = null;
    private AssetManager assetManager = null;

    /**
     * 同学们，此方法的主要目的是，宿主和插件的 DexElement融合
     */
    public void mainPluginFuse(Context mContext) throws Exception {
        File file = new File(OPathUtils.getRootDir() + File.separator + Parameter.PLUGIN_FILE_NAME);
        if (file.exists() == false) {
            return;
        }

        //  宿主的dexElements
        Object mainDexElements = getDexElements(mContext.getClassLoader());
        /*Class mBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = mBaseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object mDexPathList = pathListField.get(mContext.getClassLoader());
        Field dexElementsField = mDexPathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object mainDexElements = dexElementsField.get(mDexPathList);
        */
        //  插件的dexElements
        File fileDir = mContext.getDir("pDir", Context.MODE_PRIVATE);
        DexClassLoader dexClassLoader = new DexClassLoader(file.getAbsolutePath(), fileDir.getAbsolutePath(), null, mContext.getClassLoader());
        Object pluginDexElements = getDexElements(dexClassLoader);
        /*Class mBaseDexClassLoaderClass2 = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField2 = mBaseDexClassLoaderClass2.getDeclaredField("pathList");
        pathListField2.setAccessible(true);
        Object mDexPathList2 = pathListField2.get(dexClassLoader);
        Field dexElementsField2 = mDexPathList2.getClass().getDeclaredField("dexElements");
        dexElementsField2.setAccessible(true);
        Object pluginDexElements = dexElementsField2.get(mDexPathList2);*/

        //创造出新的 newDexElements
        int mainLen = Array.getLength(mainDexElements);
        int pluginLen = Array.getLength(pluginDexElements);
        int newDexElementsLength = (mainLen + pluginLen);
        Object newDexElements = Array.newInstance(mainDexElements.getClass().getComponentType(), newDexElementsLength);

        // 进行融合
        for (int i = 0; i < newDexElementsLength; i++) {
            // 先融合宿主
            if (i < mainLen) {
                Array.set(newDexElements, i, Array.get(mainDexElements, i));
            } else { // 在融合插件，为什么要i - mainLen，是为了保证取出pluginDexElements，是从0 开始取的
                Array.set(newDexElements, i, Array.get(pluginDexElements, i - mainLen));
            }
        }
        // 把新的替换到宿主中去
        Class mBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = mBaseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object mDexPathList = pathListField.get(mContext.getClassLoader());
        Field dexElementsField = mDexPathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        dexElementsField.set(mDexPathList, newDexElements);

        loadResource(mContext, file.getAbsolutePath());
    }

    // todo   其实 宿主的dexElements 和  插件的dexElements 代码类似，所以可以抽取成方法的
    private Object getDexElements(ClassLoader classLoader) throws Exception {
        Class mBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = mBaseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object mDexPathList = pathListField.get(classLoader);
        Field dexElementsField = mDexPathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        return dexElementsField.get(mDexPathList);
    }

    /**
     * 拥有加载资源的能力
     *
     * @param mContext
     * @throws Exception
     */
    public void loadResource(Context mContext, String pluginPath) throws Exception {
        Resources r = mContext.getResources();
        assetManager = AssetManager.class.newInstance();
        Method addAssetpathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
        addAssetpathMethod.setAccessible(true);
        addAssetpathMethod.invoke(assetManager, pluginPath);
        resources = new Resources(assetManager, r.getDisplayMetrics(), r.getConfiguration());

        // 实例化此方法 final StringBlock[] ensureStringBlocks()
         /*   Method ensureStringBlocksMethod = assetManager.getClass().getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocksMethod.setAccessible(true);
            ensureStringBlocksMethod.invoke(assetManager);// 执行了ensureStringBlocks  string.xml  color.xml   anim.xml 被初始化 */
    }

    public Resources getResources() {
        return resources;
    }

    /**
     * 只需要 让插件去那 宿主的getResources 就好了，不需要让插件去那AssetManager了
     * 因为宿主和插件进行了融合，插件只要拿到宿主中的Resources，就等于拿到了 AssetManager了，因为AssetManager属于单利的哦
     *
     * @return
     */
    public AssetManager getAssetManager() {
        return assetManager;
    }

}
