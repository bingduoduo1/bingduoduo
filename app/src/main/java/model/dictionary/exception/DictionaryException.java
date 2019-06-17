package model.dictionary.exception;

import model.config.ExceptionLevel;
import model.config.GlobalException;

public class DictionaryException extends GlobalException {
    public DictionaryException() {
        super();
    }

    public DictionaryException(String content) {
        super(content);
    }

    @Override
    protected void initExceptionLevel() {
        mexceptionlevel = ExceptionLevel.Dictionary;
    }
}
