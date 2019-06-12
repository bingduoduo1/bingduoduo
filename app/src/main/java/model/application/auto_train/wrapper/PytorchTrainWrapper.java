package model.application.auto_train.wrapper;

import model.application.auto_train.base_interface.ArgumentContainerInterface;
import model.application.auto_train.pytorch_train.PytorchTrainArgumentContainer;
import model.application.auto_train.server_tool.ScpTool;
import model.config.GlobalException;

public class PytorchTrainWrapper implements BaseWarpperInterface, ArgumentContainerInterface {
    private static PytorchTrainWrapper mWrapper = new PytorchTrainWrapper();
    private PytorchTrainArgumentContainer mContainer;
    private ScpTool mScpTool;
    private final String mOutputConfigFilePath = "pytorch_train.yaml";
    private final String mServerConfigPath = "/Users/nyz/StudioProjects/bingdoduo/config.txt";
    private final String mLocalInfoPath = "info.txt";
    private final String mServerInfoPath = "/Users/nyz/StudioProjects/bingdoduo/info.txt";
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
    public String getShowConfigInfo() {
        return "cat " + mOutputConfigFilePath + "\n";
    }

    @Override
    public String checkConfig() {
        String content = null;
        if (mContainer.isValid(content)) {
            return "echo [check config fail: " + content + "]\n";
        } else {
            return "echo [check config success]\n";
        }
    }

    @Override
    public void sendConfig() {
        mScpTool.sendMessageByPath(mOutputConfigFilePath, mServerConfigPath);
    }

    @Override
    public String receiveConfig() {
        mScpTool.receiveMessageByPath(mLocalInfoPath, mServerInfoPath);
        return "cat " + mLocalInfoPath + "\n";
    }
}