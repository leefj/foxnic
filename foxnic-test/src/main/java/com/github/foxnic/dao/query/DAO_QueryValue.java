package com.github.foxnic.dao.query;

import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spring.Db2DAO;
import com.github.foxnic.dao.spring.MySqlDAO;
import com.github.foxnic.dao.spring.OracleDAO;

 /**
  * 针对各种值类型测测试
  * */
public class DAO_QueryValue  extends TableDataTest{
 
	@Test
	public void test_int() {
		 int i=dao.insert(normalTable).set("title", "99").set("code", "098").set("read_times", 2).set("id", 10).execute();
		 assertTrue(i==1);
		 Integer value=dao.queryInteger("select title from "+normalTable+" where code=?","098");
		 assertTrue(99==value);
		 value=dao.queryInteger("select code from "+normalTable+" where code=?","098");
		 assertTrue(98==value);
		 value=dao.queryInteger("select read_times from "+normalTable+" where code=?","098");
		 assertTrue(2==value);
	}
	
	@Test
	public void test_double() {
		 int i=dao.insert(normalTable).set("title", "99.3").set("code", "96.3").set("price", 2.6).set("id", 10).execute();
		 assertTrue(i==1);
		 Double value=dao.queryDouble("select title from "+normalTable+" where code=?","96.3");
		 assertTrue(99.3==value);
		 value=dao.queryDouble("select code from "+normalTable+" where code=?","96.3");
		 assertTrue(96.3==value);
	}
	
	
	@Test
	public void test_localdate() {
		 LocalDateTime now=LocalDateTime.now();
		 Date n=DataParser.parseDate(now);
		 int i=dao.insert(normalTable).set("publish_day", now).set("enter_time", now).set("code", "96.3").set("id", 10).execute();
		 assertTrue(i==1);
		 Date value=dao.queryDate("select publish_day from "+normalTable+" where code=?","96.3");
		 DateTime vd=new  DateTime(value);
 
		 //日期部分相等即可
	 
		 assertTrue(vd.getYear()==now.getYear() && vd.getDayOfMonth()==now.getDayOfMonth());
		 if(!(dao instanceof Db2DAO))
		 {
			 assertTrue(Math.abs(vd.toDate().getTime()-n.getTime())<1000);
		 }
		 value=dao.queryDate("select enter_time from "+normalTable+" where code=?","96.3");
		 vd=new  DateTime(value);
 
		 assertTrue(Math.abs(vd.toDate().getTime()-n.getTime())<1000);
	}
	
	@Test
	public void test_date() {
		DateTime now=new DateTime();
		Date time=new Timestamp(now.toDate().getTime());
		 int i=dao.insert(normalTable).set("publish_day", now.toDate()).set("enter_time", time).set("code", "96.3").set("id", 10).execute();
		 assertTrue(i==1);
		 Date value=dao.queryDate("select publish_day from "+normalTable+" where code=?","96.3");
		 DateTime vd=new  DateTime(value);
		 //System.out.println(now.getTime()+"\n"+value.getTime());
		 //System.out.println((now.getTime()-(now.getTime()%1000))+"\n"+value.getTime());
		 //日期部分相等即可
	 
		 assertTrue(vd.getYear()==now.getYear() && vd.getDayOfMonth()==now.getDayOfMonth());
		 if(!(dao instanceof Db2DAO))
		 {
			 assertTrue(Math.abs(vd.toDate().getTime()-now.toDate().getTime())<1000);
		 }
		 value=dao.queryDate("select enter_time from "+normalTable+" where code=?","96.3");
		 vd=new  DateTime(value);
		// System.out.println(now.getTime()+"\n"+value.getTime());
		// System.out.println((now.getTime()-(now.getTime()%1000))+"\n"+value.getTime());
//		assertTrue((vd.isEqual(now)));
		 assertTrue(Math.abs(vd.toDate().getTime()-now.toDate().getTime())<1000);
	}
	
	
	@Test
	public void test_timestamp() {
		DateTime now=new DateTime();
		Date time=new Timestamp(now.toDate().getTime());
		 int i=dao.insert(normalTable).set("publish_day", now.toDate()).set("enter_time", time).set("code", "96.3").set("id", 10).execute();
		 assertTrue(i==1);
		 Timestamp value=dao.queryTimestamp("select publish_day from "+normalTable+" where code=?","96.3");
		 DateTime vd=new  DateTime(value);
		 //System.out.println(now.getTime()+"\n"+value.getTime());
		 //System.out.println((now.getTime()-(now.getTime()%1000))+"\n"+value.getTime());
		 //日期部分相等即可
	 
		 assertTrue(vd.getYear()==now.getYear() && vd.getDayOfMonth()==now.getDayOfMonth());
		 if(!(dao instanceof Db2DAO))
		 {
			 assertTrue(Math.abs(vd.toDate().getTime()-now.toDate().getTime())<1000);
		 }
		 value=dao.queryTimestamp("select enter_time from "+normalTable+" where code=?","96.3");
		 vd=new  DateTime(value);
//		 System.out.println(now.toDate().getTime()+"\n"+value.getTime());
		// System.out.println((now.getTime()-(now.getTime()%1000))+"\n"+value.getTime());
//		assertTrue((vd.isEqual(now)));
		 //语句在2秒内执行完成
		 assertTrue(Math.abs(vd.toDate().getTime()-now.toDate().getTime())<2000);
	}
 
