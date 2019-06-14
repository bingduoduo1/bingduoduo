
package com.bingduoduo.editor.view;

import android.Manifest;
//import android.content.Intent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;

import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.termux.R;
import com.bingduoduo.editor.base.BaseDrawerLayoutActivity;
import com.bingduoduo.editor.base.BaseFragment;
//import com.bingduoduo.editor.utils.Toast;
import com.termux.app.TermuxActivity;

/**
 * The type Main activity.
 */
public class MainActivity extends BaseDrawerLayoutActivity {
    private BaseFragment mCurrentFragment;
<<<<<<< HEAD
//    private int currentMenuId;
=======
    private int currentMenuId;
    private boolean isNightMode = false;
    private MenuItem mSwitchItem;
>>>>>>> zpj


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
=======
        if(savedInstanceState != null) {
            isNightMode = savedInstanceState.getBoolean("isNightMode");
            String tmp = savedInstanceState.getString("isNightModeString");

            Log.d("MainActivity", "onCreate: "+(isNightMode?"Night":"Day") + "String:"+tmp);
            if(isNightMode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

        }
>>>>>>> zpj
        this.requestPermissions();
    }


    public boolean getIsNightMode(){
        return isNightMode;
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

        Log.d("MainActivity", "onCreateOptionsMenu: =================");
        //getMenuInflater().inflate(R.menu.menu_main_drawer, menu);
        //mSwitchItem = menu.findItem(R.id.night_pattern_switch);
        //Switch switcher= (Switch) mSwitchItem.getActionView().findViewById(R.id.switcher);
        Toast.makeText(this, isNightMode?"Night":"Day",Toast.LENGTH_SHORT).show();
        Switch switcher = (Switch) findViewById(R.id.switcher);
        switcher.setChecked(isNightMode);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Toast.makeText(MainActivity.this, isChecked?"Yes":"No", Toast.LENGTH_SHORT).show();
                if (isChecked){
                    isNightMode = true;
                    Toast.makeText(MainActivity.this, isNightMode?"Yes":"No", Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", "onOptionsItemSelected: isChecked True");
                    System.out.println("in Ngiht Switch");
                    int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    getDelegate().setLocalNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_NO
                        ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                    // 同样需要调用recreate方法使之生效
                    recreate();
                    //Todo
                }else {
                    isNightMode = false;
                    //Toast.makeText(MainActivity.this, "ininini", Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", "onOptionsItemSelected: isChecked false");
                    int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    getDelegate().setLocalNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_NO
                        ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                    // 同样需要调用recreate方法使之生效
                    recreate();
                    //Todo
                }
            }
        });

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
            case R.id.night_pattern_switch:
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
            Toast.makeText(mContext,"再按一次退出软件",Toast.LENGTH_SHORT).show();
            //Toast.showShort(mContext, "再按一次退出软件");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //保存销毁之前的数据
        outState.putBoolean("isNightMode",isNightMode);
        outState.putString("isNightModeString",isNightMode?"Night":"Day");
        Toast.makeText(this, isNightMode?"Night":"Day", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "onSaveInstanceState: " +  (isNightMode?"Night":"Day"));
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //恢复数据
        isNightMode = savedInstanceState.getBoolean("isNightMode");

        //Switch switcher= (Switch) mSwitchItem.getActionView().findViewById(R.id.switcher);
        //switcher.setChecked(isNightMode);
        //Menu menu = (Menu)findViewById(R.menu.menu_main_drawer);
        //MenuItem switchItem = menu.findItem(R.id.action_open_close_nfc);
        //mSwitch = (Switch) switchItem.getActionView().findViewById(R.id.switchForActionBar);


        //Switch switcher = (Switch) findViewById(R.id.switcher);
        //switcher.setChecked(isNightMode);

        Toast.makeText(this, isNightMode?"onRestore Night":"on restore Day", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "onRestoreInstanceState: " +  (isNightMode?"Night":"Day"));
        //Switch switcher = (Switch)findViewById(R.id.switcher);
        //Toast.makeText(this,"get savedInsanceState",Toast.LENGTH_SHORT).show();
        //isNightMode = savedInstanceState.getBoolean("isNightMode");
        Log.d("MainActivity", "onCreate: "+(isNightMode?"Night":"Day"));
        //if(isNightMode){
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        //    switcher.setChecked(true);

        //}else{
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        //    switcher.setChecked(false);
        //}
    }


}
