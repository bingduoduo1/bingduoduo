package model.dictionary.model;

import org.junit.Before;
import org.junit.Test;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotMatchActionTypeExpection;

import static org.junit.Assert.*;

public class InputActionTest {
    public InputAction action1;
    public InputAction action2;
    public InputAction action3;
    @Before
    public void setUp() throws Exception {
        action1 = new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "cd");
        try
        {
            action2 = new InputAction(ActionType.COMMAND, ExecutePlaceType.SHELL, "cd");
        }catch (NotMatchActionTypeExpection e) {
            assertTrue(e instanceof NotMatchActionTypeExpection);
        }
        try
        {
            action3 = new InputAction(ActionType.INPUT,ExecutePlaceType.EDITOR,"fsdf");
        }catch (DictionaryException e1)
        {
            assertTrue(e1 instanceof DictionaryException);
        }
    }

    @Test
    public void getContentTest(){
        String s = action1.getContent();
        assertTrue(s.equals("cd"));
    }
}
