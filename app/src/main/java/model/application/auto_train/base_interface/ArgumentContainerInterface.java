package model.application.auto_train.base_interface;

public interface ArgumentContainerInterface {
    int getSize();
    boolean isValid();
    void saveObject2File();
    String getOutputFilePath();
}
