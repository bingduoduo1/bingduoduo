
package com.bingduoduo.editor.model;

import androidx.annotation.NonNull;

import com.bingduoduo.editor.entity.FileBean;

import java.io.File;
import java.util.Date;

import rx.Observable;

/**
 * File 文件相关数据处理
 */
public class FileModel implements IFileModel {
    private static FileModel instance = new FileModel();
    
    private FileModel() {
        
    }
    
    public static FileModel getInstance() {
        return instance;
    }
    
    @Override
    public Observable<FileBean> getFileBeanObservable(File file) {
        if (file == null)
        {
            return null;
        }
        return Observable.just(getFile(file, file.getName()));
    }
    
    @Override
    public Observable<File> getFileObservable(FileBean fileBean) {
        if (fileBean == null)
        {
            return null;
        }
        return Observable.just(getFile(fileBean));
    }
    
    public FileBean getFile(File file, String name) {
        FileBean bean = new FileBean();
        bean.absPath = file.getAbsolutePath();
        bean.isDirectory = file.isDirectory();
        bean.lastTime = new Date(file.lastModified());
        bean.name = name;
        
        if (file.isDirectory()) {
            bean.size = 0;
        } else {
            bean.size = file.length();
        }
        
        return bean;
    }
    
    @Override
    public File getFile(@NonNull FileBean fileBean) {
        return new File(fileBean.absPath);
    }
}
