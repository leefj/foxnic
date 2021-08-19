package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheHelper {

    private String[] conditionProperties;
    private Class ownerType=null;


    public CacheHelper(Class ownerType,String... conditionProperties) {
        this.ownerType=ownerType;
        this.conditionProperties=conditionProperties;
    }

    public String makeKey(Object vo) {
        List<String> keyParts=new ArrayList<>();
        Map<String,Object> conditions=new HashMap<>();
        String value=null;
        for (String property : conditionProperties) {
            value=BeanUtil.getFieldValue(vo,property,String.class);
            conditions.put(property,value);
            keyParts.add(value);
        }
        String sortField=BeanUtil.getFieldValue(vo,"sortField",String.class);
        if(!StringUtil.isBlank(sortField)) {
            keyParts.add(sortField);
        }
        String sortType=BeanUtil.getFieldValue(vo,"sortType",String.class);
        if(!StringUtil.isBlank(sortType)) {
            keyParts.add(sortType);
        }
        return StringUtil.join(keyParts,":");
    }







}
