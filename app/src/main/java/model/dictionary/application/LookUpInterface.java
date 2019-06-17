package model.dictionary.application;

import model.dictionary.exception.DictionaryException;

public interface LookUpInterface {
    void exactLookUpWord(final String word, StringBuffer action) throws DictionaryException;
    
    void fuzzyLookUpWord(final String word, StringBuffer action) throws DictionaryException;
    // action type should be
    // String Buffer
}
