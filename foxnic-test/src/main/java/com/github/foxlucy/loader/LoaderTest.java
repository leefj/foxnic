package com.github.foxlucy.loader;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spring.MySqlDAO;
import com.github.foxnic.dao.sql.loader.SQLoader;

/**
 * 分页测试
 */
public class LoaderTest extends TableDataTest {

	@Test
	public void test_meta() {
		
		SQLoader.setTQLScanPackage(this.dao,"com.github.foxlucy");
		
		String sql=this.dao.getSQL("#query_1");
		System.out.println("sql="+sql);
		assertTrue("select 1".equals(sql));
		
		//性能测试  10000条 500毫秒
		long t=System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			sql=this.dao.getSQL("#query_1");
		}
		System.out.println(System.currentTimeMillis()-t);
		
		
	 
 
	}

}
