package com.bingduoduo.editor.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CheckTest {

    public static boolean isNull(Object o) {
        return o == null;
    }


    @Test
    public void isEmpty() {
        Object nums[] = new Integer[50];
        boolean isEmpty = Check.isEmpty(nums);
        assertFalse(isEmpty);
        String str = new String("\n\t");
        isEmpty = Check.isEmpty(str);
        assertTrue(isEmpty);
        str = new String();
        isEmpty = Check.isEmpty(str);
        assertTrue(isEmpty);
        str = new String("\n\t5");
        isEmpty = Check.isEmpty(str);
        assertFalse(isEmpty);
        LinkedList<Integer> integers = new LinkedList<>();
        isEmpty = Check.isEmpty(integers);
        assertTrue(isEmpty);
        HashMap<Integer,String> map = new HashMap<>();
        isEmpty = Check.isEmpty(map);
        assertTrue(isEmpty);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(1);
        isEmpty = Check.isEmpty(stringBuffer);
        assertFalse(isEmpty);
    }
    @Test
    public void isNull() {
        boolean isNull = Check.isNull(null);
        assertTrue(isNull);
        int a = 10;
        isNull =Check.isNull(a);
        assertFalse(isNull);
    }

    @Test
    public void checkNull() {
        try{
            Check.CheckNull(null, "Null object");
        }catch (IllegalStateException e)
        {
            assertTrue(e instanceof IllegalStateException);
        }
    }
}
