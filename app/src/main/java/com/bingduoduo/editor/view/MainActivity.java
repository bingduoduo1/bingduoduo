
package com.bingduoduo.editor.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LongDef;
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
    private int currentMenuId;
    private GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FloatingActionButton fab_switch = (FloatingActionButton)findViewById(R.id.switch_btn_editor);
        fab_switch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, TermuxActivity.class);
                startActivity(intent);
            }
        });
        mGestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {// e1: 第一次按下的位置   e2   当手离开屏幕 时的位置  velocityX  沿x 轴的速度  velocityY： 沿Y轴方向的速度
                // 判断竖直方向移动的大小
                if(Math.abs(e1.getRawY() - e2.getRawY())>100){
                    //Toast.makeText(getApplicationContext(), "动作不合法", 0).show();
                    return true;
                }
                if(Math.abs(velocityX) < 150){
                    //Toast.makeText(getApplicationContext(), "移动的太慢", 0).show();
                    return true;
                }

                if((e1.getRawX() - e2.getRawX()) > 200){  // 表示 向右滑动表示下一页
                    // 显示下一页
                    Log.d("MainActivity", "onFling: 右$$$$$$$$$$$$$$$$$$$");
                    Intent intent = new Intent(MainActivity.this, TermuxActivity.class);
                    startActivity(intent);
                    return true;
                }

                if((e2.getRawX() - e1.getRawX()) > 200){  // 向左滑动 表示 上一页
                    // 显示上一页
                    Log.d("MainActivity", "onFling: 左$$$$$$$$$$$$$$$$$$$");
                    return true;// 消费掉当前事件  不让当前事件继续向下传递
                }
                return true;
            }
        });


    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //2.让手势识别器生效
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
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
        int id = item.getItemId();
        if (id == R.id.localhost) {//|| id == R.id.other
            if (id == currentMenuId) {
                return false;
            }
            currentMenuId = id;
            getDrawerLayout().closeDrawer(GravityCompat.START);
            return true;
        }

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
