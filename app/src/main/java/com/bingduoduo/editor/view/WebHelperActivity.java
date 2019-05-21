package com.bingduoduo.editor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebSettings;
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
        WebSettings webSettings = webView.getSettings();
        // 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        webSettings.setUseWideViewPort(true); // 将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        webSettings.setSupportZoom(true); // 支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); // 设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); // 隐藏原生的缩放控件

        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 关闭webview中缓存
        webSettings.setAllowFileAccess(true); // 设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");// 设置编码格式

        webView.loadUrl("https://github.com/bingduoduo1/bingduoduo/blob/master/README.md");
        // webView.loadUrl("file:///android_asset/helper.html");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

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
