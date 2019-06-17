package com.bingduoduo.editor.base;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bingduoduo.editor.utils.SystemBarUtils;
import com.google.android.material.navigation.NavigationView;
import com.termux.R;

import butterknife.Bind;

public abstract class BaseDrawerLayoutActivity extends BaseToolbarActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.id_drawer_layout)
    protected DrawerLayout mdrawerLayout;
    @Bind(R.id.id_navigation_view)
    protected NavigationView mnavigationView;

    protected DrawerLayout getDrawerLayout() {
        return mdrawerLayout;
    }

    protected NavigationView getNavigationView() {
        return mnavigationView;
    }

    @Override
    protected void init() {
        super.init();
        initDrawer();
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    protected void initStatusBar() {
        SystemBarUtils.tintStatusBarForDrawer(this, mdrawerLayout, getResources().getColor(R.color.colorPrimary));
    }

    private void initDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mdrawerLayout, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mdrawerLayout.setDrawerListener(toggle);
        toggle.syncState();


        mnavigationView.setNavigationItemSelectedListener(this);

        ColorStateList colorStateList = new ColorStateList(
            new int[][]{{android.R.attr.state_checked, android.R.attr.state_enabled},
                {android.R.attr.state_enabled},
                {}},
            new int[]{BaseApplication.color(R.color.colorPrimary), BaseApplication.color(R.color.colorSecondaryText), 0xffDCDDDD});
        mnavigationView.setItemIconTintList(colorStateList);//设置图标的颜色变化
        mnavigationView.setItemTextColor(colorStateList);//设置item的颜色变化
    }

    @Override
    public void onBackPressed() {
        //返回按钮
        if (mdrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mdrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
