package model.dictionary.application;


import android.util.Log;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.BaseAction;
import model.dictionary.model.CommandAction;
import model.dictionary.model.InputAction;

public class GlobalDictionary implements LookUpInterface {
    private static final String TAG = GlobalDictionary.class.getName();
    private PythonDictionary mPythonDict;
    private CommandDictionary mCommandDict;
    private TextDictionary mTextDict;
    private PytorchDictionary mPytorchDict;
    private static GlobalDictionary mGlobalDict = new GlobalDictionary();

    private GlobalDictionary() {
        mPythonDict = PythonDictionary.createDictionary();
        mCommandDict = CommandDictionary.createDictionary();
        mTextDict = TextDictionary.createDictionary();
        mPytorchDict = PytorchDictionary.createDictionary();
    }
    public static GlobalDictionary createDictionary() {
        return mGlobalDict;
    }
    @Override
    public void exactLookUpWord(String word, StringBuffer action) throws DictionaryException {
        BaseAction actionRef = null;

        //Log.d(TAG, "exactLookUpWord:"+word +";");
        actionRef = mPytorchDict.lookUpAction(word);
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
        actionRef = mTextDict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append(((InputAction) actionRef).getContent());
                return;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        }
        actionRef = mPythonDict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append( ((InputAction) actionRef).getContent());
                return;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        }
        actionRef = mCommandDict.lookUpAction(word);
        if (actionRef != null) {
            if (actionRef instanceof InputAction) {
                action.append(((InputAction) actionRef).getContent());
                return ;
            } else {
                throw new DictionaryException("invalid instance class name: " + actionRef.getClass().getSimpleName());
            }
        } else {
            throw new DictionaryException("Action Ref is Null! word: " + word + "\n");
        }
    }

    @Override
    public void fuzzyLookUpWord(String word, StringBuffer action) throws DictionaryException{
        Log.d(TAG, "optim:"+word);
        if (word.startsWith("优化算法")) {
            int loc = word.indexOf("等于");
            String substr = word.substring(loc+2).replace(" ", "").toLowerCase();
            Log.d(TAG, "optim1:"+substr);
            switch (substr) {
                case "adam": {
                    action.append("optim=adam");
                    break;
                }
                case "sjd": {
                    // fall through
                }
                case "szd": {
                    // fall through
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
            String substr = word.substring(loc+2);
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
        //throw new NotImplementedError();
    }
}
