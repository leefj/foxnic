package com.github.foxnic.commons.xml;

public enum NodeNotExistAction {
    /**
     * 常规操作，节点不存在就报异常
     * */
    EXCEPTION,
    IGNORE,
    CREATE;
}
