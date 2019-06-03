package model.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class GlobalExceptionTest {

    @Test
    public void getExceptionLevel() {
        GlobalException globalException = new GlobalException("TEST");
        assertTrue("GLOBAL".equals(globalException.getExceptionLevel()));
        globalException = new GlobalException();
        assertTrue("GLOBAL".equals(globalException.getExceptionLevel()));

    }
}
