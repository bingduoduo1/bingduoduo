package model.application.autotrain.servertool;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScpTool extends ServerTool {
    private static final String TAG = ScpTool.class.getName();
    private static ScpTool mScpTool = new ScpTool();
    private String mexecrecord;
    
    private ScpTool() {
        super.setTempFileName("scp_temp_file.txt");
        mexecrecord = null;
    }
    
    public static ScpTool createScpTool() {
        return mScpTool;
    }
    
    public String getExecRecord() {
        return mexecrecord;
    }
    
    @Override
    public void sendMessageByPath(String localPath, String serverPath) {
        String shellCmd = "sshpass -p xxx scp " + localPath + " nyz@10.136.47.63:" + serverPath + "\n";
        Log.d(TAG, "nyzsend:" + shellCmd);
        this.scpExec(shellCmd);
    }
    
    public String getSendCmd(String localPath, String serverPath) {
        String shellCmd = "sshpass -f ps.txt scp " + localPath + " nyz@10.136.47.63:" + serverPath + "\n";
        return shellCmd;
    }
    
    public String getReceiveCmd(String localPath, String serverPath) {
        String shellCmd = "sshpass -f ps.txt scp " + " nyz@10.136.47.63:" + serverPath + " " + localPath + "\n";
        return shellCmd;
    }
    
    @Override
    public void receiveMessageByPath(String localPath, String serverPath) {
        String shellCmd = "sshpass -p xxx scp " + " nyz@10.136.47.63:" + serverPath + " " + localPath + "\n";
        this.scpExec(shellCmd);
    }
    
    private void scpExec(String cmd) {
        try {
            Process scpProcess = Runtime.getRuntime().exec(cmd);
            
            InputStream in = scpProcess.getInputStream();
            InputStream err = scpProcess.getErrorStream();
            
            try {
                int ret = scpProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BufferedReader readerIn = new BufferedReader(new InputStreamReader(in));
            StringBuilder builderIn = new StringBuilder();
            while (true) {
                String temp = readerIn.readLine();
                if (temp == null) {
                    Log.d(TAG, "nyzsend stdout empty");
                    break;
                } else {
                    builderIn.append(temp + "\n");
                }
            }
            BufferedReader readerErr = new BufferedReader(new InputStreamReader(err));
            StringBuilder builderErr = new StringBuilder();
            while (true) {
                String temp = readerErr.readLine();
                if (temp == null) {
                    Log.d(TAG, "nyzsend stderr empty");
                    break;
                } else {
                    builderErr.append(temp + "\n");
                }
            }
            mexecrecord = "stdout:\n" + builderIn.toString() + "stderr:\n" + builderErr.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
