package com.github.foxnic.dao.dataperm;

public class DataPermException extends RuntimeException {

    public DataPermException(String message) {
        super(message);
    }

    public DataPermException(String message,Throwable t) {
        super(message,t);
    }

}
