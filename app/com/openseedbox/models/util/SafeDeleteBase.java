package com.openseedbox.models.util;

import com.openseedbox.models.ModelBase;

public abstract class SafeDeleteBase<T extends ModelBase> {
    public abstract T apply();
    public abstract T findById(long id);
    public boolean vetoFilter(T d) { return false; };
    public void afterVeto() {};
    public abstract String nameInsteadId(T d);
    public void afterDelete(T d) {};
    abstract public void afterFinish();
}
