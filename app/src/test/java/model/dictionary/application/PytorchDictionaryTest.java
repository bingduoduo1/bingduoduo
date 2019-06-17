package model.dictionary.application;

import org.junit.Test;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.BaseAction;
import model.dictionary.model.CustomWord;
import model.dictionary.model.NatureLanguageType;

import static org.junit.Assert.*;

public class PytorchDictionaryTest {
    PytorchDictionary pytorchDictionary = PytorchDictionary.createDictionary();
    @Test
    public void lookUpAction() {
        assertTrue(pytorchDictionary.lookUpAction(new CustomWord("SGD", NatureLanguageType.ENGLISH)) instanceof BaseAction);
    }
    @Test
    public void TestInit()
    {
        try {
            pytorchDictionary.initDictionary("torch.txt");
        }catch (NotImplementedError e)
        {
            assertTrue(e instanceof NotImplementedError);
        }catch (DictionaryException e)
        {
            assertTrue(e instanceof DictionaryException);
        }
    }
}
