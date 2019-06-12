package model.application.auto_train.wrapper;

public interface BaseWarpperInterface {
    void init();
    String getShowConfigInfo();
    String checkConfig();
    void sendConfig();
    String getSendConfigCmd();
    String receiveConfig();
    String getReceiveConfigCmd();
}
