package model.application.auto_train.server_tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import model.application.auto_train.base_interface.ServerCommunicationInterface;

public abstract class ServerTool implements ServerCommunicationInterface {
    private String mTempFileName;
    protected void setTempFileName(String name) {
        mTempFileName = name;
    }


    @Override
    public void sendMessage(String info, String serverPath) {
        try {
            File temp_file = new File(mTempFileName);
            if (!temp_file.exists()) {
                temp_file.createNewFile();
            }
            FileWriter writer = new FileWriter(temp_file);
            writer.write(info);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessageByPath(mTempFileName, serverPath);
    }

    @Override
    public void receiveMessage(String info, String serverPath) {
        receiveMessageByPath(mTempFileName, serverPath);
        try {
            File temp_file = new File(mTempFileName);
            if (!temp_file.exists()) {
                temp_file.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(temp_file));
            StringBuilder buffer = new StringBuilder();
            while (true) {
                String temp = reader.readLine();
                if (temp == null) {
                    break;
                } else {
                    buffer.append(temp+"\n");
                }
            }
            info = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
