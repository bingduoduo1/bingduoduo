

package com.bingduoduo.editor.presenter;

import androidx.annotation.NonNull;

import com.bingduoduo.editor.base.mvp.IMvpView;

/**
 * 回调方法抽象，每个界面的回调都不同，所以提取出来
 */
public interface IEditorActivityView extends IMvpView {
    int CALL_GET_FILES = 1;


    void onNameChange(@NonNull String name);

}
