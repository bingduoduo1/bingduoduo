package model.dictionary.application;

import org.junit.Test;

import java.io.StringBufferInputStream;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;

import static org.junit.Assert.*;

public class GlobalDictionaryTest {
    GlobalDictionary mDictionary = GlobalDictionary.createDictionary();

    @Test
    public void testFuzzyLookUpWord() {
        try {
            mDictionary.fuzzyLookUpWord(null ,null);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }

    @Test
    public void testFuzzyLookUpWord1() {
        String word = new String("优化算法等于adam");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }
    @Test
    public void testFuzzyLookUpWord2() {
        String word = new String("优化算法等于sjd");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }
    @Test
    public void testFuzzyLookUpWord3() {
        String word = new String("优化算法等于sgd");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }@Test
    public void testFuzzyLookUpWord4() {
        String word = new String("优化算法等于ll");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }
    @Test
    public void testFuzzyLookUpWord5() {
        String word = new String("学习率等于0.01");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word ,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }
    @Test
    public void testFuzzyLookUpWord6() {
        String word = new String("学习率等于k01");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word ,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), NotImplementedError.class);
        }
    }
    @Test
    public void testFuzzyLookUpWord7() {
        String word = new String("率等于0.01");
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.fuzzyLookUpWord(word ,action);
        } catch (DictionaryException e) {
            assertEquals(e.getClass(), DictionaryException.class);
        }
    }



    @Test
    public void testExactLookUpWord(){
        String word = "ls";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(word, action);
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        assertEquals(action.toString(), "ls");
    }
    @Test
    public void testExactLookUpWord1(){
        String pythonWord = "test";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(pythonWord, action);
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        assertEquals(action.toString(), "test");
    }
    @Test
    public void testExactLookUpWord2(){
        String textWord = "dot";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(textWord, action);
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        assertEquals(action.toString(), ".");
    }
    @Test
    public void testExactLookUpWord4(){
        String torchWord = "SGD";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(torchWord, action);
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        assertEquals(action.toString(), "SGD");
    }
    @Test
    public void testExactLookUpWord3(){
        String torchWord = "check";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(torchWord, action);
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        assertEquals(action.toString(), "check");
    }
    @Test
    public void testExactLookUpWord5(){
        String commandWord = "bash";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(commandWord, action);
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        assertEquals(action.toString(), "bash");
    }
    @Test
    public void testExactLookUpWord6(){
        String torchWord = "xxxx";
        StringBuffer action = new StringBuffer();
        try {
            mDictionary.exactLookUpWord(torchWord, action);
        } catch (DictionaryException e) {
            assertTrue(e instanceof DictionaryException);
        }
//        assertEquals(action.toString(), "check");
    }
}
