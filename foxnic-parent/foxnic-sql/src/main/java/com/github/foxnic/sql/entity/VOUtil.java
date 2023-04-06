package com.github.foxnic.sql.entity;

import com.github.foxnic.api.model.CompositeItem;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;

public class VOUtil {

    /**
     * 从 VO 提取属性值
     * */
    public static  <T,V> V getPropertyValue(T vo,String prop,Class<V> type) {
        Object value= BeanUtil.getFieldValue(vo,prop);
        if(vo instanceof Entity) {
            Entity entity = (Entity) vo;
            CompositeItem drafterNameItem = entity.getCompositeParameter().getItem(prop);
            if (StringUtil.isBlank(value) && drafterNameItem != null) {
                value = drafterNameItem.getValue();
            }
        }
        return DataParser.parse(type,value);
    }

    /**
     * 设置 VO 的属性值
     * */
    public static <T> void setPropertyValue(T vo,String prop,Object value) {
        // 提取 drafterName 并是参数置空以取消底层的查询
        BeanUtil.setFieldValue(vo,prop,value);
        if(vo instanceof Entity) {
            Entity entity = (Entity) vo;
            CompositeItem drafterNameItem = entity.getCompositeParameter().getItem(prop);
            if (drafterNameItem != null) {
                drafterNameItem.setValue(value);
            }
        }
    }

}
