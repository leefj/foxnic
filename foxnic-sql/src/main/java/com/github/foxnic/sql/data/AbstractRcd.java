package com.github.foxnic.sql.data;

public abstract class AbstractRcd {

	public abstract void set(String string, String string2);

	public abstract String getString(String string);

	public abstract AbstractRcd clone();
	
}
