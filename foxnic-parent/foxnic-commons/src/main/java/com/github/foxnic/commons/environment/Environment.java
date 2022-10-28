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
        // 加速启动
        new Thread() {
            @Override
            public void run() {
                machineId= Machine.getIdentity();
            }
        }.start();

    }

    public void  ready() {
        readyTime=System.currentTimeMillis();
    }


    /**
     * 应用启动时间
     * */
    public long getBootTime() {
        return bootTime;
    }

    /**
     * 应用启动完成时间
     * */
    public long getReadyTime() {
        return readyTime;
    }

    /**
     * 机器码
     * */
    public String getMachineId() {
        return machineId;
    }

    /**
     * 当前时间
     * */
    public Date getNowDate() {
        return new Date();
    }

    /**
     * 当前时间戳
     * */
    public Timestamp getNowTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }



}
