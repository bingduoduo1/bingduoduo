package model.application.auto_train.pytorch_train.train_argument;

import org.junit.Before;
import org.junit.Test;

import model.config.GlobalException;

import static org.junit.Assert.*;

public class BatchSizeTest {
    BatchSize batchSize1;
    BatchSize batchSize2;
    @Before
    public void setUp()
    {
        batchSize1 = new BatchSize();
        batchSize2 = new BatchSize(2);
    }


    @Test
    public void updateValue() {
        BatchSize batchSize = new BatchSize(0);
        try {
            batchSize.updateValue("1");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try {
            batchSize1.updateValue("1.0");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try {
            batchSize1.updateValue("0");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
        try {
            batchSize1.updateValue("20");
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }



    @Test
    public void validationCheck() {
        BatchSize batchSize = new BatchSize(0);
        try {
            batchSize.validationCheck();
        }catch (GlobalException e)
        {
            assertTrue(e instanceof GlobalException);
        }
    }

    @Test
    public void toStringTest() {
        System.out.println(batchSize1.getArgumentName());
        System.out.println(batchSize2.getDefaultValue());
        System.out.println(batchSize2.toString());
    }
}
