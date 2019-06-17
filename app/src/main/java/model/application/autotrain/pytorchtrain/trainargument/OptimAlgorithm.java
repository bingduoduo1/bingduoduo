package model.application.autotrain.pytorchtrain.trainargument;

import java.util.HashMap;
import java.util.Locale;

import model.application.autotrain.pytorchtrain.PytorchTrainArgument;
import model.config.GlobalException;

public class OptimAlgorithm extends PytorchTrainArgument {
    private final String mname = "OptimAlgorithm";
    
    private enum Algorithm {
        SGD, SGD_momentum, RMSprop, Adam;
        private static HashMap<String, Algorithm> mAlgorithmMap = new HashMap<String, Algorithm>() {
            {
                put("SGD", SGD);
                put("SGD_momentum", SGD_momentum);
                put("RMSprop", RMSprop);
                put("Adam", Adam);
            }
        };
        
        static boolean keyCheck(String key) {
            return mAlgorithmMap.containsKey(key);
        }
        
        static String toString(Algorithm value) {
            for (String tmp : mAlgorithmMap.keySet()) {
                if (mAlgorithmMap.get(tmp) == value) {
                    return tmp;
                }
            }
            return null;
        }
        
        static Algorithm getValueOf(String key) {
            return mAlgorithmMap.get(key);
        }
    }
    
    private Algorithm mactualalgorithm;
    
    public OptimAlgorithm(String algorithm) {
        mactualalgorithm = Algorithm.getValueOf(algorithm);
    }
    
    public OptimAlgorithm() {
        this("SGD_momentum");
    }
    
    @Override
    public void updateValue(String key) throws GlobalException {
        try {
            this.validationCheck();
        } catch (GlobalException e) {
            throw new GlobalException("optim algorithm check before update fail!---" + e.getMessage());
        }
        if (!Algorithm.keyCheck(key)) {
            throw new GlobalException("Update optim algorithm fail!---invalid input:" + key + "\n");
        }
        mactualalgorithm = Algorithm.getValueOf(key);
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "optimization Algorithm: %s\n", Algorithm.toString(mactualalgorithm));
    }
    
    @Override
    public void validationCheck() throws GlobalException {
        if (null == Algorithm.toString(mactualalgorithm)) {
            throw new GlobalException("invalid optim algorithm");
        }
    }
    
    @Override
    public String getDefaultValue() {
        return "[default optim algorithm is " + Algorithm.toString(Algorithm.SGD_momentum) + "]\n";
    }
    
    @Override
    public String getArgumentName() {
        return mname;
    }
}
