
package com.bingduoduo.editor.presenter;

import com.bingduoduo.editor.base.mvp.IMvpView;
import com.bingduoduo.editor.entity.FileBean;

import java.util.List;

/**
 * 回调方法抽象，每个界面的回调都不同，所以提取出来
 */
public interface IFolderManagerView extends IMvpView {
    // 没有参数的回调一般用成功回调即可
    int CALL_GET_FILES = 1;
    int CALL_CREATE_FOLDER = 2;
    int CALL_COPY_PASTE = 3;
    int CALL_CUT_PASTE = 4;
    int CALL_PASTE_MODE = 5;
    int CALL_CLOSE_PASTE_MODE = 6;
    int CALL_EDIT_MODE = 7;
    int CALL_CLOSE_EDIT_MODE = 8;
    int CALL_REMOVE_TAB = 9;
    int CALL_OTHER = 10;
    
    /**
     * 获取文件列表成功
     *
     * @param files the files
     */
    void getFileListSuccess(List<FileBean> files);
    
    /**
     * 增加tab
     * Add tab.
     *
     * @param title the title
     */
    void addTab(String title);
    
    /**
     * Update position.
     *
     * @param position the position
     * @param bean     the bean
     */
    void updatePosition(int position, FileBean bean);
    
    void addFilePosition(int position, FileBean bean);
    
}
