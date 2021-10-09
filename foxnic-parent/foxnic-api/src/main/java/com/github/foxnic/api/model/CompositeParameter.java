package com.github.foxnic.api.model;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

public class CompositeParameter implements Iterable<CompositeItem>, Serializable {

    private List<String> IGNORE_KEYS=Arrays.asList("pageIndex","pageSize","searchField","fuzzyField","searchValue","sortField","sortType","ids");

    private Map<String, CompositeItem> items = null;
    /**
     * 将复合查询的值转换成VO，便于取数
     * */
    public CompositeParameter(String searchValue,Map<String,Object> map) {
        JSONObject json=JSONObject.parseObject(searchValue);
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
                    item.setKey(e.getKey());
//                  不可以设置，对查询有影响
//                    if(StringUtils.isBlank(item.getField())) {
//                        item.setField(e.getKey());
//                    }
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
