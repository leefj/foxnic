package com.github.foxnic.dao.procedure;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 李方捷
 * StoredFunction 接口方面尚有更多提升空间，逐步优化
 * */
public class StoredFunction {

	private ArrayList<SqlParameter> parameters = new ArrayList<SqlParameter>();




	private HashMap<String, Object> parameterValues=null;


	/**
	 * 声明参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredFunction declareParameter(String name, int type) {
		return declareParameter(name, type, null);
	}

	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredFunction declareParameter(String name, int type, Object value) {
		SqlParameter p = new SqlParameter(name, type);
		parameters.add(p);
		if(value!=null)
		{
			if (parameterValues==null) parameterValues=new HashMap<String, Object>();
			parameterValues.put(name, value);
		}
		return this;
	}


	/**
	 * 声明参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredFunction declareVarcharParameter(String name) {
		return declareParameter(name, Types.VARCHAR, null);
	}

	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredFunction declareVarcharParameter(String name, String value) {
		return declareParameter(name, Types.VARCHAR, value);
	}

	/**
	 * 声明参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredFunction declareNumericParameter(String name) {
		return declareParameter(name, Types.NUMERIC, null);
	}

	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredFunction declareNumericParameter(String name, Number value) {
		return declareParameter(name, Types.NUMERIC, value);
	}


	/**
	 * 声明参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredFunction declareDateParameter(String name) {
		return declareParameter(name, Types.DATE, null);
	}

	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredFunction declareDateParameter(String name, Date value) {
		return declareParameter(name, Types.DATE, value);
	}

	private DataSource dataSource = null;
	private String procedureName = null;


	/**
	 * 当有多个参数时，这个函数貌似有问题
	 * @param dataSource 数据源
	 * @param procedureName 函数或过程的名称
	 * */
	public StoredFunction(DataSource dataSource, String procedureName) {
		this.procedureName = procedureName;
		this.dataSource = dataSource;
	}

	protected Object ret = null;

	/**
	 * 执行
	 * @return 返回值和out参数的集合
	 * */
	public <T> T execute(Class<T> returnType) {
		return execute(new HashMap<>(),returnType);
	}

	/**
	 * 执行
	 * @param params 参数
	 * @return 返回值和out参数的集合
	 * */
	public <T> T execute(Map<String, Object> params,Class<T> returnType) {
		if(params==null) params=parameterValues;
		SimpleJdbcCall procedure = new SimpleJdbcCall(dataSource);
		procedure.withFunctionName(this.procedureName);
		procedure.declareParameters(parameters.toArray(new SqlParameter[0]));
		procedure.setFunction(true);
		procedure.compile();
		T result= procedure.executeFunction(returnType,params);
		return result;
	}

}
