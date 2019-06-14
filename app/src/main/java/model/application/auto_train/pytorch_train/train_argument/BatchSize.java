package model.application.auto_train.pytorch_train.train_argument;



import java.util.Locale;

import model.application.auto_train.pytorch_train.PytorchTrainArgument;
import model.config.GlobalException;

public class BatchSize extends PytorchTrainArgument {
    private final String mName = "BatchSize";
    private int mBatchSize;

    public BatchSize() {
        this(1);
    }
    public BatchSize(int batchSize) {
        mBatchSize = batchSize;
    }

    @Override
    public String getArgumentName() {
        return mName;
    }

    @Override
    public void updateValue(String value) throws GlobalException {
        try {
            this.validationCheck();
        } catch (GlobalException e) {
            throw new GlobalException("BatchSize check before update fail!---"+e.getMessage());
        }
        Integer temp_value;
        try {
            temp_value = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        if (!this.batchSizeValueCheck(temp_value)) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        mBatchSize = temp_value;
    }

    @Override
    public String getDefaultValue() {
        return "[positive int value is ok(default 1).]\n";
    }

    @Override
    public void validationCheck() throws GlobalException {
        if (!batchSizeValueCheck(mBatchSize)) {
            throw new GlobalException("invalid batchsize\n");
        }
    }
    private boolean batchSizeValueCheck(int value) {
        return value >= 1;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"batch size: %d\n", mBatchSize);
    }
}
