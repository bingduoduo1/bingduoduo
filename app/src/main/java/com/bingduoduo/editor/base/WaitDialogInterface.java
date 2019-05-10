

package com.bingduoduo.editor.base;


import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * 等待框的接口
 */
public interface WaitDialogInterface {

    /**
     * 隐藏对话框
     */
    void hideWaitDialog();


    /**
     * 显示等待的对话框
     *
     * @param text
     * @return
     */
    KProgressHUD showWaitDialog(String text, boolean canBack);

}
