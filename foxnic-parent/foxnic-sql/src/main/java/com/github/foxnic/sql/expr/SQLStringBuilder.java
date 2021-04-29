package com.github.foxnic.sql.expr;

/**
 * 使用统一的格式构建SQL语句
 */
public class SQLStringBuilder {

	private static final char SPACE_CHAR = ' ';
	private StringBuilder stmt = new StringBuilder();

	public SQLStringBuilder append(SQLKeyword... kws) {
		boolean space=true;
		for (SQLKeyword kw : kws) {
			space=true;
 
			if(stmt.length()==0) {
				space=false;
			} else {
				if(SPACE_CHAR==stmt.charAt(stmt.length()-1)) {
					space=false;
				}
			}
 
			
			if(space) {
				stmt.append(SQLKeyword.SPACER.toString());
			}
			stmt.append(kw.toString());
		}
		return this;
	}

	public SQLStringBuilder append(String... subs) {
		String sub = null;
 
		boolean space=true;
		for (int i = 0; i < subs.length; i++) {
			sub = subs[i];
			if (sub == null || sub.trim().isEmpty()) {
				continue;
			}
			sub=sub.trim();
			if(sub.isEmpty()) {
				continue;
			}
			
			space=true;
			if(stmt.length()==0) {
				space=false;
			} else {
				if(SPACE_CHAR==stmt.charAt(stmt.length()-1)) {
					space=false;
				}
			}
			
			if(space) {
				stmt.append(SQLKeyword.SPACER.toString());
			}
 
			stmt.append(sub);
 
		}
		return this;
	}

	@Override
	public String toString() {
		return stmt.toString();
	}
	
	public SQLStringBuilder deleteLastChar(int chars)
	{
		if(chars<=0) return this;
		if(chars>this.stmt.length()) chars=this.stmt.length();
		this.stmt.delete(this.stmt.length()-chars, this.stmt.length());
		return this;
	}
	
	public int length()
	{
		return this.stmt.length();
	}

}
