package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.dialect.SQLDialect;

public class InsertTest {

	@Test
	public void insert_1() {
		
		
		
		// 创建一个插入语句
		Insert insert = new Insert();
		// 设置值
		insert.into("sometable").set("id", 1).set("name", "leefj");
		// 设置SQL表达式为值
		insert.setExpr("birthday", "now()");
		
		
		System.out.println(insert.getSQL());
		System.out.println(insert.getListParameterSQL());
		System.out.println(insert.getNamedParameterSQL());
		
		assertTrue("INSERT INTO sometable ( id , name , birthday ) VALUES ( 1 , 'leefj' , now() )".equals(insert.getSQL()));
		assertTrue("INSERT INTO sometable ( id , name , birthday ) VALUES ( ? , ? , now() )".equals(insert.getListParameterSQL()));
		assertTrue("INSERT INTO sometable ( id , name , birthday ) VALUES ( :PARAM_1 , :PARAM_2 , now() )".equals(insert.getNamedParameterSQL()));
		 
		
		GlobalSettings.DEFAULT_SQL_DIALECT=SQLDialect.MySQL;
		insert.quote(true);
		
		System.out.println(insert.getSQL());
		System.out.println(insert.getListParameterSQL());
		System.out.println(insert.getNamedParameterSQL());
		
		assertTrue("INSERT INTO `sometable` ( `id` , `name` , `birthday` ) VALUES ( 1 , 'leefj' , now() )".equals(insert.getSQL()));
		assertTrue("INSERT INTO `sometable` ( `id` , `name` , `birthday` ) VALUES ( ? , ? , now() )".equals(insert.getListParameterSQL()));
		assertTrue("INSERT INTO `sometable` ( `id` , `name` , `birthday` ) VALUES ( :PARAM_1 , :PARAM_2 , now() )".equals(insert.getNamedParameterSQL()));
		
		GlobalSettings.DEFAULT_SQL_DIALECT=SQLDialect.PLSQL;
		insert.quote(true);
		
		System.out.println(insert.getSQL());
		System.out.println(insert.getListParameterSQL());
		System.out.println(insert.getNamedParameterSQL());
		
		assertTrue("INSERT INTO \"sometable\" ( \"id\" , \"name\" , \"birthday\" ) VALUES ( 1 , 'leefj' , now() )".equals(insert.getSQL()));
		assertTrue("INSERT INTO \"sometable\" ( \"id\" , \"name\" , \"birthday\" ) VALUES ( ? , ? , now() )".equals(insert.getListParameterSQL()));
		assertTrue("INSERT INTO \"sometable\" ( \"id\" , \"name\" , \"birthday\" ) VALUES ( :PARAM_1 , :PARAM_2 , now() )".equals(insert.getNamedParameterSQL()));

	}

	@Test
	public void normal() {

		Insert ins = new Insert("User");
		ins.set("id", 9);
		ins.set("name", null);
		ins.setIf("addr", null);
		ins.setExpr("nic_name","nvl(name,?)", "leefj");
 
		System.out.println(ins.getSQL());
		System.out.println(ins.getListParameterSQL());
		System.out.println(ins.getNamedParameterSQL());
		
		assertTrue("INSERT INTO User ( id , name , nic_name ) VALUES ( 9 , null , nvl(name, 'leefj' ) )".equals(ins.getSQL()));
		assertTrue("INSERT INTO User ( id , name , nic_name ) VALUES ( ? , ? , nvl(name, ? ) )".equals(ins.getListParameterSQL()));
		assertTrue("INSERT INTO User ( id , name , nic_name ) VALUES ( :PARAM_1 , :PARAM_2 , nvl(name, :PARAM_3 ) )".equals(ins.getNamedParameterSQL()));
		
	}

}
