package model.dictionary.application;

import android.util.Log;

import model.dictionary.exception.DictionaryException;
import model.dictionary.model.BaseAction;
import model.dictionary.model.CommandAction;
import model.dictionary.model.InputAction;

public class GlobalDictionary implements LookUpInterface {
    private static final String TAG = GlobalDictionary.class.getName();
    private PythonDictionary mpythondict;
    private CommandDictionary mcommanddict;
    private TextDictionary mtextdict;
    private PytorchDictionary mpytorchdict;
    private static GlobalDictionary mGlobalDict = new GlobalDictionary();
    
    private GlobalDictionary() {
        mpythondict = PythonDictionary.createDictionary();
        mcommanddict = CommandDictionary.createDictionary();
        mtextdict = TextDictionary.createDictionary();
        mpytorchdict = PytorchDictionary.createDictionary();
    }
    
    public static GlobalDictionary createDictionary() {
        return mGlobalDict;
    }
    
    @Override
    public void exactLookUpWord(String word, StringBuffer action) throws DictionaryException {
        BaseAction actionRef = null;
        
        // Log.d(TAG, "exactLookUpWord:"+word +";");
        actionRef = mpytorchdict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append(((InputAction) actionRef).getContent());
                return;
            } else if (actionRef instanceof CommandAction) {
                action.append(((CommandAction) actionRef).getContent());
                return;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        }
        actionRef = mtextdict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append(((InputAction) actionRef).getContent());
                return;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        }
        actionRef = mpythondict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append(((InputAction) actionRef).getContent());
                return;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        }
        actionRef = mcommanddict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append(((InputAction) actionRef).getContent());
                return;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        } else {
            throw new DictionaryException("Action Ref is Null! word: " + word + "\n");
        }
    }
    
    @Override
    public void fuzzyLookUpWord(String word, StringBuffer action) throws DictionaryException {
        Log.d(TAG, "optim:" + word);
        if (word.startsWith("优化算法")) {
            int loc = word.indexOf("等于");
            String substr = word.substring(loc + 2).replace(" ", "").toLowerCase();
            Log.d(TAG, "optim1:" + substr);
            switch (substr) {
                case "adam": {
                    action.append("optim=adam");
                    break;
                }
                case "sjd": {
                    // fall through
                    break;
                }
                case "szd": {
                    // fall through
                    break;
                }
                case "sgd": {
                    action.append("optim=sgd");
                    break;
                }
                default: {
                    action.append("optim=invalid");
                }
            }
        } else if (word.startsWith("学习率") || word.startsWith("学习力") || word.startsWith("缺席率")
                || word.startsWith("learning_rate") || word.startsWith("learning rate")) {
            int loc = word.indexOf("等于");
            String substr = word.substring(loc + 2);
            try {
                float value = Float.parseFloat(substr);
                action.append("learning_rate=");
                action.append(value);
            } catch (NumberFormatException e) {
                action.append("learning_rate=invalid");
            }
        } else {
            throw new DictionaryException("invalid fuzzy look up\n");
        }
        // throw new NotImplementedError();
    }
}
