package model.dictionary.helper;

import java.util.ArrayList;

public abstract class InputActionHelper extends GlobalHelper {
    
    private static ArrayList<String> mTokenList = new ArrayList<String>() {
        {
            add("(");
            add(")");
            add("()");
            add(":");
            add(".");
            add(",");
            add("-");
            add("_");
            add(" ");
            add("\n");
            add("    ");
        }
    };
    
    private static ArrayList<String> mShellKeyWordList = new ArrayList<String>() {
        {
            add("ls");
            add("cd");
            add("python");
            add("ipython");
            add("pwd");
            add("cp");
            add("mv");
            add("rm");
            add("chmod");
            add("bash");
            add("jump");
            add("train");
            add("man");
            add("git");
            add("ssh");
        }
    };
    
    private static ArrayList<String> mPythonKeyWordList = new ArrayList<String>() {
        {
            add("import ");
            add("from");
            add("def");
            add("with");
            add("as");
            add("if");
            add("elif");
            add("else");
            add("while");
            add("and");
            add("not");
            add("or");
            add("for");
            add("in");
            add("is");
            add("math");
            add("sin");
            add("cos");
            add("exit");
            add("numpy");
            add("function");
            add("test");
        }
    };
    
    private static ArrayList<String> mCombinedActionList = new ArrayList<String>() {
        {
            add("welcome to use BDD!!!");
            add("bash jump.sh");
            add("zsh run.sh");
            add("man ssh");
            add("man git");
            add("bash testgit.sh");
        }
    };
    
    private static ArrayList<String> mPytorchTrainArgumentList = new ArrayList<String>() {
        {
            add("learning_rate");
            add("batch_size");
            add("optim_algorithm");
            add("SGD");
            add("SGD_momentum");
            add("RMSprop");
            add("Adam");
        }
    };
    
    public static boolean inputActionContentCheck(String content) {
        return (isAlpha(content) || isInteger(content) || mTokenList.contains(content)
                || mShellKeyWordList.contains(content) || mPythonKeyWordList.contains(content)
                || mCombinedActionList.contains(content)) || mPytorchTrainArgumentList.contains(content);
    }
}
