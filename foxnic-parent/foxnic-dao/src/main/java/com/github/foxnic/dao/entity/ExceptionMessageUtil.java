package com.github.foxnic.dao.entity;

import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBIndexMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import org.springframework.dao.DuplicateKeyException;

public class ExceptionMessageUtil {

    public static final String LOCK_TIMEOUT = "Lock wait timeout exceeded;";
    public static final String LINK_FAILURE = "Communications link failure";

    public static Result getResult(Exception exception, SuperService service) {
        Result result=ErrorDesc.failure(CommonError.DATA_REPETITION);
        String msg=null;
        if(exception instanceof  DuplicateKeyException) {
            msg=getDuplicateKeyExceptionMessage((DuplicateKeyException)exception,service);
            if(msg!=null) {
                result.message(msg);
            }
        } else {
            String message=exception.getMessage();
            if(message.contains(LOCK_TIMEOUT)) {
                result.message("数据操作等待超时，可能该数据已被锁定，请检查事务锁");
            }
            else if(message.contains(LINK_FAILURE)) {
                result.message("数据库连接被关闭，操作失败");
            }
        }

        return result;
    }

    private static String getDuplicateKeyExceptionMessage(DuplicateKeyException exception, SuperService service) {
        DBIndexMeta index=service.getUniqueIndex(exception);
        if(index==null) return "保存失败，唯一键索引冲突";
        String[] fields=index.getFields();
        if(fields==null || fields.length==0) {
            throw new IllegalArgumentException("无法识别索引字段");
        }
        DBTableMeta tm = service.getDBTableMeta();
        String[] names=new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            names[i] = tm.getColumn(fields[i]).getLabel();
        }
        return "相同的 "+StringUtil.join(names)+" 已经存在，请勿重复添加";
    }
}
