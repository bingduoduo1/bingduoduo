
package com.bingduoduo.editor.model;

import com.bingduoduo.editor.entity.FileBean;

import java.io.File;

import rx.Observable;

/**
 * FileMode抽象
 */
public interface IFileModel {
    /**
     * 将文件类型转换
     * Gets file bean.
     *
     * @param file the file
     * @return the file bean
     */
    Observable<FileBean> getFileBeanObservable(File file);
    
    /**
     * 将文件类型转换
     * Gets file observable.
     *
     * @param fileBean the file bean
     * @return the file observable
     */
    Observable<File> getFileObservable(FileBean fileBean);
    
    /**
     * 将文件类型转换为FileBean
     * Gets file bean.
     *
     * @param file the file
     * @param name the name
     * @return the file bean
     */
    FileBean getFile(File file, String name);
    
    /**
     * 将FileBean类型转换为File
     * Gets file bean.
     *
     * @param fileBean the file bean
     * @return the file
     */
    File getFile(FileBean fileBean);
}
