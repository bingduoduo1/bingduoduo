package model.dictionary.application;

import android.drm.DrmStore;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.ActionType;
import model.dictionary.model.BaseAction;
import model.dictionary.model.BaseWord;
import model.dictionary.model.CustomWord;
import model.dictionary.model.ExecutePlaceType;
import model.dictionary.model.InputAction;
import model.dictionary.model.NatureLanguageType;

import static org.junit.Assert.*;

public class CommandDictionaryTest {

    CommandDictionary mCommandDictionary;

    @Before
    public void init() {
        mCommandDictionary = CommandDictionary.createDictionary();
    }

    @Test
    public void testLookUpAction() {
        String word = "man";
        BaseAction action = mCommandDictionary.lookUpAction(word);
        assertNotEquals(action, null);
        word = "sh help";
        action = mCommandDictionary.lookUpAction(word);
        assertNotEquals(action, null);
        word = "get help";
        action = mCommandDictionary.lookUpAction(word);
        assertNotEquals(action, null);
        word = "";
        action = mCommandDictionary.lookUpAction(word);
        assertEquals(action, null);
        BaseWord base_word = new CustomWord("魔鬼", NatureLanguageType.CHINESE);
        action = mCommandDictionary.lookUpAction(base_word);
        assertEquals(action, null);
        word = "测试";
        action = mCommandDictionary.lookUpAction(word);
        assertEquals(action, null);
    }

    @Test
    public void testInitDictWithPath() {
        String path = "init_cmd_dict.txt";
        try {
            mCommandDictionary.initDictionary(path);
        } catch (DictionaryException e) {
            assertTrue(e instanceof NotImplementedError);
        }
    }

}
