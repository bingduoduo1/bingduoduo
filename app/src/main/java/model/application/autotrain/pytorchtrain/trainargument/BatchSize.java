package model.application.autotrain.pytorchtrain.trainargument;

import java.util.Locale;

import model.application.autotrain.pytorchtrain.PytorchTrainArgument;
import model.config.GlobalException;

public class BatchSize extends PytorchTrainArgument {
    private final String mname = "BatchSize";
    private int mbatchsize;
    
    public BatchSize() {
        this(1);
    }
    
    public BatchSize(int batchSize) {
        mbatchsize = batchSize;
    }
    
    @Override
    public String getArgumentName() {
        return mname;
    }
    
    @Override
    public void updateValue(String value) throws GlobalException {
        try {
            this.validationCheck();
        } catch (GlobalException e) {
            throw new GlobalException("BatchSize check before update fail!---" + e.getMessage());
        }
        Integer tempValue;
        try {
            tempValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        if (!this.batchSizeValueCheck(tempValue)) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        mbatchsize = tempValue;
    }
    
    @Override
    public String getDefaultValue() {
        return "[positive int value is ok(default 1).]\n";
    }
    
    @Override
    public void validationCheck() throws GlobalException {
        if (!batchSizeValueCheck(mbatchsize)) {
            throw new GlobalException("invalid batchsize\n");
        }
    }
    
    private boolean batchSizeValueCheck(int value) {
        return value >= 1;
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "batch size: %d\n", mbatchsize);
    }
}
