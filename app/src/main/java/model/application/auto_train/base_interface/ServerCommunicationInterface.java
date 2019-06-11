package model.application.auto_train.base_interface;

public interface ServerCommunicationInterface {
    void sendMessage(String info, String serverPath);
    void receiveMessage(String info, String serverPath);
    void sendMessageByPath(String localPath, String serverPath);
    void receiveMessageByPath(String localPath, String serverPath);
}
