package com.github.foxnic.dao.dataperm.model;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.dataperm.DataPermManager;

public class DataPermVariable {

    private Object value;

    public DataPermVariable(DataPermManager dataPermManager, Object variable){

    }

    private Object fetchValue(DataPermSubjectVariable subjectVariable) {
        Object subjectData=subjectVariable.getSubject().getSubjectGetter().get();
        String[] props=subjectVariable.getProperty().split("\\.");
        Object value=subjectData;
        for (String prop : props) {
            value= BeanUtil.getFieldValue(value,prop);
            if(value==null) return null;
        }
        return value;
    }

    public Object getValue() {
        return value;
    }
}
