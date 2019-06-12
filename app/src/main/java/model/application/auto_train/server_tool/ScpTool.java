package model.application.auto_train.server_tool;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScpTool extends ServerTool {
    private static final String TAG = ScpTool.class.getName();
    private static ScpTool mScpTool = new ScpTool();
    private String mExecRecord;

    private ScpTool() {
        super.setTempFileName("scp_temp_file.txt");
        mExecRecord = null;
    }

    public static ScpTool createScpTool() {
        return mScpTool;
    }

    public String getExecRecord() {
        return mExecRecord;
    }

    @Override
    public void sendMessageByPath(String localPath, String serverPath) {
        String shell_cmd = "sshpass -p xxx scp " + localPath + " nyz@10.136.47.63:" + serverPath + "\n";
        Log.d(TAG, "nyzsend:"+shell_cmd);
        this.scpExec(shell_cmd);
    }

    public String getSendCmd(String localPath, String serverPath) {
        String shell_cmd = "sshpass -p xxx scp " + localPath + " nyz@10.136.47.63:" + serverPath + "\n";
        return shell_cmd;
    }

    public String getReceiveCmd(String localPath, String serverPath) {
        String shell_cmd = "sshpass -p xxx scp " + " nyz@10.136.47.63:" + serverPath + " " + localPath + "\n";
        return shell_cmd;
    }

    @Override
    public void receiveMessageByPath(String localPath, String serverPath) {
        String shell_cmd = "sshpass -p xxx scp " + " nyz@10.136.47.63:" + serverPath + " " + localPath + "\n";
        this.scpExec(shell_cmd);
    }

    private void scpExec(String cmd) {
        try {
            Process scp_process = Runtime.getRuntime().exec(cmd);

            InputStream in = scp_process.getInputStream();
            InputStream err = scp_process.getErrorStream();

            try {
                int ret = scp_process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BufferedReader reader_in = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder_in = new StringBuilder();
            while (true) {
                String temp = reader_in.readLine();
                if (temp == null) {
                    Log.d(TAG, "nyzsend stdout empty");
                    break;
                } else {
                    builder_in.append(temp + "\n");
                }
            }
            BufferedReader reader_err = new BufferedReader(new InputStreamReader(err));
            StringBuilder builder_err = new StringBuilder();
            while (true) {
                String temp = reader_err.readLine();
                if (temp == null) {
                    Log.d(TAG, "nyzsend stderr empty");
                    break;
                } else {
                    builder_err.append(temp + "\n");
                }
            }
            mExecRecord = "stdout:\n" + builder_in.toString() + "stderr:\n" + builder_err.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
