package model.application.auto_train.server_tool;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScpToolTest {
    ScpTool expectedScpTool;
    String info = "Hello,world!";
    String localPath = "test.txt";
    String remotePath = "document//test.txt";
    @Before
    public void createScpTool() {
//        ScpTool expectedScpTool = new ScpTool();
        expectedScpTool = ScpTool.createScpTool();
        assertTrue(expectedScpTool.getTempFileName().equals("scp_temp_file.txt"));
        assertNull(expectedScpTool.getExecRecord());
    }

    @Test
    public void sendMessageByPath() {

        expectedScpTool.sendMessageByPath(localPath, remotePath);

    }

    @Test
    public void receiveMessageByPath() {
        String local = "test.txt";
        String remote = "document//test.txt";
        expectedScpTool.receiveMessageByPath(local, remote);

    }

    @Test
    public void sendText()
    {
        expectedScpTool.sendMessage(info, localPath);
    }
    @Test
    public void receiveText()
    {
        expectedScpTool.receiveMessage(info, remotePath);
    }
}
