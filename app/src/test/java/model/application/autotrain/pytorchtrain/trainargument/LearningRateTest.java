package model.application.autotrain.pytorchtrain.trainargument;

import org.junit.Test;

import model.config.GlobalException;

import static org.junit.Assert.*;

public class LearningRateTest {

    @Test
    public void updateValue() {
        LearningRate learningRate = new LearningRate(1.0);
        try {
            learningRate.updateValue("0.05");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        learningRate = new LearningRate();
        try {
            learningRate.updateValue("f05");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try {
            learningRate.updateValue("5.0");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try {
            learningRate.updateValue("0.05");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }

    @Test
    public void validationCheck() {
        LearningRate learningRate = new LearningRate(1.0);
        try {
            learningRate.validationCheck();
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }

    @Test
    public void toStringTest() {
        LearningRate learningRate = new LearningRate();
        System.out.println(learningRate.getArgumentName());
        System.out.println(learningRate.getDefaultValue());
        System.out.println(learningRate.toString());
    }

//    @Test
//    public void toStringTest2() {
//        LearningRate learningRate = new LearningRate();
//        try {
//            learningRate.updateValue("f5");
//        }catch (GlobalException e)
//        {
//            assertTrue(e instanceof GlobalException);
//        }
//    }
}
