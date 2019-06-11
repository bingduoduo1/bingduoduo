package model.application.auto_train.server_tool;

import java.io.IOException;
import java.io.OutputStream;

public class ScpTool extends ServerTool{
    private static ScpTool mScpTool = new ScpTool();
    private String mExecRecord;

    public String getExecRecord() {
        return mExecRecord;
    }
    private ScpTool() {
        super.setTempFileName("scp_temp_file.txt");
        mExecRecord = null;
    }
    public static ScpTool createScpTool() {
        return mScpTool;
    }
    @Override
    public void sendMessageByPath(String localPath, String serverPath) {
        String shell_cmd = "scp " + localPath + " nyz@10.135.250.200:" + serverPath + "\n";
        this.scpExec(shell_cmd);
    }

    @Override
    public void receiveMessageByPath(String localPath, String serverPath) {
        String shell_cmd = "scp " + " nyz@10.135.250.200:" + serverPath + " " + localPath + "\n";
        this.scpExec(shell_cmd);
    }

    private void scpExec(String cmd) {
        try {
            Process scp_process = Runtime.getRuntime().exec(cmd);
            OutputStream out = scp_process.getOutputStream();
            mExecRecord = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
