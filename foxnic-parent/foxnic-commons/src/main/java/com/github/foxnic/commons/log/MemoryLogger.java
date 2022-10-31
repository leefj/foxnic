package com.github.foxnic.commons.log;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.ObjectUtil;

import java.lang.reflect.Field;
import java.util.List;

public class MemoryLogger {

    public void info(Object o) {
        if(o==null) return;

        CodeBuilder code=new CodeBuilder();

        List<Field> fields = BeanUtil.getAllFields(o.getClass());
        for (Field field : fields) {
            Object value=BeanUtil.getFieldValue(o,field.getName());
            long size=ObjectUtil.sizeOf(value);
        }




    }

}
