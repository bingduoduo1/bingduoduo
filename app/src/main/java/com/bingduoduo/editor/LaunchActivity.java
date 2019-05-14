package com.bingduoduo.editor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.termux.R;

/**
 * 这个原本想用ViewPager来进行活动的切换
 * [弃用]
 */
public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }
}
