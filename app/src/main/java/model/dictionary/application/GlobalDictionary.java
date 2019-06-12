package model.dictionary.application;


import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.BaseAction;
import model.dictionary.model.InputAction;

public class GlobalDictionary implements LookUpInterface {
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
        }else{
            throw new DictionaryException("Action Ref is Null!");
        }
    }

    @Override
    public void fuzzyLookUpWord(String word, String action) throws DictionaryException{
        throw new NotImplementedError();
    }
}
