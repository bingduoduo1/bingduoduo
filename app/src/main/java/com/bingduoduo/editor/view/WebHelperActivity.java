package com.bingduoduo.editor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.termux.R;
import com.bingduoduo.editor.base.BaseToolbarActivity;
import com.bingduoduo.editor.utils.SystemBarUtils;
import com.bingduoduo.editor.utils.SystemUtils;

import butterknife.Bind;

public class WebHelperActivity extends BaseToolbarActivity {
    @Bind(R.id.helper_web)
    WebView webView;
    @Override
    public int getLayoutId() {
        return R.layout.activity_helper;
    }
    public static void startHelpActivity(Context context) {
        Intent intent = new Intent(context, WebHelperActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        // webView = (WebView) findViewById(R.id.helper_web);
        webView.loadUrl("http://baidu.com");
        // webView.loadUrl("file:///android_asset/helper.html");
        initData();
    }

    @Override
    public void initData() {

    }

    @Override
    protected void initStatusBar() {
        SystemBarUtils.tintStatusBar(this, getResources().getColor(R.color.colorPrimary));
    }
}
