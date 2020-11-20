package com.github.foxnic.sql.expr;

import org.junit.Test;

public class SetterTest {
	
	@Test
	public void aaa() {
		 Setter s=new Update("t");
		 s.set("a", "a").set("b", 4);
	}

}
