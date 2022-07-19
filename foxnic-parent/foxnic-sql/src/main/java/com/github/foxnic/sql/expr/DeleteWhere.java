package com.github.foxnic.sql.expr;

/**
 * 用于delete语句的where子句
 * @author fangjieli
 *
 */
public class DeleteWhere extends ConditionExpression<DeleteWhere>
{

	/**
	 *
	 */
	private static final long serialVersionUID = 5872885640767338898L;

	@Override
	protected SQLKeyword getKeyword() {
		return SQLKeyword.WHERE;
	}


	@Override
	public Delete parent() {
		return (Delete)super.parent();
	}


	@Override
	public Delete top() {
		return (Delete)super.top();
	}

	public DeleteWhere clone() {
		return super.clone(new DeleteWhere());
	}

}
