package model.dictionary.application;

import org.junit.Test;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.BaseAction;
import model.dictionary.model.CustomWord;
import model.dictionary.model.NatureLanguageType;

import static org.junit.Assert.*;

public class PythonDictionaryTest {
    PythonDictionary pythonDictionary = PythonDictionary.createDictionary();
    @Test
    public void lookUpAction() {
        assertTrue(pythonDictionary.lookUpAction(new CustomWord("import", NatureLanguageType.ENGLISH)) instanceof BaseAction);
    }
    @Test
    public void TestInit()
    {
        try {
            pythonDictionary.initDictionary("python.txt");
        }catch (NotImplementedError e)
        {
            assertTrue(e instanceof NotImplementedError);
        }catch (DictionaryException e)
        {
            assertTrue(e instanceof DictionaryException);
        }
    }
}
