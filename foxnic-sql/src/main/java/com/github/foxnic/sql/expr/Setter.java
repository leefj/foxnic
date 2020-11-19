package com.github.foxnic.sql.expr;

import java.util.Map;

/**
 * 语句设值置器，Update和Insert语句使用
 * */
public interface Setter<T extends Setter>  {

	public T set(String fld,Object val);
	public T sets(Object... nvs);
	public T setsIf(Object... nvs);
	public T setIf(String fld,Object val);
	public T set(String fld,SQL se);
	public T setExpr(String fld,Expr se);
	public T setExprIf(String fld,Expr se);
	public T setExpr(String fld,String se,Object... ps);
	public T setExprIf(String fld,String se,Object... ps);
	
	public Map<String,SQL> getValues();
	/**
	 * 是否将表名，字段名用引号引起来
	 * @param b 是否加入括号
	 * @return 当前对象
	 * */
	public T quote(boolean b);	
	
	/**
	 * 将表名，字段名用引号引起来<br>
	 * 未调用此方法前，默认无引号
	 * @return 当前对象
	 * */
	public T quote();
	
	
	 
}
