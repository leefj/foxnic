package com.github.foxnic.sql.treaty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.meta.DBDataType;

 
/**
 * 针对数据库字段的一些约定，数据库设计规约
 * @author 李方捷
 * */
public class DBTreaty {

	/**
	 *  版本字段
	 * */
	private String versionField="version_num";
	
	public String getVersionField() {
		return versionField;
	}


	public void setVersionField(String versionField) {
		this.versionField = versionField;
	}
 
	/**
	 * 删除标记字段名
	 * */
	private String deletedFlagField="deleted";
	
	
	public String getDeletedField() {
		return deletedFlagField;
	}


	public void setDeletedField(String deletedFlagField) {
		this.deletedFlagField = deletedFlagField;
	}
	
	private ArrayList<Object[]> logicFieldPattens=new ArrayList<>();
	
	/**
	 * 设置逻辑字段样式，符合样式的将被识别为逻辑字段
	 * @param starts 开头的字符，不区分大小写
	 * @param dataLength 字段长度
	 * */
	public void addLogicFieldPatten(String starts,Integer dataLength)
	{
		logicFieldPattens.add(new Object[] {starts.toUpperCase(),dataLength});
	}
	
	/**
	 * 设置逻辑字段样式，符合样式的将被识别为逻辑字段
	 * @param starts 开头的字符，不区分大小写
	 * */
	public void addLogicFieldPatten(String starts)
	{
		logicFieldPattens.add(new Object[] {starts.toUpperCase(),0});
	}
	
	private boolean autoCastLogicField = true; 
	
	/**
	 * 判断是否为逻辑字段
	 * */
	public boolean isLogicField(String column,int dataLength)
	{
		if(!autoCastLogicField) return false;
		String name=column.toUpperCase();
		boolean logic=name.equalsIgnoreCase(this.getDeletedField());
		if(logic) return logic;
		String starts=null;
		Integer dataLengthPatten=null;
		for (Object[] nv : logicFieldPattens) {
			starts=(String)nv[0];
			dataLengthPatten=(Integer)nv[1];
			if(dataLengthPatten>0) {
				logic=name.startsWith(starts) && dataLength==dataLengthPatten;
			} else  {
				logic=name.startsWith(starts);
			}
			if(logic) return logic;
		}
		return false;
	}

	/**
	 * 数据库中用于true的字面量
	 * @return 值
	 * */
	public Object getTrueValue() {
		return trueValue;
	}
 
	/**
	 * 数据库中用于true的字面量
	 * @param trueValue true值
	 * */
	public void setTrueValue(Object trueValue) {
		if(trueValue instanceof CharSequence || trueValue instanceof Number)
		{
			this.trueValue = trueValue;
		}
		else
		{
			throw new IllegalArgumentException("参数类型错误，要求 CharSequence 或 Number 类型");
		}
	}


	/**
	 * false的数据库值
	 * @return false值
	 * */
	public  Object getFalseValue() {
		return falseValue;
	}


	/**
	 * 设置false值
	 * @param falseValue false值
	 * */
	public void setFalseValue(Object falseValue) {
		if(trueValue instanceof CharSequence || trueValue instanceof Number)
		{
			this.falseValue = falseValue;
		}
		else
		{
			throw new IllegalArgumentException("参数类型错误，要求 CharSequence 或 Number 类型");
		}
	}

	/**
	 * 创建人ID字段名
	 * @return 字段名
	 * */
	public String getCreateUserIdField() {
		return createUserIdField;
	}

	/**
	 * 创建人ID字段名
	 * @param createUserIdField 字段名
	 * */
	public void setCreateUserIdField(String createUserIdField) {
		this.createUserIdField = createUserIdField;
	}
	

	/**
	 * 创建时间字段名
	 * @return 字段名
	 * */
	public String getCreateTimeField() {
		return createTimeField;
	}

	/**
	 * 创建时间字段名
	 * @param createTimeField 字段名
	 * */
	public void setCreateTimeField(String createTimeField) {
		this.createTimeField = createTimeField;
	}

	/**
	 * 更新人ID字段名
	 * @return 字段名
	 * */
	public String getUpdateUserIdField() {
		return updateUserIdField;
	}

	/**
	 * 更新人ID字段名
	 * @param updateUserIdField 字段名
	 * */
	public void setUpdateUserIdField(String updateUserIdField) {
		this.updateUserIdField = updateUserIdField;
	}

	/**
	 * 更新时间字段名
	 * @return 字段名
	 * */
	public String getUpdateTimeField() {
		return updateTimeField;
	}

