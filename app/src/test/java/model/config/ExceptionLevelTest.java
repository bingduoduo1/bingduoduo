package model.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExceptionLevelTest {

    @Test
    public void toStringTest() {
        assertTrue("Dictionary".equals(ExceptionLevel.toString(ExceptionLevel.Dictionary)));
        assertTrue("GLOBAL".equals(ExceptionLevel.toString(ExceptionLevel.GLOBAL)));
//        ExceptionLevel level = null;
//        assertTrue("WTF".equals(ExceptionLevel.toString(level)));
    }
}
