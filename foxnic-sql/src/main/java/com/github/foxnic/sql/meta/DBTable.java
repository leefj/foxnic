package com.github.foxnic.sql.meta;

import java.util.Arrays;
import java.util.List;

public class DBTable {

	private String name;
	private String comment;
	private List<DBField> fields;
	
	protected void init(String name,String comment,DBField... fields) {
		this.name=name;
		this.comment=comment;
		this.fields=Arrays.asList(fields);
		for (DBField dbField : fields) {
			dbField.setTable(this);
		}
	}
	
	public String name() {
		return name;
	}
	
	public String comment() {
		return comment;
	}
	
	public List<DBField> fields() {
		return fields;
	}
 
}
