package model.dictionary.model;

import org.junit.Test;

import static org.junit.Assert.*;
//    GENERAL,
//    EDITOR,
//    SHELL
public class ExecutePlaceTypeTest {
    @Test
    public void testExePlaceTypeTest()
    {
        ExecutePlaceType typeGeneral = ExecutePlaceType.GENERAL;
        ExecutePlaceType typeEditor = ExecutePlaceType.EDITOR;
        ExecutePlaceType typeShell = ExecutePlaceType.SHELL;
        assertFalse(typeEditor.equals(typeGeneral));
        assertFalse(typeGeneral.equals(typeShell));
        ExecutePlaceType typeShell2 = ExecutePlaceType.SHELL;
        assertTrue(typeShell2.equals(typeShell));
    }

}
