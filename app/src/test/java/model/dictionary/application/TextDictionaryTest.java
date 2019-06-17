package model.dictionary.application;

import org.junit.Test;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.BaseAction;
import model.dictionary.model.CustomWord;
import model.dictionary.model.NatureLanguageType;

import static org.junit.Assert.*;

public class TextDictionaryTest {
    TextDictionary textDictionary = TextDictionary.createDictionary();
    @Test
    public void initDictionary() {
        try {
            textDictionary.initDictionary("textdic.txt");
        }catch (NotImplementedError e)
        {
            assertTrue(e instanceof NotImplementedError);
        }catch (DictionaryException e)
        {
            assertTrue(e instanceof DictionaryException);
        }
    }

    @Test
    public void lookUpAction() {
        assertNull(textDictionary.lookUpAction(new CustomWord("systemout", NatureLanguageType.ENGLISH)));
        assertTrue(textDictionary.lookUpAction(new CustomWord("dot", NatureLanguageType.ENGLISH)) instanceof BaseAction);
        assertTrue(textDictionary.lookUpAction(new CustomWord("10", NatureLanguageType.ENGLISH)) instanceof BaseAction);
    }
}
