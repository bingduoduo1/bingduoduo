package model.config;

import static model.config.ExceptionLevel.GLOBAL;

public class GlobalException extends Exception {
    protected ExceptionLevel mexceptionlevel;

    public GlobalException() {
        initExceptionLevel();
    }

    public GlobalException(String content) {
        super(content);
        initExceptionLevel();
    }

    public String getExceptionLevel() {
        return ExceptionLevel.toString(mexceptionlevel);
    }

    protected void initExceptionLevel() {
        mexceptionlevel = GLOBAL;
    }
}
