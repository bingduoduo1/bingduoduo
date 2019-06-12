package model.application.auto_train.base_interface;

import model.config.GlobalException;

public interface ArgumentContainerInterface {
    int getSize();
    boolean isValid();
    void saveObject2File();
    String getOutputFilePath();
    void updateValue(String key, String value) throws GlobalException;
}