	/**
	 * 更新时间字段名
	 * @param updateTimeField 字段名
	 * */
	public void setUpdateTimeField(String updateTimeField) {
		this.updateTimeField = updateTimeField;
	}


	/**
	 * 删除人ID字段名
	 * @return 字段名
	 * */
	public String getDeleteUserIdField() {
		return deleteUserIdField;
	}

	/**
	 * 删除人ID字段名
	 * @param deleteUserIdField 字段名
	 * */
	public void setDeleteUserIdField(String deleteUserIdField) {
		this.deleteUserIdField = deleteUserIdField;
	}

	/**
	 * 删除时间字段名
	 * @return 字段名
	 * */
	public String getDeleteTimeField() {
		return deleteTimeField;
	}

	/**
	 * 删除时间字段名
	 * @param deleteTimeField 字段名
	 * */
	public void setDeleteTimeField(String deleteTimeField) {
		this.deleteTimeField = deleteTimeField;
	}



	private Object trueValue="Y";
	/**
	 *数据库中用于false的字面量
	 * */
	private Object falseValue="N";
 
	private String createUserIdField="create_by";
	
	private String createTimeField="create_time";
 
	private String updateUserIdField="update_by";
 
	private String updateTimeField="update_time";
 
	private String deleteUserIdField="delete_by";
 
	private String deleteTimeField="delete_time";
	
	
	/**
	 * 获得用于Deleted判断的SQL语句，以AND
	 * @param hasDeletedField  是否有deleted字段
	 * @param deleted 是否删除的逻辑值
	 * @return CE 
	 * */
	public ConditionExpr getDeletedCE(boolean hasDeletedField,boolean deleted)
	{
		if(hasDeletedField)
		{
			return new ConditionExpr(this.deletedFlagField+" = ?",deleted?trueValue:falseValue);
		}
		else
		{
			return new ConditionExpr();
		}
	}
	
	private String[] fields=null;
	
	/**
	 * 返回所有内部字段
	 * @return 字符结合
	 * */
	public String[] getFields()
	{
		if(fields==null) {
			fields=new String[] {createUserIdField,createTimeField,updateUserIdField,updateTimeField,deleteUserIdField,deleteTimeField,deletedFlagField};
		}
		return fields;
	}
	
	private Set<String> excludeTables= new HashSet<String>();
	
	/**
	 * 加入规则以外的表
	 * @param table 表名
	 * */
	public void addExcludeTable(String... table)
	{
		for (String t : table) {
			excludeTables.add(t.trim().toUpperCase());
		}
	}
	
	/**
	 * 判断是否规则外的表
	 * @param table 表名
	 * @return 逻辑值
	 * */
	public boolean isExcludeTable(String table)
	{
		table=table.trim().toUpperCase();
		return excludeTables.contains(table);
	}
	
	boolean isAllowUpdateWithoutWhere=false;
	boolean isAllowDeleteWithoutWhere=false;
	
	/**
	 * 执行Update语句时是否允许没有where条件，默认不允许，必须有 where
	 * @return 逻辑值
	 * */
	public boolean isAllowUpdateWithoutWhere() {
		return isAllowUpdateWithoutWhere;
	}

	/**
	 * 执行Update语句时是否允许没有where条件，默认不允许，必须有 where
	 * @param isAllowUpdateWithoutWhere 逻辑值
	 * */
	public void setAllowUpdateWithoutWhere(boolean isAllowUpdateWithoutWhere) {
		this.isAllowUpdateWithoutWhere = isAllowUpdateWithoutWhere;
	}


	/**
	 * 执行Delete语句时是否允许没有where条件，默认不允许，必须有 where
	 * @return 逻辑值
	 * */
	public boolean isAllowDeleteWithoutWhere() {
		return isAllowDeleteWithoutWhere;
	}

	/**
	 * 执行Delete语句时是否允许没有where条件，默认不允许，必须有 where
	 * @param isAllowDeleteWithoutWhere 逻辑值
	 * */
	public void setAllowDeleteWithoutWhere(boolean isAllowDeleteWithoutWhere) {
		this.isAllowDeleteWithoutWhere = isAllowDeleteWithoutWhere;
	}
	
	
	private DBDataType userIdDataType=null;

	public DBDataType getUserIdDataType() {
		return userIdDataType;
	}

	/**
	 * 设置UserId字段的类型，创建人，修改人，删除人
	 * */
	public void setUserIdDataType(DBDataType userIdDataType) {
		this.userIdDataType = userIdDataType;
	}


	public boolean isAutoCastLogicField() {
		return autoCastLogicField;
	}


	public void setAutoCastLogicField(boolean autoCastLogicField) {
		this.autoCastLogicField = autoCastLogicField;
	}
	 
	
}
