package com.github.foxnic.dao.queue;

import com.github.foxnic.dao.entity.Entity;

public class SimpleMessage<M extends Entity> {
    private M message;
    private int retrys=0;

    public SimpleMessage(M message) {
        this.message=message;
    }

    public int getRetrys() {
        return retrys;
    }

    public void setRetrys(int retrys) {
        this.retrys = retrys;
    }

    public M getMessage() {
        return message;
    }
}
