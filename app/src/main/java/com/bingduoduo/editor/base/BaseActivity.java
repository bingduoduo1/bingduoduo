package com.bingduoduo.editor.base;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;

import com.bingduoduo.editor.utils.SystemBarUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import butterknife.ButterKnife;

/**
 * 原始Activity封装
 */
public abstract class BaseActivity extends AppCompatActivity
    implements BaseViewInterface, WaitDialogInterface {

    protected BaseApplication application;
    protected LayoutInflater inflater;
    protected Context mcontext;

    public BaseApplication getBaseApplication() {
        return application;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public Context getMcontext() {
        return mcontext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        inflater = getLayoutInflater();
        init();
        initStatusBar();
        onCreateAfter(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);//解绑定
        super.onDestroy();
    }

    private boolean isFirstFocused = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirstFocused && hasFocus) {
            isFirstFocused = false;
            initData();//此时界面渲染完毕,可以用来初始化数据等
        }
    }

    protected void init() {
        mcontext = getApplicationContext();
        application = (BaseApplication) getApplication();
    }

    protected void initStatusBar() {
        SystemBarUtils.tintStatusBar(this, 0);
    }

    @Override
    public void hideWaitDialog() {
        if (mwait != null && mwait.isShowing())
        {
            mwait.dismiss();
        }
    }

    private KProgressHUD mwait;

    @Override
    public KProgressHUD showWaitDialog(String message, boolean canBack) {
        if (mwait == null)
        {
            mwait = KProgressHUD.create(mcontext)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("请稍等")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        }
        else if (mwait.isShowing())
        {
            mwait.dismiss();
        }
        mwait.setCancellable(canBack)
            .setDetailsLabel(message)
            .show();

        return mwait;
    }
}
