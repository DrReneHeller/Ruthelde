package com.ruthelde.Target;

import java.util.Observable;

public class TargetModel extends Observable {

    private String notificationMessage;

    private Target target;

    public TargetModel(Target target, String notificationMessage) {
        this.target = target;
        this.notificationMessage = notificationMessage;
    }

    public Target getTarget() {
        return this.target;
    }

    public void setTarget(Target target)
    {
        this.target = target;
        setChanged();
        notifyObservers(notificationMessage);

    }

    public void setTargetSilent(Target target){
        this.target = target;
    }
}
