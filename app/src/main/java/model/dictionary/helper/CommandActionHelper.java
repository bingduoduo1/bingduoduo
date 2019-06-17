package model.dictionary.helper;

import java.util.ArrayList;

public abstract class CommandActionHelper extends GlobalHelper {
    private static ArrayList<String> mCommandList = new ArrayList<String>() {
        {
            add("backspace"); // origin DEL
        }
    };
    
    private static ArrayList<String> mAutoTrainCommandList = new ArrayList<String>() {
        {
            add("show");
            add("check");
            add("send");
            add("receive");
        }
    };
    
    public static boolean commandActionContentCheck(String content) {
        return (mCommandList.contains(content) || mAutoTrainCommandList.contains(content));
    }
}
