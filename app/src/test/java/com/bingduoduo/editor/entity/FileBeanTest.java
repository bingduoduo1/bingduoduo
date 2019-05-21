package com.bingduoduo.editor.entity;

import android.app.admin.SystemUpdatePolicy;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Spliterator;

import static org.junit.Assert.*;

public class FileBeanTest {
    FileBean fileBean = new FileBean();
    @Before
    public void setup()
    {
        fileBean.name = "test";
        fileBean.lastTime = new Date();
    }
    @Test
    public void toStringTest() {
        String answer = "FileBean{" +
            "name='" + "test" + '\'' +
            ", absPath='null" + '\'' +
            ", isDirectory=false" +
            ", lastTime=" + fileBean.lastTime.toString() +
            ", size=0"  +
            '}';
        String string = fileBean.toString();
        System.out.println(string);
        System.out.println(answer);
        Log.d("tostring", "toStringTest: " + string);
        assertTrue(answer.equals(string));
    }

    @Test
    public void hashCodeTest() {
        assertTrue("test".hashCode() == fileBean.hashCode());
    }

    @Test
    public void equals() {
        boolean isEqual = fileBean.equals(null);
        assertFalse(isEqual);
        isEqual = fileBean.equals(fileBean);
        assertTrue(isEqual);
        FileBean fileBean1 = new FileBean();
        fileBean1.name = "test";
        fileBean1.lastTime = new Date();
        isEqual = fileBean.equals(fileBean1);
        assertTrue(isEqual);
        isEqual = fileBean.equals("d");
        assertFalse(isEqual);
    }
}
