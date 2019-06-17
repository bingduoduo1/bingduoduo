package model.application.autotrain.pytorchtrain;

import org.junit.Test;


import model.config.GlobalException;

import static org.junit.Assert.*;

public class PytorchTrainArgumentContainerTest {
    PytorchTrainArgumentContainer pytorchTrainArgumentContainer = new PytorchTrainArgumentContainer("test.txt");
    @Test
    public void updateTest(){
        String errMsg = "";
        pytorchTrainArgumentContainer.isValid(errMsg);
    }

    @Test
    public void updateTest1()
    {
    }
    @Test
    public void updateTest2()
    {
        try {
            pytorchTrainArgumentContainer.updateValue("speed", "1.00");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try{
            pytorchTrainArgumentContainer.updateValue("batch_size", "0.2");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }

    @Test
    public void isValid() {
    }

    @Test
    public void getSize() {
        System.out.println(pytorchTrainArgumentContainer.getSize());
        System.out.println(pytorchTrainArgumentContainer.getOutputFilePath());
        pytorchTrainArgumentContainer.saveObject2File();
    }
    @Test
    public void saveTest()
    {
        PytorchTrainArgumentContainer pytorchTrainArgumentContainer1 = new PytorchTrainArgumentContainer("testException.txt");
        pytorchTrainArgumentContainer1.saveObject2File();
    }


}
