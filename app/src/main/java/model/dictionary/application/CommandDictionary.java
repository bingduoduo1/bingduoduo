package model.dictionary.application;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotImplementedError;
import model.dictionary.model.ActionType;
import model.dictionary.model.BaseAction;
import model.dictionary.model.BaseWord;
import model.dictionary.model.CommandAction;
import model.dictionary.model.ExecutePlaceType;
import model.dictionary.model.InputAction;
import model.dictionary.model.CustomWord;
import model.dictionary.model.NatureLanguageType;

public class CommandDictionary implements BaseDictionaryInterface {
    private HashMap<BaseWord, BaseAction> mdictionary;
    private static CommandDictionary mDictReference = new CommandDictionary();

    private CommandDictionary() {
        mdictionary = new HashMap<BaseWord, BaseAction>();
        initDictionary();
    }

    public static CommandDictionary createDictionary() {
        return mDictReference;
    }

    @Override
    public void initDictionary() {
        try {
            mdictionary.put(new CustomWord("ls", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ls"));
            mdictionary.put(new CustomWord("cd", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "cd"));
            mdictionary.put(new CustomWord("pwd", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "pwd"));
            mdictionary.put(new CustomWord("cp", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "cp"));
            mdictionary.put(new CustomWord("copy", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "cp"));
            mdictionary.put(new CustomWord("mv", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "mv"));
            mdictionary.put(new CustomWord("move", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "mv"));
            mdictionary.put(new CustomWord("rm", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "rm"));
            mdictionary.put(new CustomWord("remove", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "rm"));
            mdictionary.put(new CustomWord("chmod", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "chmod"));
            mdictionary.put(new CustomWord("change mode", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "chmod"));
            mdictionary.put(new CustomWord("bash", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "bash"));
            mdictionary.put(new CustomWord("jump", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "bash jump.sh"));
            mdictionary.put(new CustomWord("train", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "zsh run.sh"));
            mdictionary.put(new CustomWord("man", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "man"));
            mdictionary.put(new CustomWord("git", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "git"));
            mdictionary.put(new CustomWord("ssh", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ssh"));
            mdictionary.put(new CustomWord("push", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "bash testgit.sh"));

            mdictionary.put(new CustomWord("python", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "python"));

            // 中文指令字
            mdictionary.put(new CustomWord("退格", NatureLanguageType.CHINESE),
                new CommandAction(ActionType.COMMAND, ExecutePlaceType.GENERAL, "backspace"));
            //python
            mdictionary.put(new CustomWord("派送", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ipython"));
            mdictionary.put(new CustomWord("拍手", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ipython"));
            mdictionary.put(new CustomWord("胎生", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ipython"));
            mdictionary.put(new CustomWord("pass and", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ipython"));
            mdictionary.put(new CustomWord("拍摄", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ipython"));
            mdictionary.put(new CustomWord("潘松", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "ipython"));


            // 组合动作(combined action)
            mdictionary.put(new CustomWord("bdd", NatureLanguageType.ENGLISH),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "welcome to use BDD!!!"));
            mdictionary.put(new CustomWord("冰多多", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "welcome to use BDD!!!"));
            mdictionary.put(new CustomWord("冰", NatureLanguageType.CHINESE),
                new InputAction(ActionType.INPUT, ExecutePlaceType.GENERAL, "welcome to use BDD!!!"));

        } catch (DictionaryException e) {
            e.printStackTrace();
            //Log.e(TAG, "init cmd dict fail");
        }
    }

    @Override
    public void initDictionary(String configPath) throws DictionaryException {
        throw new NotImplementedError();
    }

    public BaseAction lookUpAction(BaseWord key) {
        //for (Map.Entry<BaseWord, BaseAction> entry : mdictionary.entrySet()) {
        //Log.e(TAG, "Key:"+entry.getKey().getRawData() + " Value:" + entry.getValue().getActionType() );
        //}
        Pattern cmdHelp = Pattern.compile("[a-z]+ help$");
        Matcher cmdHelpMatch = cmdHelp.matcher(key.getRawData());
        if (mdictionary.containsKey(key)) {
            //Log.d(TAG, "lookUpAction: " + key.getRawData() + "::" + key.getNatureType());
            return mdictionary.get(key);
        } else if (cmdHelpMatch.find()) {
            String cmd = key.getRawData().split(" ")[0];
            //Log.d(TAG, "cmd content:"+cmd);
            if (cmd.equals("sh")) {
                cmd = "ssh";
            }
            if (cmd.equals("get")) {
                cmd = "git";
            }
            try {
                return new InputAction(ActionType.INPUT, ExecutePlaceType.SHELL, "man " + cmd);
            } catch (DictionaryException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
