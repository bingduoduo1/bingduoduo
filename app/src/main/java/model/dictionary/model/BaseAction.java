package model.dictionary.model;

public abstract class BaseAction {
    private ActionType mActionType;
    private ExecutePlaceType mExecutePlace;

    BaseAction(ActionType actionType, ExecutePlaceType executePlace) {
        mActionType = actionType;
        mExecutePlace = executePlace;
    }

//    public ActionType getActionType()
//    {
//        return mActionType;
//    }

    public ExecutePlaceType getExecutePlace() {
        return mExecutePlace;
    }
}
