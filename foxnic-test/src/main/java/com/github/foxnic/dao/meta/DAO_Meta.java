package com.github.foxnic.dao.meta;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.sql.meta.DBDataType;

/**
 * 分页测试
 */
public class DAO_Meta extends TableDataTest {

	@Test
	public void test_meta() {
		//初始化数据
		for (int i = 1; i <= 10; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}

	 
		long t0=System.currentTimeMillis();
		String[] tables=dao.getTableNames();
		long t1=System.currentTimeMillis();
		assertTrue(tables.length>0);
		
		tables=dao.getTableNames();
		long t2=System.currentTimeMillis();
		dao.refreshMeta();
		tables=dao.getTableNames();
		long t3=System.currentTimeMillis();
		
		System.out.println("MC  "+(t2-t1)+","+(t3-t2));
		assertTrue((t2-t1)==0 && (t2-t1)<=(t3-t2));
		
		DBTableMeta tm=dao.getTableMeta(normalTable);
		String topic=tm.getTopic();
		assertTrue("新闻".equals(topic));
		assertTrue(tm!=null);
		assertTrue(tm.getComments()!=null);
		assertTrue(tm.getComments().length()>0);
		
		//列元数据
		
		List<DBColumnMeta> cms=tm.getColumns();
		assertTrue(cms.size()>5);
		for (DBColumnMeta cm : cms) {
			if(cm.getComment()==null) 
			{
				System.out.println();
			}
			 assertTrue(cm.getComment()!=null);
			 assertTrue(cm.getComment().length()>0);
			 assertTrue(cm.getDetail().length()>0);
			 
			 if(cm.getDBDataType()==DBDataType.STRING) {
				 assertTrue(cm.getCharLength()>0);
				 assertTrue(cm.getCharLength()<=cm.getDataLength());
			 }
		}
		
	}

}
