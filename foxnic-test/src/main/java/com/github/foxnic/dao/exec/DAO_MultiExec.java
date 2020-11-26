package com.github.foxnic.dao.exec;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.Update;

public class DAO_MultiExec extends TableDataTest {

	@Test 
	public void test_1()
	{
	//	this.dao.beginTransaction();
		int id=5;
		Insert insert=new Insert(normalTable);
		insert.set("ID", id);
		insert.set("price", 108);
		
		Update update=new Update(normalTable);
		update.set("price", 228).where("id=?", id);
		
		int r=this.dao.multiExecute(insert,update);
		assertTrue(r==2);
		
		r=dao.queryInteger("select price from "+normalTable+" where id=?",id);
		assertTrue(r==228);
		
		
		r=this.dao.multiExecute("update "+normalTable+" set price=16 where id=5","delete from "+normalTable);
		
		assertTrue(r==2);
		
		r=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(r==0);
		
	 
 
	}
	
}
