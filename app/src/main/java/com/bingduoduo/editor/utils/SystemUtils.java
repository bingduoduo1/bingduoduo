
package com.bingduoduo.editor.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bingduoduo.editor.base.BaseApplication;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 系统相关工具集合
 * 来至网上，作者未知（表示感谢）
 */
public class SystemUtils {
    /**
     * 复制到剪切板
     *
     */
    public static void copyToClipBoard(Context context, String text) {
        ClipData clipData = ClipData.newPlainText("text_copy", text);
        ClipboardManager manager = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(clipData);
    }

    public static String getAssertString(Context context, String filename) {
        AssetManager am = context.getAssets();
        InputStream is = null;

        try {
            is = am.open(filename);
            return new String(readInputStream(is)).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static byte[] readInputStream(InputStream in) {
        byte[] buffer = null;
        try {
            int length = in.available();
            buffer = new byte[length];
            in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
