package com.github.foxnic.dao.data;

import java.io.Serializable;

public abstract class RcdSet  implements Iterable<Rcd>, Serializable {

	public abstract Rcd find(String string, Object string2);

	public abstract void changeColumnLabel(String string, String string2);

	public abstract int size();

	public abstract void add(Rcd r);

}
