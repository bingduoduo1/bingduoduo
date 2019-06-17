package model.dictionary.model;

public abstract class BaseAction {
    private ActionType mactionType;
    private ExecutePlaceType mexecutePlace;

    BaseAction(ActionType actionType, ExecutePlaceType executePlace) {
        mactionType = actionType;
        mexecutePlace = executePlace;
    }

    public ExecutePlaceType getExecutePlace() {
        return mexecutePlace;
    }
}
