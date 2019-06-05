package model.application.auto_train.pytorch_train.train_argument;

import org.junit.Test;

import model.config.GlobalException;

import static org.junit.Assert.*;

public class OptimAlgorithmTest {

    @Test
    public void updateValue() {
        OptimAlgorithm optimAlgorithm = new OptimAlgorithm("ssssd");
        try {
            optimAlgorithm.updateValue("SGD");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        optimAlgorithm = new OptimAlgorithm();
        try {
            optimAlgorithm.updateValue("sSGD");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try {
            optimAlgorithm.updateValue("SGD");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }

    @Test
    public void validationCheck() {
        OptimAlgorithm optimAlgorithm = new OptimAlgorithm("ssssd");
        System.out.println(optimAlgorithm.toString());
        try {
            optimAlgorithm.validationCheck();
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }

    @Test
    public void getDefaultValue() {
    }

    @Test
    public void getArgumentName() {
        OptimAlgorithm optimAlgorithm = new OptimAlgorithm();
        System.out.println(optimAlgorithm.toString());
        System.out.println(optimAlgorithm.getArgumentName());
        System.out.println(optimAlgorithm.getDefaultValue());
    }
}
