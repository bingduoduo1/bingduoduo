package model.application.auto_train.wrapper;

import org.junit.Test;

import model.config.GlobalException;

import static org.junit.Assert.*;

public class PytorchTrainWrapperTest {
    PytorchTrainWrapper pytorchTrainWrapper = PytorchTrainWrapper.createWrapper();

    @Test
    public void getOutputFilePath() {
        System.out.println(pytorchTrainWrapper.getOutputFilePath());
        System.out.println(pytorchTrainWrapper.getSize());
        String errMsg = new String();
        System.out.println(pytorchTrainWrapper.isValid(errMsg));
        System.out.println(errMsg);

    }

    @Test
    public void saveObject2File() {
        pytorchTrainWrapper.saveObject2File();
    }

    @Test
    public void updateTest1() {
        System.out.println(pytorchTrainWrapper.update("batch_size", "3"));
    }

    @Test
    public void updateTest2() {
        System.out.println(pytorchTrainWrapper.update("batch_size", "0.2"));
    }

    @Test
    public void getShowConfigInfo() {
        System.out.println(pytorchTrainWrapper.getShowConfigInfo());

    }

    @Test
    public void checkConfig() {
        System.out.println(pytorchTrainWrapper.checkConfig());
    }

    @Test
    public void sendConfig() {
        pytorchTrainWrapper.sendConfig();
    }

    @Test
    public void receiveConfig() {
        System.out.println(pytorchTrainWrapper.receiveConfig());
    }

    @Test
    public void init() {
        pytorchTrainWrapper.init();
    }

    @Test
    public void getSendConfigCmd() {
        System.out.println(pytorchTrainWrapper.getSendConfigCmd());
    }

    @Test
    public void getReceiveConfigCmd() {
        System.out.println(pytorchTrainWrapper.getReceiveConfigCmd());
    }
}
