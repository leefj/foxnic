package com.github.foxnic.generator.config;

import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.meta.DBField;

public class EnumConfig {


	private DBField codeField;
	private DBField textField;
	private ConditionExpr conditionExpr;

	public EnumConfig(DBField codeField,DBField textField,ConditionExpr conditionExpr) {
		this.codeField=codeField;
		this.textField=textField;
		this.conditionExpr=conditionExpr;
	}

	public Expr getSelect() {
		Expr expr=new Expr("select distinct "+codeField.name()+","+textField.name()+" from "+codeField.table().name());
		if(conditionExpr!=null) {
			conditionExpr.startWithWhere();
			expr.append(conditionExpr);
		}
		return expr;
	}

	public String getCodeField() {
		return codeField.name();
	}

	public String getTextField() {
		return textField.name();
	}



}
