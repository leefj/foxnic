package com.github.foxnic.commons.environment;

import com.github.foxnic.commons.network.Machine;

import java.sql.Timestamp;
import java.util.Date;

public class Environment {

    private static Environment environment = new Environment();

    public static Environment getEnvironment() {
        return environment;
    }

    private long bootTime;
    private long readyTime;
    private String machineId;

    public void init() {
        bootTime=System.currentTimeMillis();
        machineId= Machine.getIdentity();
    }

    public void  ready() {
        readyTime=System.currentTimeMillis();
    }


    public long getBootTime() {
        return bootTime;
    }

    public long getReadyTime() {
        return readyTime;
    }

    public String getMachineId() {
        return machineId;
    }

    public Date getNowDate() {
        return new Date();
    }

    public Timestamp getNowTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }


}
