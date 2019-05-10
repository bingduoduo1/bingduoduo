

package com.bingduoduo.editor.base;

import android.os.Build;
import androidx.annotation.NonNull;
//import android.support.design.widget.AppBarLayout;
//import androidx.app.ActionBar;
//import android.support.v7.widget.Toolbzar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.termux.R;
import com.bingduoduo.editor.utils.Check;
import com.google.android.material.appbar.AppBarLayout;

import java.lang.reflect.Method;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import butterknife.Bind;


/**
 * 带有Toolbar的Activity封装
 */
public abstract class BaseToolbarActivity extends BaseActivity {
    @Bind(R.id.id_toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.id_appbarLayout)
    protected AppBarLayout mAppBar;

    @Override
    protected void init() {
        super.init();
        initActionBar(mToolbar);
        initAppBarLayout(mAppBar);
    }

    protected void initAppBarLayout(AppBarLayout appBar) {
        if (appBar == null) return;
        if (Build.VERSION.SDK_INT >= 21) {
            this.mAppBar.setElevation(0f);
        }
    }
    /**
     * 初始化actionbar
     *
     * @param toolbar the mToolbar
     */
    private void initActionBar(Toolbar toolbar) {
        if (!Check.isEmpty(getSubtitleString())) {
            toolbar.setSubtitle(getSubtitleString());
        }
        toolbar.setTitle(getTitleString());
        setSupportActionBar(toolbar);
        if (hasBackButton()) {//如果需要返回按钮
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }


    public AppBarLayout getAppBar() {
        return mAppBar;
    }

    /**
     * 子类可以重写,若不重写默认为程序名字
     *
     * @return 返回主标题的资源id
     */
    @NonNull
    protected String getTitleString() {
        return BaseApplication.string(R.string.app_name);
    }

    protected boolean hasBackButton() {
        return false;
    }
    /**
     * 子类可以重写,若不重写默认为空 返回String资源
     *
     * @return 副标题的资源id
     */
    @NonNull
    protected String getSubtitleString() {
        return "";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // finish();
                onBackPressed();// 返回
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setOverflowIconVisible(menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 显示菜单图标
     *
     * @param menu menu
     */
    private void setOverflowIconVisible(Menu menu) {
        try {
            Class clazz = Class.forName("android.support.v7.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            Log.d("OverflowIconVisible", e.getMessage());
        }
    }
}
