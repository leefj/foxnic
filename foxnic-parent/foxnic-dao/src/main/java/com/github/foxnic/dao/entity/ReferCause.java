package com.github.foxnic.dao.entity;

public class ReferCause {



    private String table;

    private String field;

    private boolean hasRefer = false;
    private String message;

    public boolean hasRefer() {
        return hasRefer;
    }

    public String message() {
        return message;
    }

    /**
     * 引用的数据表
     * */
    public String table() {
        return table;
    }

    /**
     * 引用的字段
     * */
    public String field() {
        return field;
    }


    public ReferCause(boolean hasRefer , String message,String table,String field) {
        this.hasRefer=hasRefer;
        this.message=message;
        this.table=table;
        this.field=field;
    }

    public ReferCause(boolean hasRefer) {
        this.hasRefer=hasRefer;
    }

    public static ReferCause noRefers() {
        return new ReferCause(false);
    }

    public static class Names<T> {

        private T localId;
        private String masterName;
        private String localName;
        public String getMasterName() {
            return masterName;
        }

        public void setMasterName(String masterName) {
            this.masterName = masterName;
        }

        public String getLocalName() {
            return localName;
        }

        public void setLocalName(String localName) {
            this.localName = localName;
        }

        public T getLocalId() {
            return localId;
        }

        public void setLocalId(T localId) {
            this.localId = localId;
        }
    }

}
