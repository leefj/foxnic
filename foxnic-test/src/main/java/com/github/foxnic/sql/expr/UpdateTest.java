package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

import com.github.foxnic.sql.dialect.SQLDialect;

public class UpdateTest {

	@Test
	public void doTest()
	{
		DateTime dt=new DateTime(2019,11,22,11,11,11);
		Date date=dt.toDate();
		//创建一个Delete语句
		Update update=new Update("sometable");
		update.set("f1", 99).set("f2",date).setExpr("create_time", "sysdate");
		//设置值
		update.where().and("id=?",8);
		// 输出 UPDATE sometable SET id=1,name='leefj',birthday=now()  WHERE  id= 8 
		System.out.println(update.getSQL(SQLDialect.PLSQL));
		System.out.println(update.getSQL(SQLDialect.MySQL));
		
		System.out.println(update.getNamedParameterSQL());
		System.out.println(update.getListParameterSQL());
		
		assertTrue("UPDATE sometable SET f1 = 99 , f2 = to_date('2019-11-22 11:11:11','yyyy-mm-dd hh24:mi:ss') , create_time = sysdate WHERE id= 8".equals(update.getSQL(SQLDialect.PLSQL)));
		assertTrue("UPDATE sometable SET f1 = 99 , f2 = str_to_date('2019-11-22 11:11:11','%Y-%m-%d %H:%i:%s') , create_time = sysdate WHERE id= 8".equals(update.getSQL(SQLDialect.MySQL)));
		assertTrue("UPDATE sometable SET f1 = :PARAM_1 , f2 = :PARAM_2 , create_time = sysdate WHERE id= :PARAM_3".equals(update.getNamedParameterSQL()));
		assertTrue("UPDATE sometable SET f1 = ? , f2 = ? , create_time = sysdate WHERE id= ?".equals(update.getListParameterSQL()));
		
	}
	
}
