
package com.bingduoduo.editor.base;

import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bingduoduo.editor.utils.Check;
import com.google.android.material.appbar.AppBarLayout;
import com.termux.R;

import java.lang.reflect.Method;

import butterknife.Bind;

//import android.support.design.widget.AppBarLayout;
//import androidx.app.ActionBar;
//import android.support.v7.widget.Toolbzar;

/**
 * 带有Toolbar的Activity封装
 */
public abstract class BaseToolbarActivity extends BaseActivity {
    @Bind(R.id.id_toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.id_appbarLayout)
    protected AppBarLayout appBarLayout;
    
    @Override
    protected void init() {
        super.init();
        initActionBar(toolbar);
        initAppBarLayout(appBarLayout);
    }
    
    protected void initAppBarLayout(AppBarLayout appBar) {
        if (appBar == null)
        {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            this.appBarLayout.setElevation(0f);
        }
    }
    
    /**
     * 初始化actionbar
     *
     * @param toolbar the toolbar
     */
    private void initActionBar(Toolbar toolbar) {
        if (!Check.isEmpty(getSubtitleString())) {
            toolbar.setSubtitle(getSubtitleString());
        }
        toolbar.setTitle(getTitleString());
        setSupportActionBar(toolbar);
        if (hasBackButton()) {
            // 如果需要返回按钮
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            
        }
    }
    
    public Toolbar getToolbar() {
        return toolbar;
    }
    
    public AppBarLayout getAppBar() {
        return appBarLayout;
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
                onBackPressed();// 返回
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
