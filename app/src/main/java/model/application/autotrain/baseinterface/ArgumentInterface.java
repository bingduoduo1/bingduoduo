package model.application.autotrain.baseinterface;

import model.config.GlobalException;

public interface ArgumentInterface {
    void updateValue(String value) throws GlobalException;

    String getDefaultValue();

    String toString();

    void validationCheck() throws GlobalException;
}
