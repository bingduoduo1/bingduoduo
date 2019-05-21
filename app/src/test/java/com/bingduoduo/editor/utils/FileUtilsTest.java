package com.bingduoduo.editor.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.*;
@RunWith(MockitoJUnitRunner.class)
public class FileUtilsTest {
    File file;
    @Mock
    Context mContext;
    @Before
    public void setUp() throws Exception {
        file = new File("C:\\data\\test");

    }

    @Test
    public void deleteDir() {
        assertTrue(FileUtils.deleteDir(file));
    }

    @Test
    public void getFile() {
        //获取文件目录

        when(mContext.getFilesDir()).thenReturn(new File("C:\\data\\test"));
        System.out.println(FileUtils.getFile(mContext));
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
