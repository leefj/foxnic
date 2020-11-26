package com.github.foxnic.dao.exec;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.sql.SQLParserUtil;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.expr.Update;
import com.github.foxnic.sql.parameter.BatchParamBuilder;

public class DAO_BatchExec extends TableDataTest {

	@Test 
	public void test_1()
	{
		int r=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(r==0);
		
		BatchParamBuilder pb=new BatchParamBuilder();
		for (int i = 0; i < 100; i++) {
			pb.add("A-"+i,15.6*i);
			//System.out.println(i);
		}
		this.dao.batchExecute("insert into "+normalTable+"(code,price) values(?,?)", pb.getBatchList());
		
		 r=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(r==100);
		
		 
		List<SQL> sqls=new ArrayList<>();
		RcdSet rs=dao.query("select * from "+normalTable);
		for (Rcd rcd : rs) {
			rcd.set("title", "TITLE-"+rcd.getString("code"));
			sqls.add(SQLParserUtil.buildUpdate(rcd, SaveMode.ALL_FIELDS));
		}
		int[] zs=dao.batchExecute(sqls);
		for (int i : zs) {
			assertTrue(i==1);
		}
 
		rs=dao.query("select * from "+normalTable);
		for (Rcd rcd : rs) {
			assertTrue(rcd.getString("title").startsWith("TITLE-"));
		}
		
	}
	
}
