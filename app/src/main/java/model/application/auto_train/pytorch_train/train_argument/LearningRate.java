package model.application.auto_train.pytorch_train.train_argument;

import java.util.Locale;

import model.application.auto_train.pytorch_train.PytorchTrainArgument;
import model.config.GlobalException;

public class LearningRate extends PytorchTrainArgument {
    private final String mName = "LearningRate";
    private double mInitLr;

    public LearningRate(double initLr) {
        mInitLr = initLr;
    }
    public LearningRate() {
        this(0.01);
    }


    @Override
    public String getArgumentName() {
        return mName;
    }

    @Override
    public String getDefaultValue() {
        return "[positive double value is OK(default 0.01)]\n";
    }

    @Override
    public void updateValue(String value) throws GlobalException {
        try {
            this.validationCheck();
        } catch (GlobalException e) {
            throw new GlobalException("LR check before update fail!---"+e.getMessage());
        }
        Double temp_value;
        try {
            temp_value = Double.parseDouble(value);
        } catch(NumberFormatException e){
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        if (!this.initLrCheck(temp_value)) {
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
