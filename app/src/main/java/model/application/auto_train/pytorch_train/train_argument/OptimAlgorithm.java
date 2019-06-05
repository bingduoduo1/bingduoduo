package model.application.auto_train.pytorch_train.train_argument;

import model.application.auto_train.pytorch_train.PytorchTrainArgument;
import model.config.GlobalException;

import java.util.HashMap;
import java.util.Locale;

public class OptimAlgorithm extends PytorchTrainArgument {
    private final String mName = "OptimAlgorithm";
    private enum Algorithm {
        SGD,
        SGD_momentum,
        RMSprop,
        Adam;
        private static HashMap<String, Algorithm> mAlgorithmMap = new HashMap<String, Algorithm>() {{
            put("SGD", SGD);
            put("SGD_momentum", SGD_momentum);
            put("RMSprop", RMSprop);
            put("Adam", Adam);
        }};
        static boolean keyCheck(String key) {
            return mAlgorithmMap.containsKey(key);
        }

        static String toString(Algorithm value) {
            for (String tmp: mAlgorithmMap.keySet()) {
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
    private Algorithm mActualAlgorithm;

    public OptimAlgorithm (String algorithm) {
        mActualAlgorithm = Algorithm.getValueOf(algorithm);
    }

    public OptimAlgorithm () {
        this("SGD_momentum");
    }

    @Override
    public void updateValue(String key) throws GlobalException {
        try {
            this.validationCheck();
        } catch (GlobalException e) {
            throw new GlobalException("optim algorithm check before update fail!---"+e.getMessage());
        }
        if (!Algorithm.keyCheck(key)) {
            throw new GlobalException("Update optim algorithm fail!---invalid input:" + key + "\n");
        }
        mActualAlgorithm = Algorithm.getValueOf(key);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"init_lr: %s\n", Algorithm.toString(mActualAlgorithm));
    }

    @Override
    public void validationCheck() throws GlobalException {
        if (null == Algorithm.toString(mActualAlgorithm)) {
            throw new GlobalException("invalid optim algorithm");
        }
    }

    @Override
    public String getDefaultValue() {
        return "default optim algorithm is " + Algorithm.toString(Algorithm.SGD_momentum);
    }

    @Override
    public String getArgumentName() {
        return mName;
    }
}
