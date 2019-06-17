package model.dictionary.application;

import java.util.HashMap;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.ActionType;
import model.dictionary.model.BaseAction;
import model.dictionary.model.BaseWord;
import model.dictionary.model.ExecutePlaceType;
import model.dictionary.model.InputAction;
import model.dictionary.model.CustomWord;
import model.dictionary.model.NatureLanguageType;

public class PythonDictionary implements BaseDictionaryInterface {
    private HashMap<BaseWord, BaseAction> mdictionary;
    private static PythonDictionary mDictReference = new PythonDictionary();
    
    private PythonDictionary() {
        mdictionary = new HashMap<BaseWord, BaseAction>();
        initDictionary();
    }
    
    public static PythonDictionary createDictionary() {
        return mDictReference;
    }
    
    @Override
    public void initDictionary() {
        try {
            mdictionary.put(new CustomWord("import", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "import "));
            mdictionary.put(new CustomWord("math", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "math"));
            mdictionary.put(new CustomWord("sin", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "sin"));
            mdictionary.put(new CustomWord("cos", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "cos"));
            mdictionary.put(new CustomWord("exit", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "exit"));
            mdictionary.put(new CustomWord("from", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "from"));
            mdictionary.put(new CustomWord("define", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "def"));
            mdictionary.put(new CustomWord("with", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "with"));
            mdictionary.put(new CustomWord("as", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "as"));
            mdictionary.put(new CustomWord("if", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "if"));
            mdictionary.put(new CustomWord("else", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "else"));
            mdictionary.put(new CustomWord("else if", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "elif"));
            mdictionary.put(new CustomWord("and", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "and"));
            mdictionary.put(new CustomWord("与", NatureLanguageType.CHINESE),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "and"));
            mdictionary.put(new CustomWord("or", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "or"));
            mdictionary.put(new CustomWord("或", NatureLanguageType.CHINESE),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "or"));
            mdictionary.put(new CustomWord("not", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "not"));
            mdictionary.put(new CustomWord("for", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "for"));
            mdictionary.put(new CustomWord("in", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "in"));
            mdictionary.put(new CustomWord("is", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "is"));
            mdictionary.put(new CustomWord("numpy", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "numpy"));
            mdictionary.put(new CustomWord("num py", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "numpy"));
            mdictionary.put(new CustomWord("function", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "function"));
            mdictionary.put(new CustomWord("test", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "test"));
            
            // 中文
            mdictionary.put(new CustomWord("男排", NatureLanguageType.CHINESE),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "numpy"));
            
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void initDictionary(String configPath) throws DictionaryException {
        throw new NotImplementedError();
    }
    
    public BaseAction lookUpAction(BaseWord key) {
        if (mdictionary.containsKey(key)) {
            return mdictionary.get(key);
        } else {
            return null;
        }
    }
}
