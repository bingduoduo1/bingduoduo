package model.dictionary.model;

import model.dictionary.exception.DictionaryException;
import model.dictionary.exception.NotMatchActionTypeExpection;

import static model.dictionary.helper.InputActionHelper.inputActionContentCheck;

public class InputAction extends model.dictionary.model.BaseAction {
    private String mcontent;
    
    public InputAction(ActionType actionType, ExecutePlaceType executePlace, String content)
            throws DictionaryException {
        super(actionType, executePlace);
        if (actionType != ActionType.INPUT) {
            throw new NotMatchActionTypeExpection();
        }
        if (!inputActionContentCheck(content)) {
            throw new DictionaryException("invalid input action content------------------" + content + "\n");
        }
        mcontent = content;
    }
    
    public String getContent() {
        return mcontent;
    }
}
