package model.application.auto_train.pytorch_train.train_argument;

import java.util.Locale;

import model.application.auto_train.pytorch_train.PytorchTrainArgument;
import model.config.GlobalException;

public class LearningRate extends PytorchTrainArgument {
    private final String mName = "LearningRate";
    private double mInitLr;

    public void PytorchTrainArgument() {
        PytorchTrainArgument(0.01);
    }

    public void PytorchTrainArgument(double initLr) {
        mInitLr = initLr;
    }

    @Override
    public String getDefaultValue() {
        return "positive double value is OK";
    }

    @Override
    public void updateValue(String value) throws GlobalException {
        try {
            this.validationCheck();
        } catch (GlobalException e) {
            throw new GlobalException("LR check before update fail!---"+e.getMessage());
        }

        double temp_value = Double.parseDouble(value);
        if (temp_value == Double.NaN || !this.initLrCheck(temp_value)) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        mInitLr = temp_value;
    }

    @Override
    public void validationCheck() throws GlobalException {
        if (!initLrCheck(mInitLr)) {
            throw new GlobalException("invalid init lr\n");
        }
    }

    private boolean initLrCheck(double value) {
        return value > 0 && value < 1;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"init_lr: %.6f\n", mInitLr);
    }
}
