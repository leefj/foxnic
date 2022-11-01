package com.github.foxnic.commons.log;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ObjectUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RamLogger {

    public static void info(String tag,Object o) {
        if(o==null) return;

        int total=0;

        Map<String,Integer> sizeMap=new HashMap<>();
        List<Field> fields = BeanUtil.getAllFields(o.getClass());
        for (Field field : fields) {
            Object value=BeanUtil.getFieldValue(o,field.getName());
            int size=ObjectUtil.sizeOf(value);
            total += size;
            sizeMap.put(field.getName(),size);
        }

        StringBuilder builder = new StringBuilder("\n┏━━━ RAM [ "+tag+" , total = "+total/(1024)+"KB , "+o.getClass().getName()+" ] ━━━ \n");
        for (Map.Entry<String, Integer> e : sizeMap.entrySet()) {
            builder.append("┣ "+e.getKey()+" : "+ e.getValue()/1024 + "KB , "+Math.round((e.getValue() * 10000.0)/total)/100+"% \n");
        }
        builder.append("┗━━━ PERFORMANCE [ "+tag+" , total = "+total/(1024)+"KB , "+o.getClass().getName()+" ] ━━━");

        Logger.info(builder);

    }

}
