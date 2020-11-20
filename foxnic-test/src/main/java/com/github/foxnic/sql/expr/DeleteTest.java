package com.github.foxnic.sql.expr;

import org.junit.Test;

public class DeleteTest {

	@Test
	public void delete_test() {
		Delete del=new Delete("table");
		del.where("id=?", 18).and("deleted=?","Y");
		
		System.out.println(del.getSQL());
		System.out.println(del.getListParameterSQL());
		System.out.println(del.getNamedParameterSQL());
		
	}
	
}
