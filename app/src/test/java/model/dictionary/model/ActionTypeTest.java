package model.dictionary.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class ActionTypeTest {
    @Test
    public void TestEqual()
    {
        ActionType typeComand = ActionType.COMMAND;
        ActionType typeInput = ActionType.INPUT;
        assertFalse(typeComand.equals(typeInput));
        ActionType typeCommand2 = ActionType.COMMAND;
        assertTrue(typeCommand2.equals(typeComand));
    }
}
