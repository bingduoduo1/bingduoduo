
package com.bingduoduo.editor.base.mvp;

import com.bingduoduo.editor.model.DataManager;

import rx.subscriptions.CompositeSubscription;

/**
 * presenter类的父类，提供Presenter接口的实现
 */
public class BasePresenter<T extends IMvpView> implements IPresenter<T> {

    private T mvpView;

    protected DataManager dataManager;

    /**
     * 用来保存 每个Presenter的所有订阅（请求），onDestory（detachView）或者subscribe的onCompleted中取消订阅
     * 自己维护生命周期，防止内存泄露
     */
    public CompositeSubscription compositeSubscription;

    @Override
    public void attachView(T mvpView) {
        this.mvpView = mvpView;
        this.compositeSubscription = new CompositeSubscription();
        this.dataManager = DataManager.getInstance();
    }

    @Override
    public void detachView() {
        this.compositeSubscription.unsubscribe();
        this.compositeSubscription = null;
        this.mvpView = null;
    }

    public T getMvpView() {
        return mvpView;
    }

    protected void callFailure(int errorCode, String message, int flag) {
        if (getMvpView() == null) {
            return;
        }
        getMvpView().onFailure(errorCode, message, flag);
    }

    protected void callShowProgress(String message, boolean canBack, int flag) {
        if (getMvpView() == null) {
            return;
        }
        getMvpView().showWait(message, canBack, flag);
    }

    protected void callHideProgress(int flag) {
        if (getMvpView() == null) {
            return;
        }
        getMvpView().hideWait(flag);
    }

    protected void callOtherSuccess(int flag) {
        if (getMvpView() == null) {
            return;
        }
        getMvpView().otherSuccess(flag);
    }
}
