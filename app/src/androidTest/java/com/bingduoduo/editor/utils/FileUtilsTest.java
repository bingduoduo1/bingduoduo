package com.bingduoduo.editor.utils;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FileUtilsTest {

    File file;
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void deleteDir() {
        assertTrue(FileUtils.deleteDir(file));
    }

    @Test
    public void getFile() {
        //获取文件目录
    }


    @Test
    public void writeByte() {
    }

    @Test
    public void readFile() {
    }

    @Test
    public void readFileByLines() {
    }

    @Test
    public void copyFolder() {
    }

    @Test
    public void copyFolder1() {
    }

    @Test
    public void moveFolder() {
    }

    @Test
    public void moveFolder1() {
    }

    @Test
    public void deleteFile() {
    }

    @Test
    public void closeableClose() {
    }

    @Test
    public void uri2FilePath() {
    }
}
