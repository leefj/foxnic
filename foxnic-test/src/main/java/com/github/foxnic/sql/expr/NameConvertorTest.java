package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;

public class NameConvertorTest {

	@Test
	public void nc_test() {
		
		DefaultNameConvertor nc=new DefaultNameConvertor();
		String n=nc.getClassName("org_dept", 1);
		assertTrue("Dept".equals(n));
		n=nc.getClassName("org_dept", 0);
		assertTrue("OrgDept".equals(n));
		n=nc.getClassName("org_dept", 2);
		assertTrue("OrgDept".equals(n));
	}
	
}
