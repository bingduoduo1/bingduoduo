
package com.bingduoduo.editor.utils;

import android.content.Context;
import android.os.SystemClock;
import android.view.Gravity;

public class Toast {
    /**
     * @Description Toast 全局控制
     */
    public static boolean isShow = true;
    public static android.widget.Toast toast;
    public static final int LENGTH_SHORT = android.widget.Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = android.widget.Toast.LENGTH_LONG;
    public static String oldMsg = "";
    private static long oldTime = 0;
    private static long newTime = 0;

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, CharSequence message) {
        showToast(context, message.toString(), LENGTH_SHORT);
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, CharSequence message) {
        showToast(context, message.toString(), LENGTH_LONG);
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, CharSequence message, int duration) {
        showToast(context, message.toString(), duration);
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, int message, int duration) {
        showToast(context, context.getResources().getString(message), duration);
    }

    private static void showToast(Context context, String message, int duration) {
        if (!isShow || message == null || context == null) {
            return;
        }
        if (toast == null) {
            toast = android.widget.Toast.makeText(context, message, duration);
            toast.setGravity(Gravity.BOTTOM, 0, 300);
            toast.show();
        } else {
            if (oldMsg.equals(message)) {
                newTime = SystemClock.uptimeMillis();
                if (newTime - oldTime >= 50) {
                    toast.show();
                }
            } else {
                oldTime = SystemClock.uptimeMillis();
                toast.setText(message);
                toast.setDuration(duration);
                toast.show();
            }
        }
    }
}
