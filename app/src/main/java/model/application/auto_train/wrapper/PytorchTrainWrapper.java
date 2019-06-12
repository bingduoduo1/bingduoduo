package model.application.auto_train.wrapper;

import android.content.Context;
import android.util.Log;

import model.application.auto_train.base_interface.ArgumentContainerInterface;
import model.application.auto_train.pytorch_train.PytorchTrainArgumentContainer;
import model.application.auto_train.server_tool.ScpTool;
import model.config.GlobalException;

public class PytorchTrainWrapper implements BaseWarpperInterface, ArgumentContainerInterface {
    private static final String TAG = PytorchTrainWrapper.class.getName();
    private static PytorchTrainWrapper mWrapper = new PytorchTrainWrapper();
    private PytorchTrainArgumentContainer mContainer;
    private ScpTool mScpTool;
    private final String mBasePath = "/data/data/com.bingduoduo/files/home/";
    private final String mOutputConfigFilePath = mBasePath + "pytorch_train.yaml";
    private final String mServerConfigPath = "/Users/nyz/StudioProjects/bingduoduo/config.txt";
    private final String mLocalInfoPath = mBasePath + "info.txt";
    private final String mServerInfoPath = "/Users/nyz/StudioProjects/bingduoduo/info.txt";
    private PytorchTrainWrapper() {
        mContainer = new PytorchTrainArgumentContainer(mOutputConfigFilePath);
        mScpTool = ScpTool.createScpTool();
    }

    public static PytorchTrainWrapper createWrapper() {
        return mWrapper;
    }

    @Override
    public String getOutputFilePath() {
        return mOutputConfigFilePath;
    }

    @Override
    public int getSize() {
        return mContainer.getSize();
    }

    @Override
    public boolean isValid(String errContent) {
        return mContainer.isValid(errContent);
    }

    @Override
    public void saveObject2File() {
        mContainer.saveObject2File();
    }

    @Override
    public void updateValue(String key, String value) throws GlobalException {
        mContainer.updateValue(key, value);
    }

    @Override
    public String update(String key, String value) {
        try {
            this.updateValue(key, value);
            mContainer.saveObject2File();
            return "update " + key + "OK\n";
        } catch (GlobalException e) {
            return "update fail!---" + e.getMessage();
        }
    }

    @Override
    public String getShowConfigInfo() {
        return "cat " + mOutputConfigFilePath + "\n";
    }

    @Override
    public String checkConfig() {
        String content = null;
        if (!mContainer.isValid(content)) {
            return "echo [check config fail: " + content + "]\n";
        } else {
            return "echo [check config success]\n";
        }
    }

    @Override
    public void sendConfig() {
        mContainer.saveObject2File();
        Log.d(TAG, "nyzsend");
        mScpTool.sendMessageByPath(mOutputConfigFilePath, mServerConfigPath);
        Log.d(TAG, "nyzsend"+mScpTool.getExecRecord());
    }

    @Override
    public String receiveConfig() {
        mScpTool.receiveMessageByPath(mLocalInfoPath, mServerInfoPath);
        return "cat " + mLocalInfoPath + "\n";
    }

    @Override
    public void init() {
        mContainer.saveObject2File();
    }

    @Override
    public String getSendConfigCmd() {
        mContainer.saveObject2File();
        String cmd = mScpTool.getSendCmd(mOutputConfigFilePath, mServerConfigPath);
        return cmd;
    }

    @Override
    public String getReceiveConfigCmd() {
        String cmd = mScpTool.getReceiveCmd(mLocalInfoPath, mServerInfoPath);
        cmd += "cat " + mLocalInfoPath + "\n";
        return cmd;
    }
}
