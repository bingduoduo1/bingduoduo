
package com.bingduoduo.editor.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LongDef;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;

import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.termux.R;
import com.bingduoduo.editor.base.BaseDrawerLayoutActivity;
import com.bingduoduo.editor.base.BaseFragment;
import com.bingduoduo.editor.utils.Toast;
import com.termux.app.TermuxActivity;

/**
 * The type Main activity.
 */
public class MainActivity extends BaseDrawerLayoutActivity {
    private BaseFragment mCurrentFragment;
//    private int currentMenuId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestPermissions();
        // 在 FolderMangaerFragment 用
        // @OnClick(R.id.menu2_fab_switch)//default is R.id.fab
        // 代替了这个fab
        //FloatingActionButton fab_switch = (FloatingActionButton)findViewById(R.id.switch_btn_editor);
        //fab_switch.setOnClickListener(new View.OnClickListener(){
        //    @Override
        //    public void onClick(View v){
        //        Intent intent = new Intent(MainActivity.this, TermuxActivity.class);
        //        startActivity(intent);
        //    }
        //});
    }



    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                int permission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
                }

                if(permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            setDefaultFragment(R.id.content_fragment_container);
        }
    }

    @Override
    public void initData() {

    }

    private void setDefaultFragment(@IdRes int fragmentId) {
        mCurrentFragment = new FolderManagerFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(fragmentId, mCurrentFragment)
                .commit();
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (onOptionsItemSelected(item)) {
            getDrawerLayout().closeDrawer(GravityCompat.START);
        }
        return false;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_helper:
                WebHelperActivity.startHelpActivity(this);
                return true;
            case R.id.menu_about:
                AboutActivity.startAboutActivity(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private long customTime = 0;
    @Override
    public void onBackPressed() {// 返回按钮
        if (getDrawerLayout().isDrawerOpen(GravityCompat.START)) {// 侧滑菜单打开，关闭菜单
            getDrawerLayout().closeDrawer(GravityCompat.START);
            return;
        }

        if (mCurrentFragment != null && mCurrentFragment.onBackPressed()) {// 如果Fragment有处理，则不据需执行
            return;
        }

        // 没有东西可以返回了，剩下软件退出逻辑
        if (Math.abs(customTime - System.currentTimeMillis()) < 2000) {
            finish();
        } else {// 提示用户退出
            customTime = System.currentTimeMillis();
            Toast.showShort(mContext, "再按一次退出软件");
        }
    }






}
