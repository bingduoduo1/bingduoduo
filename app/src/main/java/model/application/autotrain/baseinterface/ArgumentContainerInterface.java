package model.application.autotrain.baseinterface;

import model.config.GlobalException;

public interface ArgumentContainerInterface {
    int getSize();

    boolean isValid(String errContent);

    void saveObject2File();

    String getOutputFilePath();

    void updateValue(String key, String value) throws GlobalException;
}
