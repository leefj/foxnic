package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.lang.StringUtil;

public enum SortType {

    ASC, DESC, ASC_NL,DESC_NL;

    public static SortType parse(String sort) {
        if(StringUtil.isBlank(sort)) return null;
        sort=sort.trim();
        for (SortType value : SortType.values()) {
            if(value.name().equalsIgnoreCase(sort)) return value;
        }
        return null;
    }



}
