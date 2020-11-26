package com.github.foxnic.dao.exec;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.Update;

public class DAO_Transaction  extends TableDataTest {
	
	@Test
	public void test_transaction_commit()
	{
		dao.execute("delete from "+normalTable);
		int r=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(r==0);
		
		int id=18;
		Insert insert=new Insert(normalTable);
		insert.set("ID", id);
		insert.set("price", 108);
		
		Update update=new Update(normalTable);
		update.set("price", 228).where("id=?", id);
 
		this.dao.beginTransaction();
		this.dao.execute(insert);
		this.dao.execute(update);
		this.dao.commit();
		
		r=dao.queryInteger("select price from "+normalTable+" where id=?",id);
		assertTrue(r==228);
 
		
	}
	
	@Test
	public void test_transaction_rollback()
	{
		dao.execute("delete from "+normalTable);
		int r=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(r==0);
		
		int id=18;
		Insert insert=new Insert(normalTable);
		insert.set("ID", id);
		insert.set("price", 108);
		
		Update update=new Update(normalTable);
		update.set("price", 228).where("id=?", id);
 
		this.dao.beginTransaction();
		this.dao.execute(insert);
		this.dao.execute(update);
		this.dao.rollback();
		
		r=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(r==0);
 
	}

}
