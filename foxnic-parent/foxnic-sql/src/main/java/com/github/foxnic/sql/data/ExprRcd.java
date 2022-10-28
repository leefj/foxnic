package com.github.foxnic.sql.data;

import com.github.foxnic.sql.meta.DBField;

import java.math.BigDecimal;
import java.util.Date;

public interface ExprRcd {

    Object getValue(String field);

    String getString(String field);
    String getString(DBField field);

    Integer getInteger(String field);
    Integer getInteger(DBField field);

    Double getDouble(String field);
    Double getDouble(DBField field);

    BigDecimal getBigDecimal(String field);
    BigDecimal getBigDecimal(DBField field);

    Date getDate(String field);
    Date getDate(DBField field);

    Boolean getBoolean(String field);
    Boolean getBoolean(DBField field);
}