	@Test
	public void test_time() {
		DateTime now=new DateTime();
		Date time=new Timestamp(now.toDate().getTime());
		Time nt=new Time(now.toDate().getTime());
		 int i=dao.insert(normalTable).set("publish_day", now.toDate())
				 .set("alert_time", nt)
				 .set("enter_time", time).set("code", "96.3").set("id", 10).execute();
		 
		 assertTrue(i==1);
		 
		 Time value=dao.queryTime("select alert_time from "+normalTable+" where code=?","96.3");
		 
		 LocalTime t0=DateUtil.toLocalTime(nt);
		 LocalTime t1=DateUtil.toLocalTime(value);
	   
	    //LocalDate localDate = localDateTime.toLocalDate();
		 
		 assertTrue(t1.getHour()==t0.getHour());
		 assertTrue(t1.getMinute()==t0.getMinute());
		 assertTrue(t1.getSecond()==t0.getSecond());
 
	}
	
	@Test
	public void test_clob() {
		
		String id=IDGenerator.getSUID();
		String cnt="你好DouDou，这是一个Clob字段";
		 int i=dao.insert(clobTable).set("id", id)
				 .set("content",cnt)
				 .execute();
		 //
		 assertTrue(i==1);
		 //
		 String cnt2=dao.queryString("select content from "+clobTable+" where id=?",id);
		 assertTrue(cnt.equals(cnt2));
		 
		 RcdSet rs=dao.query("select * from "+clobTable);
		 assertTrue(rs.size()>0);
//		 for (Rcd r : rs) {
//			 String ctv=r.getString("content");
//			 if(!cnt.equals(ctv)) {
//				 System.out.println();
//			 }
//			 assertTrue(cnt.equals(ctv));
//		}
	}
	 
	@Test
	public void test_raw() {
		
		if(dao instanceof OracleDAO)
		{
			int i=dao.queryInteger("select 1 from dual");
			assertTrue(i==1);
			
			i=dao.queryInteger("select DEMO_SEQ.nextVal from dual");
			assertTrue(i==1);
			
			i=dao.queryInteger("select DEMO_SEQ.nextVal from dual");
			assertTrue(i==2);
			
		}
		
		else if(dao instanceof Db2DAO)
		{
			int i=dao.queryInteger("select 1 from sysibm.dual");
			assertTrue(i==1);
		}
		
		else if(dao instanceof MySqlDAO)
		{
			int i=dao.queryInteger("select 1 ");
			assertTrue(i==1);
		}
		
	}
	
	
	
	

}
