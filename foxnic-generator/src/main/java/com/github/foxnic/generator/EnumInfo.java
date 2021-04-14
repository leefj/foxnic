package com.github.foxnic.generator;

import com.github.foxnic.sql.expr.ConditionExpr;

public class EnumInfo {
	
	private String dataTable;
	private String nameField;
	private String textField;
	private ConditionExpr conditionExpr;
	
	public String getDataTable() {
		return dataTable;
	}
	public void setDataTable(String dataTable) {
		this.dataTable = dataTable;
	}
	public String getNameField() {
		return nameField;
	}
	public void setNameField(String nameField) {
		this.nameField = nameField;
	}
	public String getTextField() {
		return textField;
	}
	public void setTextField(String textField) {
		this.textField = textField;
	}
	public ConditionExpr getConditionExpr() {
		return conditionExpr;
	}
	public void setConditionExpr(ConditionExpr conditionExpr) {
		this.conditionExpr = conditionExpr;
	}

}
