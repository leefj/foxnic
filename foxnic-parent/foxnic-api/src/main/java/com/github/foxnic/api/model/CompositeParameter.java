package com.github.foxnic.api.model;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;


import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

public class CompositeParameter implements Iterable<CompositeItem>, Serializable {

    private List<String> IGNORE_KEYS=Arrays.asList("pageIndex","pageSize","searchField","fuzzyField","searchValue","sortField","sortType","ids");

    private Map<String, CompositeItem> items = null;

    private Set<String> fuzzyFields = null;
    private Set<String> searchFields = null;
    private String searchValue = null;

    public Set<String> getFuzzyFields() {
        return fuzzyFields;
    }

    public Set<String> getSearchFields() {
        return searchFields;
    }

    public String getSearchValue() {
        return searchValue;
    }

    /**
     * 将复合查询的值转换成VO，便于取数
     * */
    public CompositeParameter(String searchValue,Map<String,Object> map) {


        String searchFieldStr=(String)map.get("searchField");
        String fuzzyFieldStr=(String)map.get("fuzzyField");

        this.searchValue = searchValue;

        fuzzyFields=new HashSet<>();
        if(!StringUtils.isBlank(fuzzyFieldStr)) {
            fuzzyFields.addAll(Arrays.asList(fuzzyFieldStr.split(",")));
        }

        JSONObject json=null;

        //如果是复合查询，则转换Json
        if("$composite".equals(searchFieldStr) && searchValue!=null) {
            json = JSONObject.parseObject(searchValue);
        } else {
            //如果是非复合查询，就设置查询值
            if(!StringUtils.isBlank(searchFieldStr)) {
                String[] fs=searchFieldStr.split(",");
                searchFields=new HashSet<>();
                searchFields.addAll(Arrays.asList(fs));
            }
        }
        if(json==null) {
            json=new JSONObject();
        }
        items=new HashMap<>();
        JSONObject jsonItem = null;
        Object itm;
        for (String key : json.keySet()) {
            itm=json.get(key);
            if(itm==null) continue;
            if(itm instanceof  JSONObject) {
                jsonItem = (JSONObject) itm;
            } else {
                jsonItem=new JSONObject();
                jsonItem.put("value",itm);
            }
            CompositeItem item=jsonItem.toJavaObject(CompositeItem.class);
            item.setParameter(this);
            item.setKey(key);
//            不可以设置，对查询有影响
//            if(StringUtils.isBlank(item.getField())) {
//                item.setField(key);
//            }
            items.put(key,item);
        }
        //
        if(map!=null) {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if(IGNORE_KEYS.contains(e.getKey())) continue;
                //如果有值，且对应的key已经存在，则忽略
                if(e.getValue()!=null && items.get(e.getKey())!=null) {
                    continue;
                }
                if(e.getValue()!=null && items.get(e.getKey())==null) {
                    jsonItem=new JSONObject();
                    jsonItem.put("value",e.getValue());
                    CompositeItem item=jsonItem.toJavaObject(CompositeItem.class);
                    item.setParameter(this);
                    if(fuzzyFields.contains(e.getKey())) {
                        item.setFuzzy(true);
                    }
                    item.setKey(e.getKey());
                    items.put(e.getKey(),item);
                }
            }
        }

    }

    public CompositeItem getItem(String key) {
        return this.items.get(key);
    }

    public Object getValue(String key) {
        CompositeItem itm=this.items.get(key);
        if(itm==null) return null;
        return itm.getValue();
    }

    @Override
    public Iterator<CompositeItem> iterator() {
        return items.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super CompositeItem> action) {
        items.values().forEach(action);
    }

    @Override
    public Spliterator<CompositeItem> spliterator() {
        return items.values().spliterator();
    }
}
