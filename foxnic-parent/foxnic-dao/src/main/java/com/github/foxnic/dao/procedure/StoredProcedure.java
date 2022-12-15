package com.github.foxnic.dao.procedure;

import com.github.foxnic.sql.meta.DBDataType;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 李方捷
 * StoredProcedure 接口方面尚有更多提升空间，逐步优化
 * */
public class StoredProcedure {

	private ArrayList parameters = new ArrayList();

	private class InnerProcedure extends org.springframework.jdbc.object.StoredProcedure {
		InnerProcedure(DataSource dataSource, String procedureName) {
			super(dataSource, procedureName);
		}
	}


	/**
	 * 声明参数，并设置参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareParameter(String name, DBDataType type) {
		SqlParameter p = new SqlParameter(name, type.getDefaultJDBCType());
		parameters.add(p);
		return this;
	}

	/**
	 * 声明OUT参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareOutParameter(String name, DBDataType type) {
		SqlOutParameter p = new SqlOutParameter(name, type.getDefaultJDBCType());
		parameters.add(p);
		return this;
	}


	/**
	 * 声明IN_OUT参数
	 * @param name 参数名称
	 * @param type 参数类型
	 * @return StoredProcedure
	 * */
	public StoredProcedure declareInOutParameter(String name, DBDataType type) {
		SqlInOutParameter p = new SqlInOutParameter(name, type.getDefaultJDBCType());
		parameters.add(p);
		return this;
	}

	private DataSource dataSource = null;
	private String procedureName = null;

	/**
	 * 当有多个参数时，这个函数貌似有问题
	 * @param dataSource 数据源
	 * @param procedureName 函数或过程的名称
	 * */
	public StoredProcedure(DataSource dataSource, String procedureName) {
		this.procedureName = procedureName;
		this.dataSource = dataSource;
	}

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
		InnerProcedure procedure = new InnerProcedure(dataSource, procedureName);
		for (Object p : parameters) {
			if(p instanceof SqlParameter) {
				procedure.declareParameter((SqlParameter) p);
			} else if(p instanceof SqlOutParameter) {
				procedure.declareParameter((SqlOutParameter) p);
			} else if(p instanceof SqlInOutParameter) {
				procedure.declareParameter((SqlInOutParameter) p);
			}
		}
		procedure.setFunction(false);
		procedure.compile();
		HashMap<String, Object> map = (HashMap<String, Object>) procedure.execute(params);
		return map;
	}

}
