package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.dao.entity.Entity;

public class CacheInvalidEvent<E extends Entity> {

    private CacheInvalidEventType eventType;
    private E valueBefore;
    private E valueAfter;
    private String dataTable;

    public CacheInvalidEvent(CacheInvalidEventType eventType,String dataTable,E valueBefore,E valueAfter) {
        this.eventType = eventType ;
        this.dataTable = dataTable ;
        this.valueBefore = valueBefore;
        this.valueAfter = valueAfter;
    }

    public CacheInvalidEventType getEventType() {
        return eventType;
    }

    public E getValueBefore() {
        return valueBefore;
    }

    public E getValueAfter() {
        return valueAfter;
    }

    public String getDataTable() {
        return dataTable;
    }

}
