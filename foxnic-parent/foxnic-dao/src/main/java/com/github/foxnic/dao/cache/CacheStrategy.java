package com.github.foxnic.dao.cache;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class CacheStrategy {

    private String[] conditionProperties;
    private String name;

    public boolean isAccurate() {
        return isAccurate;
    }

    /**
     * 是否精准计算归属的 key
     * */
    private boolean isAccurate=false;

    /**
     * 是否缓存null和空的集合对象
     * */
    public boolean isCacheEmptyResult() {
        return cacheEmptyResult;
    }

    /**
     * 是否缓存空对象
     * */
    private boolean cacheEmptyResult;

    public CacheStrategy(String name,boolean isAccurate,boolean cacheEmptyResult,String... conditionProperties) {
        this.name=name;
        this.isAccurate=isAccurate;
        this.conditionProperties=conditionProperties;
        this.cacheEmptyResult=cacheEmptyResult;

    }

    /**
     * 制作缓存 key
     * @param param 传入方法的参数
     * */
    public String makeKey(Object... param) {
        if(this.isAccurate) {
            return makeAccurateKey(param);
        } else {
            return makeGeneralKey(this.name,param);
        }
    }

    /**
     * 制作通用的缓存 key
     * @param param 传入方法的参数
     * */
    public static String makeGeneralKey(String prefix,Object... param) {
        JSONArray array=new JSONArray();
        for (Object o : param) {
            array.add(o);
        }
        return  prefix+":"+MD5Util.encrypt32(array.toJSONString());
    }

    private String makeAccurateKey(Object[] param) {
        if(param.length==0) return null;
        if(param.length>1) {
            throw new IllegalArgumentException("仅支持单一参数");
        }
        Object vo=param[0];
        if(vo==null) return null;

        if(!(vo instanceof Entity)) {
            throw new IllegalArgumentException("仅支实体");
        }

        String searchField=BeanUtil.getFieldValue(vo,"searchField",String.class);
        if(!StringUtil.isBlank(searchField)) {
            Logger.error("指定 searchField 时缓存无效");
            BeanUtil.getFieldValue(vo,"searchField",String.class);
            return null;
        }
        String fuzzyField=BeanUtil.getFieldValue(vo,"fuzzyField",String.class);
        if(!StringUtil.isBlank(fuzzyField)) {
            Logger.error("指定 fuzzyField 时缓存无效");
            BeanUtil.getFieldValue(vo,"fuzzyField",String.class);
            return null;
        }

        List<String> keyParts=new ArrayList<>();
        String value=null;
        for (String property : conditionProperties) {
            value=BeanUtil.getFieldValue(vo,property,String.class);
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
        return this.name+":"+StringUtil.join(keyParts,":");

    }


}
