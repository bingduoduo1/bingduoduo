package model.application.auto_train.pytorch_train;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.application.auto_train.base_interface.ArgumentContainerInterface;
import model.application.auto_train.pytorch_train.train_argument.BatchSize;
import model.application.auto_train.pytorch_train.train_argument.LearningRate;
import model.application.auto_train.pytorch_train.train_argument.OptimAlgorithm;
import model.application.auto_train.wrapper.PytorchTrainWrapper;
import model.config.GlobalException;

public class PytorchTrainArgumentContainer implements ArgumentContainerInterface {
    private static final String TAG = PytorchTrainWrapper.class.getName();
    private static final String defaultTemplateFilePath = "default_config_pytorch_train.yaml";
    private String mOutputFilePath;
    private static ArrayList<String> trainArgumentKey = new ArrayList<String>(){{
        add("learning_rate");
        add("batch_size");
        add("optim_algorithm");
    }};
    private HashMap<String, PytorchTrainArgument> mConfigMap = new HashMap<String, PytorchTrainArgument>();
    public PytorchTrainArgumentContainer(String outputFilePath) {
        this(defaultTemplateFilePath, outputFilePath);
    }

    public PytorchTrainArgumentContainer(String templateFilePath, String outputFilePath) {
        mOutputFilePath = outputFilePath;
        // TODO: load config from file
        mConfigMap.put("learning_rate", new LearningRate());
        mConfigMap.put("batch_size", new BatchSize());
        mConfigMap.put("optim_algorithm", new OptimAlgorithm());
    }

    @Override
    public boolean isValid(String errContent) {
        if (!configKeyCheck()) {
            errContent = "key check error";
            return false;
        }
        for(PytorchTrainArgument tmp: mConfigMap.values()) {
            try {
                tmp.validationCheck();
            } catch (GlobalException e) {
                System.err.println(e.getMessage());
                errContent = e.getMessage();
                return false;
            }
        }
        return true;
    }
    private boolean configKeyCheck() {
        HashSet<String> actualSet = new HashSet<String>(mConfigMap.keySet());
        HashSet<String> expectSet = new HashSet<String>(trainArgumentKey);
        Log.d(TAG, "actual:"+actualSet.toString());
        Log.d(TAG, "expect:"+expectSet.toString());
        int originSize = actualSet.size();
        actualSet.addAll(expectSet);
        int checkSize = actualSet.size();
        Log.d(TAG, "expect:"+originSize+"\t"+checkSize);
        return originSize == checkSize;
    }

    @Override
    public int getSize() {
        return trainArgumentKey.size();
    }

    @Override
    public String getOutputFilePath() {
        return mOutputFilePath;
    }

    @Override
    public void saveObject2File() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("******************************\n");
        buffer.append("train config content:\n");
        for (PytorchTrainArgument tmp: mConfigMap.values()) {
            buffer.append("\t" + tmp.toString() + tmp.getDefaultValue());
        }
        buffer.append("******************************\n");
        File output_file = new File(mOutputFilePath);
        FileWriter writer = null;
        try {
            boolean ret = output_file.createNewFile();
            Log.d(TAG, "create file:" + ret);
            writer = new FileWriter(output_file);
            writer.write(buffer.toString());
            writer.flush();
            writer.close();
        } catch (IOException e){
            Log.d(TAG, "create file error");
            e.printStackTrace();
        }
    }

    @Override
    public void updateValue(String key, String value) throws GlobalException {
        if (mConfigMap.containsKey(key)) {
            try {
                mConfigMap.get(key).updateValue(value);
            } catch (GlobalException e) {
                throw e;
            }
        } else {
            throw new GlobalException("invalid pytorch train argument");
        }
    }
}
