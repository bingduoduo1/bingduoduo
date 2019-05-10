package com.bingduoduo.editor.base.mvp;
/**
 * Presenter顶层抽象
 * The type Base presenter.
 */
public interface IPresenter<V extends IMvpView> {
    /**
     * 建立关系
     */
    void attachView(V mvpView);
    /**
     * 分离界面和Presenter
     */
    void detachView();

}