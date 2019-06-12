package model.application.auto_train.wrapper;

public interface BaseWarpperInterface {
    String getShowConfigInfo();
    String checkConfig();
    void sendConfig();
    String receiveConfig();
}
