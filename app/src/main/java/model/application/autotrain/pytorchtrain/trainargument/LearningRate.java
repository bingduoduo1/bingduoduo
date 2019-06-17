package model.application.autotrain.pytorchtrain.trainargument;

import java.util.Locale;

import model.application.autotrain.pytorchtrain.PytorchTrainArgument;
import model.config.GlobalException;

public class LearningRate extends PytorchTrainArgument {
    private final String mname = "LearningRate";
    private double minitlr;
    
    public LearningRate(double initLr) {
        minitlr = initLr;
    }
    
    public LearningRate() {
        this(0.01);
    }
    
    @Override
    public String getArgumentName() {
        return mname;
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
            throw new GlobalException("LR check before update fail!---" + e.getMessage());
        }
        Double tempValue;
        try {
            tempValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        if (!this.initLrCheck(tempValue)) {
            throw new GlobalException("Update value fail!---invalid input:" + value + "\n");
        }
        minitlr = tempValue;
    }
    
    @Override
    public void validationCheck() throws GlobalException {
        if (!initLrCheck(minitlr)) {
            throw new GlobalException("invalid init lr\n");
        }
    }
    
    private boolean initLrCheck(double value) {
        return value > 0 && value < 1;
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "init_lr: %.6f\n", minitlr);
    }
}
