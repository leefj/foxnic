package com.github.foxnic.dao.procedure;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

/**
 * @author 李方捷
 * */
public class StoredProcedure {

	private ArrayList<SqlParameter> parameters = new ArrayList<SqlParameter>();
	private ArrayList<SqlOutParameter> outParameters = new ArrayList<SqlOutParameter>();
	private ArrayList<SqlInOutParameter> inOutParameters = new ArrayList<SqlInOutParameter>();

	private class InnerProcedure extends org.springframework.jdbc.object.StoredProcedure {
		InnerProcedure(DataSource dataSource, String procedureName) {
			super(dataSource, procedureName);
		}
	}
	
	private HashMap<String, Object> parameterValues=null;
	
	 
	/**
	 * 声明参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareParameter(String name, int type) {
		return declareParameter(name, type, null);
	}
	
	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareParameter(String name, int type,Object value) {
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
	public StoredProcedure declareVarcharParameter(String name) {
		return declareParameter(name, Types.VARCHAR, null);
	}
	
	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareVarcharParameter(String name,String value) {
		return declareParameter(name, Types.VARCHAR, value);
	}
	
	/**
	 * 声明参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareNumericParameter(String name) {
		return declareParameter(name, Types.NUMERIC, null);
	}

	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareNumericParameter(String name,Number value) {
		return declareParameter(name, Types.NUMERIC, value);
	}
	
	
	/**
	 * 声明参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareDateParameter(String name) {
		return declareParameter(name, Types.DATE, null);
	}
	
	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareDateParameter(String name,Date value) {
		return declareParameter(name, Types.DATE, value);
	}
	
	
	
	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareOutParameter(String name, int type) {
		return declareOutParameter(name, type, null);
	}

	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareOutParameter(String name, int type,Object value) {
		SqlOutParameter p = new SqlOutParameter(name, type);
		outParameters.add(p);
		if(value!=null)
		{
			if (parameterValues==null) parameterValues=new HashMap<String, Object>();
			parameterValues.put(name, value);
		}
		return this;
	}
	
	
	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareVarcharOutParameter(String name) {
		return declareOutParameter(name, Types.VARCHAR, null);
	}

	/**
	 * 声明OUT参数
	 * 	@param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareVarcharOutParameter(String name,String value) {
		return declareOutParameter(name, Types.VARCHAR, value);
	}
	
	
	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareNumericOutParameter(String name) {
		return declareOutParameter(name, Types.NUMERIC, null);
	}

	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareNumericOutParameter(String name,Number value) {
		return declareOutParameter(name, Types.NUMERIC, value);
	}
	
	
	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareDateOutParameter(String name) {
		return declareOutParameter(name, Types.DATE, null);
	}

	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareDateOutParameter(String name,Date value) {
		return declareOutParameter(name, Types.DATE, value);
	}
 
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareInOutParameter(String name, int type) {
		return declareInOutParameter(name, type, null);
	}
	
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareInOutParameter(String name, int type,Object value) {
		SqlInOutParameter p = new SqlInOutParameter(name, type);
		inOutParameters.add(p);
		if(value!=null)
		{
			if (parameterValues==null) parameterValues=new HashMap<String, Object>();
			parameterValues.put(name, value);
		}
		return this;
	}
	
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareNumericInOutParameter(String name) {
		return declareInOutParameter(name, Types.NUMERIC, null);
	}
	
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareNumericInOutParameter(String name,Number value) {
		return declareInOutParameter(name, Types.NUMERIC, value);
	}
	
	
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareDateInOutParameter(String name) {
		return declareInOutParameter(name, Types.DATE, null);
	}
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareDateInOutParameter(String name,Date value) {
		return declareInOutParameter(name, Types.DATE, value);
	}
 
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareVarcharInOutParameter(String name) {
		return declareInOutParameter(name, Types.VARCHAR, null);
	}
	
	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @param value 参数值
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareVarcharInOutParameter(String name,String value) {
		return declareInOutParameter(name, Types.VARCHAR, value);
	}
	

	private DataSource dataSource = null;
	private String procedureName = null;
	private boolean isFunction = false;

	/**
	 * 当有多个参数时，这个函数貌似有问题
	 * @param dataSource 数据源
	 * @param procedureName 函数或过程的名称
	 * @param isFunction 是否函数
	 * */
	public StoredProcedure(DataSource dataSource, String procedureName,
			boolean isFunction) {
		this.procedureName = procedureName;
		this.isFunction = isFunction;
		this.dataSource = dataSource;
	}

	protected Object ret = null;
	
	/**
	 * 执行
	 * @return 返回值和out参数的集合
	 * */
	public Map<String, Object> execute() {
		return execute(null);
	}

	/**
	 * 执行
	 * @param params 参数
	 * @return 返回值和out参数的集合
	 * */
	public Map<String, Object> execute(Map<String, Object> params) {
		if(params==null) params=parameterValues;
		InnerProcedure procedure = new InnerProcedure(dataSource, procedureName);
		for (SqlParameter p : parameters) {
			procedure.declareParameter(p);
		} 
		for (SqlOutParameter p : outParameters) {
			procedure.declareParameter(p);
		}
		for (SqlInOutParameter p : inOutParameters) {
			procedure.declareParameter(p);
		}
		procedure.setFunction(isFunction);

		procedure.compile();
		HashMap<String, Object> map = (HashMap<String, Object>) procedure.execute(params);
		return map;
	}

}
