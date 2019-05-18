package model.dictionary.helper;

import org.junit.Test;

import model.dictionary.model.InputAction;

import static org.junit.Assert.*;

public class InputActionHelperTest {

    @Test
    public void inputActionContentCheck() {
        boolean ret1 = InputActionHelper.inputActionContentCheck("xxx");
        assertFalse(ret1);
        boolean ret2 = InputActionHelper.inputActionContentCheck("man ssh");
        assertTrue(ret2);
    }
}
