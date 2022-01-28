package com.github.foxnic.dao.relation;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.expr.SQL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JoinResult<S extends Entity,T extends Entity> {

	private List<SQL> statements;

	private Collection<S> sourceList;

	private Collection<T> targetList;

	private Class<T> sourceType;

	private Class<T> targetType;

	private String sourceTable;

	private String targetTable;

	private RcdSet targetRecords;

	private PropertyRoute<S, T> propertyRoute;

	private Join[] joinPath;

	public List<SQL> getStatements() {
		return statements;
	}

	public void addStatement(SQL statement) {
		if(this.statements==null) this.statements=new ArrayList<>();
		this.statements.add(statement);
	}

	public Collection<S> getSourceList() {
		return sourceList;
	}

	public void setSourceList(Collection<S> sourceList) {
		this.sourceList = sourceList;
	}

	public RcdSet getTargetRecords() {
		return targetRecords;
	}

	public void setTargetRecords(RcdSet rcdSet) {
		this.targetRecords = rcdSet;
	}

	public PropertyRoute<S, T> getPropertyRoute() {
		return propertyRoute;
	}

	public void setPropertyRoute(PropertyRoute<S, T> propertyRoute) {
		this.propertyRoute = propertyRoute;
	}

	public Join[] getJoinPath() {
		return joinPath;
	}

	public void setJoinPath(Join[] joinPath) {
		this.joinPath = joinPath;
	}

	public JoinResult<S,T> merge(JoinResult<S,T> rightResult) {
		this.sourceList.addAll(rightResult.getSourceList());
		this.targetList.addAll(rightResult.getTargetList());
		// 从换缓存里来的，没有记录集无需合并
		if(this.targetRecords!=null && rightResult.targetRecords!=null) {
			for (Rcd r : rightResult.targetRecords) {
				this.targetRecords.add(r);
			}
		}
		this.statements.addAll(rightResult.getStatements());
		return this;
	}

	public Class<T> getSourceType() {
		return sourceType;
	}

	public void setSourceType(Class<T> sourceType) {
		this.sourceType = sourceType;
	}

	public Class<T> getTargetType() {
		return targetType;
	}

	public void setTargetType(Class<T> targetType) {
		this.targetType = targetType;
	}

	public void setTargetList(Collection<T> targetList) {
		this.targetList = targetList;
	}

	public String getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public String getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}

	public Collection<T> getTargetList() {
		return targetList;
	}

}
