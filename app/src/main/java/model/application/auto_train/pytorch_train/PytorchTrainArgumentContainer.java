package model.application.auto_train.pytorch_train;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import model.application.auto_train.base_interface.ArgumentContainerInterface;
import model.application.auto_train.pytorch_train.train_argument.BatchSize;
import model.application.auto_train.pytorch_train.train_argument.LearningRate;
import model.application.auto_train.pytorch_train.train_argument.OptimAlgorithm;
import model.config.GlobalException;

public class PytorchTrainArgumentContainer implements ArgumentContainerInterface {
    private static final String defaultTemplateFilePath = "config_pytorch_train.yaml";
    private String mOutputFilePath;
    private static ArrayList<String> trainArgumentKey = new ArrayList<String>(){{
        add("learning_rate");
        add("batch_size");
        add("optim_algorithm");
    }};
    private HashMap<String, PytorchTrainArgument> mConfigMap;
    public void PytorchTrainArgumentContainer(String outputFilePath) {
        PytorchTrainArgumentContainer(defaultTemplateFilePath, outputFilePath);
    }

    public void PytorchTrainArgumentContainer(String templateFilePath, String outputFilePath) {
        mOutputFilePath = outputFilePath;
        // TODO: load config from file
        mConfigMap.put("learning_rate", new LearningRate());
        mConfigMap.put("batch_size", new BatchSize());
        mConfigMap.put("optim_algorithm", new OptimAlgorithm());
    }

    @Override
    public boolean isValid() {
        if (!configKeyCheck()) {
            return false;
        }
        for(PytorchTrainArgument tmp: mConfigMap.values()) {
            try {
                tmp.validationCheck();
            } catch (GlobalException e) {
                System.err.println(e.getMessage());
                return false;
            }
        }
        return true;
    }
    private boolean configKeyCheck() {
        Set actualSet = mConfigMap.keySet();
        Set expectSet = new HashSet<String>(trainArgumentKey);
        int originSize = actualSet.size();
        actualSet.addAll(expectSet);
        int checkSize = actualSet.size();
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
        for (PytorchTrainArgument tmp: mConfigMap.values()) {
            buffer.append(tmp.toString());
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(mOutputFilePath));
        } catch (IOException e){
            e.printStackTrace();
        }
        out.write(buffer.toString());
        out.flush();
        out.close();
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
