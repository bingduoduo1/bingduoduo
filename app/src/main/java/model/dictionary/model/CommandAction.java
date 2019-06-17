package model.dictionary.model;

import android.util.Log;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotMatchActionTypeExpection;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static model.dictionary.helper.CommandActionHelper.commandActionContentCheck;

public class CommandAction extends BaseAction {
    private String mcontent;
    
    public CommandAction(ActionType actionType, ExecutePlaceType executePlace, String content)
            throws DictionaryException {
        super(actionType, executePlace);
        if (actionType != ActionType.COMMAND) {
            throw new NotMatchActionTypeExpection();
        }
        if (!commandActionContentCheck(content)) {
            Log.d(TAG, "check cmd content error\n");
            throw new DictionaryException("invalid input action content------------------\n");
        }
        mcontent = content;
    }
    
    public String getContent() {
        return mcontent;
    }
}
