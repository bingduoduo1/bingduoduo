package model.dictionary.application;

import android.util.Log;

import java.util.HashMap;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.ActionType;
import model.dictionary.model.BaseAction;
import model.dictionary.model.BaseWord;
import model.dictionary.model.CommandAction;
import model.dictionary.model.CustomWord;
import model.dictionary.model.ExecutePlaceType;
import model.dictionary.model.InputAction;
import model.dictionary.model.NatureLanguageType;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PytorchDictionary implements BaseDictionaryInterface {
    private HashMap<BaseWord, BaseAction> mdictionary;
    private static PytorchDictionary mDictReference = new PytorchDictionary();
    
    private PytorchDictionary() {
        mdictionary = new HashMap<BaseWord, BaseAction>();
        this.initDictionary();
    }
    
    public static PytorchDictionary createDictionary() {
        return mDictReference;
    }
    
    @Override
    public void initDictionary() {
        try {
            mdictionary.put(new CustomWord("learning rate", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "learning_rate"));
            mdictionary.put(new CustomWord("batch size", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "batch_size"));
            mdictionary.put(new CustomWord("optimization", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "optim_algorithm"));
            mdictionary.put(new CustomWord("SGD", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "SGD"));
            mdictionary.put(new CustomWord("momentum", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "SGD_momentum"));
            mdictionary.put(new CustomWord("Adam", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "Adam"));
            mdictionary.put(new CustomWord("RMS", NatureLanguageType.ENGLISH),
                    new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "RMSprop"));
            mdictionary.put(new CustomWord("show", NatureLanguageType.ENGLISH),
                    new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "show"));
            mdictionary.put(new CustomWord("展示", NatureLanguageType.CHINESE),
                    new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "show"));
            mdictionary.put(new CustomWord("check", NatureLanguageType.ENGLISH),
                    new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "check"));
            mdictionary.put(new CustomWord("send", NatureLanguageType.ENGLISH),
                    new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "send"));
            mdictionary.put(new CustomWord("发送", NatureLanguageType.CHINESE),
                    new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "send"));
            mdictionary.put(new CustomWord("receive", NatureLanguageType.ENGLISH),
                    new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "receive"));
        } catch (DictionaryException e) {
            Log.d(TAG, "initDictionary: Errrrorrororororrororororrororororo");
            e.printStackTrace();
        }
    }
    
    @Override
    public void initDictionary(String configPath) throws DictionaryException {
        throw new NotImplementedError();
    }
    
    @Override
    public BaseAction lookUpAction(BaseWord key) {
        if (mdictionary.containsKey(key)) {
            return mdictionary.get(key);
        } else {
            return null;
        }
    }
}
