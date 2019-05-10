
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
     * 显示键盘
     *
     * @param dialog
     */
    public static void showSoftKeyboard(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(4);
    }

    /**
     * 显示键盘
     *
     * @param view
     */
    public static void showSoftKeyboard(View view) {
        ((InputMethodManager) BaseApplication.context().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 隐藏键盘
     *
     * @param view
     */
    public static void hideSoftKeyboard(View view) {
        if (view == null)
            return;
        ((InputMethodManager) BaseApplication.context().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /***
     * 获取activity的宽度
     *
     * @param activity the activity
     * @return width pixels
     */
    public static int getWidthPixels(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /***
     * 获取activity的高度
     *
     * @param activity the activity
     * @return height pixels
     */
    public static int getHeightPixels(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }



    /**
     * 判断软件是前台还是后台
     *
     * @param context the mContext
     * @return boolean
     */
    public static boolean isBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }



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

    /**
     * 获取状态栏高度
     */
    public static int barHeight = 0;

    public static int getStatusBarHeight() {
        if (barHeight > 0) {
            return barHeight;
        }
        if (BaseApplication.context() == null) {
            return 0;
        }
        Class<?> c;
        Object obj;
        Field field;
        int x;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            barHeight = BaseApplication.context().getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            Log.i("test", "状态栏高度获取失败了");
        }
        return barHeight;
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
