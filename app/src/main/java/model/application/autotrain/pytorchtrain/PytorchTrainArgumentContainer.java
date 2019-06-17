package model.application.autotrain.pytorchtrain;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.application.autotrain.baseinterface.ArgumentContainerInterface;
import model.application.autotrain.pytorchtrain.trainargument.BatchSize;
import model.application.autotrain.pytorchtrain.trainargument.LearningRate;
import model.application.autotrain.pytorchtrain.trainargument.OptimAlgorithm;
import model.application.autotrain.wrapper.PytorchTrainWrapper;
import model.config.GlobalException;

public class PytorchTrainArgumentContainer implements ArgumentContainerInterface {
    private static final String TAG = PytorchTrainWrapper.class.getName();
    private static final String defaultTemplateFilePath = "default_config_pytorch_train.yaml";
    private String moutputfilepath;
    private static ArrayList<String> trainArgumentKey = new ArrayList<String>() {
        {
            add("learning_rate");
            add("batch_size");
            add("optim_algorithm");
        }
    };
    private HashMap<String, PytorchTrainArgument> mconfigmap = new HashMap<String, PytorchTrainArgument>();
    
    public PytorchTrainArgumentContainer(String outputFilePath) {
        this(defaultTemplateFilePath, outputFilePath);
    }
    
    public PytorchTrainArgumentContainer(String templateFilePath, String outputFilePath) {
        moutputfilepath = outputFilePath;
        // TODO: load config from file
        mconfigmap.put("learning_rate", new LearningRate());
        mconfigmap.put("batch_size", new BatchSize());
        mconfigmap.put("optim_algorithm", new OptimAlgorithm());
    }
    
    @Override
    public boolean isValid(String errContent) {
        if (!configKeyCheck()) {
            errContent = "key check error";
            return false;
        }
        for (PytorchTrainArgument tmp : mconfigmap.values()) {
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
        HashSet<String> actualSet = new HashSet<String>(mconfigmap.keySet());
        HashSet<String> expectSet = new HashSet<String>(trainArgumentKey);
        Log.d(TAG, "actual:" + actualSet.toString());
        Log.d(TAG, "expect:" + expectSet.toString());
        int originSize = actualSet.size();
        actualSet.addAll(expectSet);
        int checkSize = actualSet.size();
        Log.d(TAG, "expect:" + originSize + "\t" + checkSize);
        return originSize == checkSize;
    }
    
    @Override
    public int getSize() {
        return trainArgumentKey.size();
    }
    
    @Override
    public String getOutputFilePath() {
        return moutputfilepath;
    }
    
    @Override
    public void saveObject2File() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("******************************\n");
        buffer.append("train config content:\n");
        for (PytorchTrainArgument tmp : mconfigmap.values()) {
            buffer.append("\t" + tmp.toString() + tmp.getDefaultValue());
        }
        buffer.append("******************************\n");
        File outputFile = new File(moutputfilepath);
        FileWriter writer = null;
        try {
            boolean ret = outputFile.createNewFile();
            Log.d(TAG, "create file:" + ret);
            writer = new FileWriter(outputFile);
            writer.write(buffer.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.d(TAG, "create file error");
            e.printStackTrace();
        }
    }
    
    @Override
    public void updateValue(String key, String value) throws GlobalException {
        if (mconfigmap.containsKey(key)) {
            try {
                mconfigmap.get(key).updateValue(value);
            } catch (GlobalException e) {
                throw e;
            }
        } else {
            throw new GlobalException("invalid pytorch train argument");
        }
    }
}
