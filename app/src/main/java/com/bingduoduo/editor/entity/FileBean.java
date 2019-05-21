package com.bingduoduo.editor.entity;

import java.util.Date;

/**
 * 文件实体
 */
public class FileBean {
    /**
     * 文件名字
     */
    public String name;
    /**
     * 绝对路径
     */
    public String absPath;
    /**
     * 是否文件夹
     */
    public boolean isDirectory;
    /**
     * 最后修改时间
     */
    public Date lastTime;

    /**
     * 文件大小
     */
    public long size;

    public boolean isSelect = false;

//    public FileBean(String name, String absPath, boolean isDirectory, Date lastTime, long size) {
//        this.name = name;
//        this.absPath = absPath;
//        this.isDirectory = isDirectory;
//        this.lastTime = lastTime;
//        this.size = size;
//    }

    public FileBean() {
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "name='" + name + '\'' +
                ", absPath='" + absPath + '\'' +
                ", isDirectory=" + isDirectory +
                ", lastTime=" + lastTime +
                ", size=" + size +
                '}';
    }

    @Override
    public int hashCode() {// 重写hashCode方法
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {// 重写equals方法
        if (obj == null) return false;
        if (this == obj)
            return true;

        if (obj instanceof FileBean) {
            FileBean p = (FileBean) obj;
            return name.equals(p.name) && lastTime.equals(p.lastTime);
        }
        return false;
    }
}
