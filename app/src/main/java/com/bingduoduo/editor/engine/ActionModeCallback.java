
package com.bingduoduo.editor.engine;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * ActionMode回调 状态栏又要保持透明，又要有颜色
 * 可以用v7包下的ActionMode也可以用普通的ActionMode
 * 如果是v7 则startSupportActionMode(pasteModeCallback);
 * 普通的startActionMode(pasteModeCallback);
 */
public abstract class ActionModeCallback implements ActionMode.Callback {
    private static float DEFAULT_ALPHA = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.1f : 0.2f;
    protected int statusBarColor;
    private Activity mActivity;
    private int mActionModeStatusBarColor = 0;

    protected ActionModeCallback(@NonNull Activity activity, @ColorRes int actionModeStatusBarColorRes) {
        this.mActionModeStatusBarColor = activity.getResources().getColor(actionModeStatusBarColorRes);
        this.mActivity = activity;
    }

    @Override
    public final boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBarColor = mActivity.getWindow().getStatusBarColor();
            //set your gray color
            mActivity.getWindow().setStatusBarColor(mActionModeStatusBarColor);
//            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        return onCreateActionModeCustom(mode, menu);
    }

    public abstract boolean onCreateActionModeCustom(ActionMode mode, Menu menu);

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public final void onDestroyActionMode(ActionMode mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActivity.getWindow().setStatusBarColor(statusBarColor);
        }
        onDestroyActionModeCustom(mode);
    }

    public abstract void onDestroyActionModeCustom(ActionMode mode);
}
