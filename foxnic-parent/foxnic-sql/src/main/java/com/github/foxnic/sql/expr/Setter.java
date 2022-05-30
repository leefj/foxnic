package com.github.foxnic.sql.expr;

import com.github.foxnic.sql.meta.DBField;

import java.util.Map;

/**
 * 语句设值置器，Update和Insert语句使用
 * */
public interface Setter<T extends Setter> extends SQL {

	T set(String fld,Object val);
	T set(DBField fld, Object val);
	T sets(Object... nvs);
	T setsIf(Object... nvs);
	T setIf(String fld,Object val);
	T set(String fld,SQL se);
	T setExpr(String fld,Expr se);
	T setExprIf(String fld,Expr se);
	T setExpr(String fld,String se,Object... ps);
	T setExprIf(String fld,String se,Object... ps);
	
	Map<String,SQL> getValues();
	/**
	 * 是否将表名，字段名用引号引起来
	 * @param b 是否加入括号
	 * @return 当前对象
	 * */
	T quote(boolean b);	
	
	/**
	 * 将表名，字段名用引号引起来<br>
	 * 未调用此方法前，默认无引号
	 * @return 当前对象
	 * */
	T quote();
	
	
	 
}
