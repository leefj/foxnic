package com.github.foxnic.sql.meta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBTable {
 
	private String schema;
	private String name;
	private String comment;
	private Map<String,DBField> fields;
	
	protected void init(String name,String comment,DBField... fields) {
		this.init("default", name, comment, fields);
	}
	
	protected void init(String schema,String name,String comment,DBField... fields) {
		this.schema=schema;
		this.name=name;
		this.comment=comment;
		this.fields=new HashMap<>();
		for (DBField dbField : fields) {
			dbField.setTable(this);
			this.fields.put(dbField.name().toUpperCase(), dbField);
		}
	}
	
	public String name() {
		return name;
	}
	
	public String comment() {
		return comment;
	}
	
	public List<DBField> fields() {
		return Arrays.asList(fields.values().toArray(new DBField[0]));
	}

	public String getSchema() {
		return schema;
	}
	
	@Override
	public String toString() {
		return this.name();
	}

	public DBField getField(String column) {
		if(column==null) return null;
		return fields.get(column.toUpperCase());
	}
 
}
