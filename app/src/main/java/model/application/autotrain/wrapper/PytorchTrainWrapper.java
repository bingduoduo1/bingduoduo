package model.application.autotrain.wrapper;

import android.util.Log;

import model.application.autotrain.baseinterface.ArgumentContainerInterface;
import model.application.autotrain.pytorchtrain.PytorchTrainArgumentContainer;
import model.application.autotrain.servertool.ScpTool;
import model.config.GlobalException;

public class PytorchTrainWrapper implements BaseWarpperInterface, ArgumentContainerInterface {
    private static final String TAG = PytorchTrainWrapper.class.getName();
    private static PytorchTrainWrapper mWrapper = new PytorchTrainWrapper();
    private PytorchTrainArgumentContainer container;
    private ScpTool scpTool;
    private final String mbasepath = "/data/data/com.bingduoduo/files/home/";
    private final String moutputconfigfilepath = mbasepath + "pytorch_train.yaml";
    private final String mserverconfigpath = "/Users/nyz/StudioProjects/bingduoduo/config.txt";
    private final String mlocalinfopath = mbasepath + "info.txt";
    private final String mserverinfopath = "/Users/nyz/StudioProjects/bingduoduo/info.txt";
    
    private PytorchTrainWrapper() {
        container = new PytorchTrainArgumentContainer(moutputconfigfilepath);
        scpTool = ScpTool.createScpTool();
    }
    
    public static PytorchTrainWrapper createWrapper() {
        return mWrapper;
    }
    
    @Override
    public String getOutputFilePath() {
        return moutputconfigfilepath;
    }
    
    @Override
    public int getSize() {
        return container.getSize();
    }
    
    @Override
    public boolean isValid(String errContent) {
        return container.isValid(errContent);
    }
    
    @Override
    public void saveObject2File() {
        container.saveObject2File();
    }
    
    @Override
    public void updateValue(String key, String value) throws GlobalException {
        container.updateValue(key, value);
    }
    
    @Override
    public String update(String key, String value) {
        try {
            this.updateValue(key, value);
            container.saveObject2File();
            return "update " + key + "OK\n";
        } catch (GlobalException e) {
            return "update fail!---" + e.getMessage();
        }
    }
    
    @Override
    public String getShowConfigInfo() {
        return "cat " + moutputconfigfilepath + "\n";
    }
    
    @Override
    public String checkConfig() {
        String content = null;
        if (!container.isValid(content)) {
            return "echo [check config fail: " + content + "]\n";
        } else {
            return "echo [check config success]\n";
        }
    }
    
    @Override
    public void sendConfig() {
        container.saveObject2File();
        Log.d(TAG, "nyzsend");
        scpTool.sendMessageByPath(moutputconfigfilepath, mserverconfigpath);
        Log.d(TAG, "nyzsend" + scpTool.getExecRecord());
    }
    
    @Override
    public String receiveConfig() {
        scpTool.receiveMessageByPath(mlocalinfopath, mserverinfopath);
        return "cat " + mlocalinfopath + "\n";
    }
    
    @Override
    public void init() {
        container.saveObject2File();
    }
    
    @Override
    public String getSendConfigCmd() {
        container.saveObject2File();
        String cmd = scpTool.getSendCmd(moutputconfigfilepath, mserverconfigpath);
        return cmd;
    }
    
    @Override
    public String getReceiveConfigCmd() {
        String cmd = scpTool.getReceiveCmd(mlocalinfopath, mserverinfopath);
        cmd += "cat " + mlocalinfopath + "\n";
        return cmd;
    }
}
