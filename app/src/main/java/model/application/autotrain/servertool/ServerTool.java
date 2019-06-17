package model.application.autotrain.servertool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import model.application.autotrain.baseinterface.ServerCommunicationInterface;

public abstract class ServerTool implements ServerCommunicationInterface {
    private String mtempfilename;
    
    protected void setTempFileName(String name) {
        mtempfilename = name;
    }
    
    public String getTempFileName() {
        return mtempfilename;
    }
    
    @Override
    public void sendMessage(String info, String serverPath) {
        try {
            File tempFile = new File(mtempfilename);
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            FileWriter writer = new FileWriter(tempFile);
            writer.write(info);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessageByPath(mtempfilename, serverPath);
    }
    
    @Override
    public void receiveMessage(String info, String serverPath) {
        receiveMessageByPath(mtempfilename, serverPath);
        try {
            File tempFile = new File(mtempfilename);
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            StringBuilder buffer = new StringBuilder();
            while (true) {
                String temp = reader.readLine();
                if (temp == null) {
                    break;
                } else {
                    buffer.append(temp + "\n");
                }
            }
            info = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
