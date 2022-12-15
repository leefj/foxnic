package com.github.foxnic.dao.procedure;

import com.github.foxnic.sql.meta.DBDataType;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author 李方捷
 * StoredFunction 接口方面尚有更多提升空间，逐步优化
 * */
public class StoredFunction<T> {

	public static final String RESULT = "result";

	private class InnerProcedure extends org.springframework.jdbc.object.StoredProcedure {
		InnerProcedure(DataSource dataSource, String procedureName) {
			super(dataSource, procedureName);
		}
	}

	private DataSource dataSource = null;
	private String name = null;
	private String catalog = null;

	private Class<T> returnType = null;

	/**
	 * 当有多个参数时，这个函数貌似有问题
	 * @param dataSource 数据源
	 * @param name 函数或过程的名称
	 * */
	public StoredFunction(DataSource dataSource, String name,Class<T> returnType) {
		this.name=name.trim();
		this.dataSource = dataSource;
		this.returnType = returnType;
	}

	/**
	 * 调用函数
	 * @param params 参数
	 * @return 返回值和out参数的集合
	 * */
	public T execute(Object... params) {
		int jdbcType= DBDataType.parseFromType(this.returnType).getDefaultJDBCType();
		InnerProcedure procedure = new InnerProcedure(dataSource, name);
		procedure.declareParameter(new SqlOutParameter(RESULT, jdbcType));
		int i=0;
		for (Object param : params) {
			jdbcType = DBDataType.parseFromType(param.getClass()).getDefaultJDBCType();
			procedure.declareParameter(new SqlParameter("P"+i,jdbcType));
		}
		procedure.setFunction(true);
		procedure.compile();
		Map<String, Object> map =   procedure.execute(params);
		return (T)map.get(RESULT);
	}

}
