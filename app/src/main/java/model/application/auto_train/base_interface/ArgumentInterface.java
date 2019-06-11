package model.application.auto_train.base_interface;

import model.config.GlobalException;

public interface ArgumentInterface {
    void updateValue(String value) throws GlobalException;
    String getDefaultValue();
    String toString();
    void validationCheck() throws GlobalException;
}
