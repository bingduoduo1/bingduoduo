package model.dictionary.helper;

import org.junit.Test;

import static org.junit.Assert.*;

public class GlobalHelperTest {

    @Test
    public void isAlpha() {
        boolean ret1 = GlobalHelper.isAlpha("da");
        boolean ret2 = GlobalHelper.isAlpha("G");
        assertFalse(ret1);
        assertTrue(ret2);
    }

    @Test
    public void isInteger() {
        boolean ret1 = GlobalHelper.isInteger("12");
        assertTrue(ret1);
        try{
            boolean ret2 = GlobalHelper.isInteger("1l");
        }catch (NumberFormatException e)
        {
            assertTrue(e instanceof NumberFormatException);
        }
    }
}
