package model.dictionary.application;

import model.dictionary.exception.DictionaryException;
import model.dictionary.model.BaseAction;
import model.dictionary.model.BaseWord;
import model.dictionary.model.CustomWord;
import model.dictionary.model.NatureLanguageType;

interface BaseDictionaryInterface {
    void initDictionary();

    void initDictionary(final String configPath) throws DictionaryException;

    BaseAction lookUpAction(BaseWord key);

    default BaseAction lookUpAction(String word) {
        NatureLanguageType languageType;
        BaseWord key;
        if (word.length() > 0) {
            char c = word.charAt(0);
            if ('a' <= c && c <= 'z' || 'A' <= c && c <= 'Z') {
                languageType = NatureLanguageType.ENGLISH;
            } else {
                languageType = NatureLanguageType.CHINESE;
            }
            key = new CustomWord(word, languageType);
        } else {
            languageType = NatureLanguageType.ENGLISH;
            key = new CustomWord(word, NatureLanguageType.ENGLISH);
        }

        return lookUpAction(key);
    }
}
