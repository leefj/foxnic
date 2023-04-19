package com.github.foxnic.api.transter;

public class ResultDataVisitor {
    private Result result;
    public ResultDataVisitor(Result result) {
        this.result=result;
    }
    public <E> E getData(Class<E> type) {
        return (E)result.getData(type);
    }

    public <E> E getDataByKey(Object key,Class<E> type) {
        return (E)result.getDataByKey(key,type);
    }

}
