package model.application.auto_train.pytorch_train.train_argument;

<<<<<<< HEAD
=======
import android.util.Log;

>>>>>>> zpj
import model.application.auto_train.pytorch_train.PytorchTrainArgument;
import model.config.GlobalException;

import java.util.HashMap;
import java.util.Locale;

public class OptimAlgorithm extends PytorchTrainArgument {
<<<<<<< HEAD
=======
    private static final String TAG = OptimAlgorithm.class.getName();
>>>>>>> zpj
    private final String mName = "OptimAlgorithm";
    private enum Algorithm {
        SGD,
        SGD_momentum,
        RMSprop,
        Adam;
        private static HashMap<String, Algorithm> mAlgorithmMap = new HashMap<String, Algorithm>() {{
<<<<<<< HEAD
            put("SGD", SGD);
            put("SGD_momentum", SGD_momentum);
            put("RMSprop", RMSprop);
            put("Adam", Adam);
        }};
        static boolean keyCheck(String key) {
=======
            put("sgd", SGD);
            put("sgd_momentum", SGD_momentum);
            put("rmsprop", RMSprop);
            put("adam", Adam);
        }};
        static boolean keyCheck(String key) {
            Log.d(TAG, "optim3:"+key);
>>>>>>> zpj
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
<<<<<<< HEAD
        this("SGD_momentum");
=======
        this("sgd_momentum");
>>>>>>> zpj
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
<<<<<<< HEAD
        return String.format(Locale.ENGLISH,"init_lr: %s\n", Algorithm.toString(mActualAlgorithm));
=======
        return String.format(Locale.ENGLISH,"optimization Algorithm: %s\n", Algorithm.toString(mActualAlgorithm));
>>>>>>> zpj
    }

    @Override
    public void validationCheck() throws GlobalException {
        if (null == Algorithm.toString(mActualAlgorithm)) {
            throw new GlobalException("invalid optim algorithm");
        }
    }

    @Override
    public String getDefaultValue() {
<<<<<<< HEAD
        return "default optim algorithm is " + Algorithm.toString(Algorithm.SGD_momentum);
=======
        return "[default optim algorithm is " + Algorithm.toString(Algorithm.SGD_momentum) + "]\n";
>>>>>>> zpj
    }

    @Override
    public String getArgumentName() {
        return mName;
    }
}
