
package com.bingduoduo.editor.presenter;

import androidx.annotation.NonNull;

import com.bingduoduo.editor.base.mvp.IMvpView;

/**
 * 回调方法抽象，每个界面的回调都不同，所以提取出来
 */
public interface IEditorFragmentView extends IMvpView {
    // 没有参数的回调一般用成功回调即可
    int CALL_LOAOD_FILE = 1;
    int CALL_NO_SAVE = 2;
    int CALL_SAVE = 3;
    int CALL_EXIT = 4;
    
    // 文件读取成功
    void onReadSuccess(@NonNull String name, @NonNull String content);
    
}
