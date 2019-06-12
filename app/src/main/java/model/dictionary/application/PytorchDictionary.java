package model.dictionary.application;

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

public class PytorchDictionary implements BaseDictionaryInterface {
    private HashMap<BaseWord, BaseAction> mDictionary;
    private static PytorchDictionary mDictReference = new PytorchDictionary();

    private PytorchDictionary() {
        mDictionary = new HashMap<BaseWord, BaseAction>();
        this.initDictionary();
    }

    public static PytorchDictionary createDictionary() {
        return mDictReference;
    }

    @Override
    public void initDictionary() {
        try {
           mDictionary.put(new CustomWord("learning rate", NatureLanguageType.ENGLISH),
               new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "learning_rate"));
            mDictionary.put(new CustomWord("batch size", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "batch_size"));
            mDictionary.put(new CustomWord("optimization", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "optim_algorithm"));
            mDictionary.put(new CustomWord("SGD", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "SGD"));
            mDictionary.put(new CustomWord("momentum", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "SGD_momentum"));
            mDictionary.put(new CustomWord("Adam", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "Adam"));
            mDictionary.put(new CustomWord("RMS", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.TRAIN_CONFIG, "RMSprop"));
            mDictionary.put(new CustomWord("show", NatureLanguageType.ENGLISH),
                new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "show"));
            mDictionary.put(new CustomWord("check", NatureLanguageType.ENGLISH),
                new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "check"));
            mDictionary.put(new CustomWord("send", NatureLanguageType.ENGLISH),
                new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "send"));
            mDictionary.put(new CustomWord("receive", NatureLanguageType.ENGLISH),
                new CommandAction(ActionType.COMMAND, ExecutePlaceType.TRAIN_CONFIG, "receive"));
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initDictionary(String config_path) throws DictionaryException {
        throw new NotImplementedError();
    }

    @Override
    public BaseAction lookUpAction(BaseWord key) {
        if (mDictionary.containsKey(key)) {
            return mDictionary.get(key);
        } else {
            return null;
        }
    }
}
