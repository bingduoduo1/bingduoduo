

package com.bingduoduo.editor.base;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;
import android.view.LayoutInflater;

import com.bingduoduo.editor.utils.SystemBarUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import butterknife.ButterKnife;

/**
 * 原始Activity封装
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseViewInterface, WaitDialogInterface {

    protected BaseApplication application;
    protected LayoutInflater inflater;
    protected Context mContext;


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
        mContext = getApplicationContext();
        application = (BaseApplication) getApplication();
    }

    protected void initStatusBar() {
        SystemBarUtils.tintStatusBar(this, 0);
    }

    @Override
    public void hideWaitDialog() {
        if (mWait != null && mWait.isShowing())
            mWait.dismiss();
    }

    private KProgressHUD mWait;

    @Override
    public KProgressHUD showWaitDialog(String message, boolean canBack) {
        if (mWait == null)
            mWait = KProgressHUD.create(mContext)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("请稍等")
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f);
        else if (mWait.isShowing()) mWait.dismiss();
        mWait.setCancellable(canBack)
                .setDetailsLabel(message)
                .show();
        return mWait;
    }



    /**
     * 修复长按文本启动系统的复制粘贴ActionMode的状态栏颜色
     */
//    private void fixActionModeCallback(AppCompatActivity activity, ActionMode mode) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//            return;
//
//        if (!(mode instanceof StandaloneActionMode))
//            return;
//
//        try {
//            final Field mCallbackField = mode.getClass().getDeclaredField("mCallback");
//            mCallbackField.setAccessible(true);
//            final Object mCallback = mCallbackField.get(mode);
//
//            final Field mWrappedField = mCallback.getClass().getDeclaredField("mWrapped");
//            mWrappedField.setAccessible(true);
//            final ActionMode.Callback mWrapped = (ActionMode.Callback) mWrappedField.get(mCallback);
//
//            final Field mDelegateField = AppCompatActivity.class.getDeclaredField("mDelegate");
//            mDelegateField.setAccessible(true);
//            final Object mDelegate = mDelegateField.get(activity);
//
//            mCallbackField.set(mode, new ActionMode.Callback() {
//
//                @Override
//                public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
//                    return mWrapped.onCreateActionMode(mode, menu);
//                }
//
//                @Override
//                public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
//                    return mWrapped.onPrepareActionMode(mode, menu);
//                }
//
//                @Override
//                public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
//                    return mWrapped.onActionItemClicked(mode, item);
//                }
//
//                @Override
//                public void onDestroyActionMode(final android.support.v7.view.ActionMode mode) {
//                    Class mDelegateClass = mDelegate.getClass().getSuperclass();
//                    Window mWindow = null;
//                    PopupWindow mActionModePopup = null;
//                    Runnable mShowActionModePopup = null;
//                    ActionBarContextView mActionModeView = null;
//                    AppCompatCallback mAppCompatCallback = null;
//                    ViewPropertyAnimatorCompat mFadeAnim = null;
//                    android.support.v7.view.ActionMode mActionMode = null;
//
//                    Field mFadeAnimField = null;
//                    Field mActionModeField = null;
//
//                    while (mDelegateClass != null) {
//                        try {
//                            if (TextUtils.equals("AppCompatDelegateImplV7", mDelegateClass.getSimpleName())) {
//                                Field mActionModePopupField = mDelegateClass.getDeclaredField("mActionModePopup");
//                                mActionModePopupField.setAccessible(true);
//                                mActionModePopup = (PopupWindow) mActionModePopupField.get(mDelegate);
//
//                                Field mShowActionModePopupField = mDelegateClass.getDeclaredField("mShowActionModePopup");
//                                mShowActionModePopupField.setAccessible(true);
//                                mShowActionModePopup = (Runnable) mShowActionModePopupField.get(mDelegate);
//
//                                Field mActionModeViewField = mDelegateClass.getDeclaredField("mActionModeView");
//                                mActionModeViewField.setAccessible(true);
//                                mActionModeView = (ActionBarContextView) mActionModeViewField.get(mDelegate);
//
//                                mFadeAnimField = mDelegateClass.getDeclaredField("mFadeAnim");
//                                mFadeAnimField.setAccessible(true);
//                                mFadeAnim = (ViewPropertyAnimatorCompat) mFadeAnimField.get(mDelegate);
//
//                                mActionModeField = mDelegateClass.getDeclaredField("mActionMode");
//                                mActionModeField.setAccessible(true);
//                                mActionMode = (android.support.v7.view.ActionMode) mActionModeField.get(mDelegate);
//
//                            } else if (TextUtils.equals("AppCompatDelegateImplBase", mDelegateClass.getSimpleName())) {
//                                Field mAppCompatCallbackField = mDelegateClass.getDeclaredField("mAppCompatCallback");
//                                mAppCompatCallbackField.setAccessible(true);
//                                mAppCompatCallback = (AppCompatCallback) mAppCompatCallbackField.get(mDelegate);
//
//                                Field mWindowField = mDelegateClass.getDeclaredField("mWindow");
//                                mWindowField.setAccessible(true);
//                                mWindow = (Window) mWindowField.get(mDelegate);
//                            }
//
//                            mDelegateClass = mDelegateClass.getSuperclass();
//                        } catch (NoSuchFieldException e) {
//                            e.printStackTrace();
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    if (mActionModePopup != null) {
//                        mWindow.getDecorView().removeCallbacks(mShowActionModePopup);
//                    }
//
//                    if (mActionModeView != null) {
//                        if (mFadeAnim != null) {
//                            mFadeAnim.cancel();
//                        }
//
//                        mFadeAnim = ViewCompat.animate(mActionModeView).alpha(0.0F);
//
//                        final PopupWindow mActionModePopupFinal = mActionModePopup;
//                        final ActionBarContextView mActionModeViewFinal = mActionModeView;
//                        final ViewPropertyAnimatorCompat mFadeAnimFinal = mFadeAnim;
//                        final AppCompatCallback mAppCompatCallbackFinal = mAppCompatCallback;
//                        final android.support.v7.view.ActionMode mActionModeFinal = mActionMode;
//                        final Field mFadeAnimFieldFinal = mFadeAnimField;
//                        final Field mActionModeFieldFinal = mActionModeField;
//
//                        mFadeAnim.setListener(new ViewPropertyAnimatorListenerAdapter() {
//                            public void onAnimationEnd(View view) {
//                                mActionModeViewFinal.setVisibility(View.GONE);
//                                if (mActionModePopupFinal != null) {
//                                    mActionModePopupFinal.dismiss();
//                                } else if (mActionModeViewFinal.getParent() instanceof View) {
//                                    ViewCompat.requestApplyInsets((View) mActionModeViewFinal.getParent());
//                                }
//
//                                mActionModeViewFinal.removeAllViews();
//                                mFadeAnimFinal.setListener((ViewPropertyAnimatorListener) null);
//
//                                try {
//                                    if (mFadeAnimFieldFinal != null) {
//                                        mFadeAnimFieldFinal.set(mDelegate, null);
//                                    }
//                                } catch (IllegalAccessException e) {
//                                    e.printStackTrace();
//                                }
//
//                                mWrapped.onDestroyActionMode(mode);
//
//                                if (mAppCompatCallbackFinal != null) {
//                                    mAppCompatCallbackFinal.onSupportActionModeFinished(mActionModeFinal);
//                                }
//
//                                try {
//                                    if (mActionModeFieldFinal != null) {
//                                        mActionModeFieldFinal.set(mDelegate, null);
//                                    }
//                                } catch (IllegalAccessException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }
}
