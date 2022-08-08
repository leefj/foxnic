package com.github.foxnic.dao.cache;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.relation.Join;
import com.github.foxnic.dao.relation.PropertyRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * @param isAccurate  是否精确匹配
     * @param cacheEmptyResult 是否缓存空的集合对象
     * @param conditionProperties 属性清单
     * */
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
    public String makeKey(boolean updating,Object... param) {
        if(this.isAccurate) {
            return makeAccurateKey(updating,param);
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

    private String makeAccurateKey(boolean updating , Object[] param) {
        if(param.length==0) return null;
        if(param.length>1) {
            throw new IllegalArgumentException("仅支持单一参数");
        }
        Object vo=param[0];
        if(vo==null) return null;

        if(vo instanceof String) {
            return this.name+":"+vo;
        }

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

        Map<String,Object> otherPropValueMap=BeanUtil.toMap(vo);
        List<String> keyParts=new ArrayList<>();
        String value=null;
        for (String property : conditionProperties) {
            value=BeanUtil.getFieldValue(vo,property,String.class);
            keyParts.add(value);
            otherPropValueMap.remove(property);
        }
        otherPropValueMap.remove("sortField");
        otherPropValueMap.remove("sortType");

        // 如果还有其它查询条件，则不匹配缓存键
        if(!updating && !otherPropValueMap.isEmpty()) return null;


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

    public String getName() {
        return name;
    }

    public String[] makeRelatedKeys(PropertyRoute route, Entity entity) {
        String[] keys=new String[2];
        String key="join:"+route.getKey()+":"+route.getPropertyWithClass();

        Join last2Join=null;

        if(route.getJoinsCount()>=2){
            List<Join> joins=route.getJoins();
            last2Join=joins.get(joins.size()-2);
        }
        //last2Join=null;

        //编辑当前对象，精准控制缓存
        if(ReflectUtil.isSubType(route.getSlavePoType(),entity.getClass())) {
            List<String> keyParts=new ArrayList<>();
            String value=null;
            for (String property : conditionProperties) {
                value=BeanUtil.getFieldValue(entity,property,String.class);
                keyParts.add(value);
            }
            String parts=StringUtil.join(keyParts);
            keys[0]=key+":records:"+parts;
            keys[1]=key+":record:"+parts;
        }
        //维护了最接近 target 的关系表，精准控制缓存
        else if(last2Join!=null) {
            List<String> keyParts=new ArrayList<>();
            String value=null;
            for (String property : conditionProperties) {
                value=BeanUtil.getFieldValue(entity,property,String.class);
                keyParts.add(value);
            }
            String parts=StringUtil.join(keyParts);
            keys[0]=key+":records:"+parts;
            keys[1]=key+":record:"+parts;
        }
        // 维护了较远的关系表，精准匹配太复杂，全部抹杀
        else {
            keys[0]=key+":records:**:**";
            keys[1]=key+":record:**:**";
        }
        return keys;
    }
}
