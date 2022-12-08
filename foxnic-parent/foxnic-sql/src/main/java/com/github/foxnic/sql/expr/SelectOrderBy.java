package com.github.foxnic.sql.expr;

public class SelectOrderBy extends OrderBy<SelectOrderBy> {

	/**
	 *
	 */
	private static final long serialVersionUID = -8121419857278599732L;

	@Override
	public Select top() {
		return (Select)super.top();
	}

	public GroupBy groupBy() {
		return this.top().groupBy();
	}

	public SelectWhere where() {
		return this.top().where();
	}

	public SelectOrderBy clone() {
		return super.clone(new SelectOrderBy());
	}

}
