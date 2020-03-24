package com.oyf.plugininterface;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @创建者 oyf
 * @创建时间 2020/3/24 11:34
 * @描述
 **/
public class OPathUtils {
    public static String getRootDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getOptimizedDirectory(Context context) {
        return context.getDir("p", Context.MODE_PRIVATE).getAbsolutePath();
    }
}
