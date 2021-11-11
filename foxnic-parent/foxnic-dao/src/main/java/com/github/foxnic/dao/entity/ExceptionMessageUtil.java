package com.github.foxnic.dao.entity;

import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBIndexMeta;
import com.github.foxnic.dao.meta.DBMetaData;
import com.github.foxnic.dao.meta.DBTableMeta;
import org.springframework.dao.DuplicateKeyException;

public class ExceptionMessageUtil {

    public static Result getResult(Exception exception, SuperService service) {
        Result result=ErrorDesc.failure(CommonError.DATA_REPETITION);
        String msg=null;
        if(exception instanceof  DuplicateKeyException) {
            msg=getDuplicateKeyExceptionMessage((DuplicateKeyException)exception,service);
            if(msg!=null) {
                result.message(msg);
            }
        }

        return result;
    }

    private static String getDuplicateKeyExceptionMessage(DuplicateKeyException exception, SuperService service) {
        String msg=exception.getMessage();
        String key="' for key '";
        int a=msg.lastIndexOf(key);
        if(a==-1) return null;
        int b=msg.indexOf("'",a+key.length());
        key=msg.substring(a+key.length(),b);
        DBTableMeta tm=service.getDBTableMeta();
        DBIndexMeta index=tm.getIndex(key);
        if(index==null) {
            DBMetaData.buildIndex(service.dao(),service.table(),tm);
            index=tm.getIndex(key);
        }
        if(index==null) return null;
        String[] fields=index.getFields();
        String[] names=new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            names[i] = tm.getColumn(fields[i]).getLabel();
        }
        return "相同的 "+StringUtil.join(names)+" 已经存在，请勿重复添加";
    }
}
