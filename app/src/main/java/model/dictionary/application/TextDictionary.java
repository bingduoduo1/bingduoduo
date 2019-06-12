package model.dictionary.application;

import java.util.HashMap;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.helper.GlobalHelper;
import model.dictionary.model.ActionType;
import model.dictionary.model.BaseAction;
import model.dictionary.model.BaseWord;
import model.dictionary.model.CustomWord;
import model.dictionary.model.ExecutePlaceType;
import model.dictionary.model.InputAction;
import model.dictionary.model.NatureLanguageType;

public class TextDictionary implements BaseDictionaryInterface {
    private HashMap<BaseWord, BaseAction> mDictionary;
    private static TextDictionary mDictReference = new TextDictionary();

    private TextDictionary() {
        mDictionary = new HashMap<BaseWord, BaseAction>();
        initDictionary();
    }
    public static TextDictionary createDictionary() {
        return mDictReference;
    }
    @Override
    public void initDictionary() {
        try {
            for (char item='a'; item<='z'; ++item) {
                String str = String.valueOf(item);
                mDictionary.put(new CustomWord(str, NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, str));
            }
            mDictionary.put(new CustomWord("left bracket", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "("));
            mDictionary.put(new CustomWord("right bracket", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, ")"));
            mDictionary.put(new CustomWord("左括号", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "("));
            mDictionary.put(new CustomWord("右括号", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, ")"));
            mDictionary.put(new CustomWord("括号", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "()"));
            mDictionary.put(new CustomWord("colon", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, ":"));
            mDictionary.put(new CustomWord("冒号", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, ":"));
            mDictionary.put(new CustomWord("dot", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "."));
            mDictionary.put(new CustomWord("点", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "."));
            mDictionary.put(new CustomWord("comma", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, ","));
            mDictionary.put(new CustomWord("逗号", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, ","));
            mDictionary.put(new CustomWord("杠", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "-"));
            mDictionary.put(new CustomWord("下划线", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "_"));
            mDictionary.put(new CustomWord("space", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, " "));
            mDictionary.put(new CustomWord("空格", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, " "));
            mDictionary.put(new CustomWord("tab", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "    "));  // 默认用4个空格代替
            mDictionary.put(new CustomWord("换行", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "\n"));
            mDictionary.put(new CustomWord("回车", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "\n"));  // 不区分

        } catch (DictionaryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initDictionary(String config_path) throws DictionaryException{
        throw new NotImplementedError();
    }

    public BaseAction lookUpAction(BaseWord key) {
        if (mDictionary.containsKey((CustomWord)key)) {
            return mDictionary.get(key);
        } else if (GlobalHelper.isInteger(key.getRawData())){
            try {
                return new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, key.getRawData());
            } catch (DictionaryException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
