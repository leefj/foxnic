package com.github.foxnic.sql.data;

import java.io.Serializable;

public abstract class AbstractRcdSet  implements Iterable<AbstractRcd>, Serializable {

	public abstract AbstractRcd find(String string, Object string2);

	public abstract void changeColumnLabel(String string, String string2);

	public abstract int size();

	public abstract void add(AbstractRcd r);

}
