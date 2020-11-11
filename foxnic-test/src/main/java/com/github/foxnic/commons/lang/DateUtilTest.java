package com.github.foxnic.commons.lang;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

public class DateUtilTest {

	@Test
	public void test_parse()
	{
		String t=DateUtil.getCurrTime("yyyy-MM-dd hh:mm:ss.S");
		assertTrue(t!=null && t.length()>0);
		
		DateTime d1=new DateTime(2019, 9, 25, 8, 8, 14);
		//
		DateTime d2=new DateTime(DateUtil.parse("2019").getTime());
		assertTrue(d1.getYear()==d2.getYear());
		//
		d2=new DateTime(DateUtil.parse("2019-09").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear());
		//
		d2=new DateTime(DateUtil.parse("201909").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear());
		//
		d2=new DateTime(DateUtil.parse("20199").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear());
		//
		d2=new DateTime(DateUtil.parse("2019/09").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear());
		//
		d2=new DateTime(DateUtil.parse("2019/9").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear());
		
		
		//
		d2=new DateTime(DateUtil.parse("2019-09-25").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth());
		//
		d2=new DateTime(DateUtil.parse("20190925").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth());
		//
		d2=new DateTime(DateUtil.parse("2019/09/25").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth());
		//
		d2=new DateTime(DateUtil.parse("2019/9/25").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth());
		
		
		//
		d2=new DateTime(DateUtil.parse("2019-09-25 08").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay());
	 
		d2=new DateTime(DateUtil.parse("2019-09-25 8").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay());
	 
		d2=new DateTime(DateUtil.parse("2019/09/25 8").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay());
	 
		
		d2=new DateTime(DateUtil.parse("2019-09-2508").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay());
	 
		d2=new DateTime(DateUtil.parse("2019-09-250808").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay() && d1.getMinuteOfHour() == d2.getMinuteOfHour());
	 
		d2=new DateTime(DateUtil.parse("2019-09-25 08:08").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay() && d1.getMinuteOfHour() == d2.getMinuteOfHour());
	 
		d2=new DateTime(DateUtil.parse("2019-09-25 08:08:14").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay() && d1.getMinuteOfHour() == d2.getMinuteOfHour()  && d1.getSecondOfMinute() == d2.getSecondOfMinute());
	 
		d2=new DateTime(DateUtil.parse("2019-09-25 080814").getTime());
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay() && d1.getMinuteOfHour() == d2.getMinuteOfHour()  && d1.getSecondOfMinute() == d2.getSecondOfMinute());
 
	}
	
	@Test
	public void test_locals()
	{
		DateTime d1=new DateTime(2019, 9, 25, 8, 8, 14);
		LocalDateTime ldt= DateUtil.toLocalDateTime(d1.toDate());
		DateTime d2=new DateTime(DateUtil.toDate(ldt));
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth()  && d1.getHourOfDay() == d2.getHourOfDay() && d1.getMinuteOfHour() == d2.getMinuteOfHour()  && d1.getSecondOfMinute() == d2.getSecondOfMinute());
		 
		//
		LocalDate ld= DateUtil.toLocalDate(d1.toDate());
		d2=new DateTime(DateUtil.toDate(ld));
		assertTrue(d1.getYear()==d2.getYear() && d1.getMonthOfYear()==d2.getMonthOfYear() && d1.getDayOfMonth()==d2.getDayOfMonth());
		
		//
		LocalTime lt= DateUtil.toLocalTime(d1.toDate());
		d2=new DateTime(DateUtil.toDate(lt));
		assertTrue(d1.getHourOfDay() == d2.getHourOfDay() && d1.getMinuteOfHour() == d2.getMinuteOfHour()  && d1.getSecondOfMinute() == d2.getSecondOfMinute());
 
	}
	
	
	@Test
	public void test_week()
	{
		 
		Date d=DataParser.parseDate("2020-03-01");
		Date d1=DataParser.parseDate("2020-03-01");
		String w=DateUtil.getChineseWeek(d, true);
		assertTrue("周日".equals(w));
		
		d=DataParser.parseDate("2020-03-02");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周一".equals(w));

		d=DataParser.parseDate("2020-03-03");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周二".equals(w));
		
		d=DataParser.parseDate("2020-03-04");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周三".equals(w));
		
		d=DataParser.parseDate("2020-03-05");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周四".equals(w));
		
		d=DataParser.parseDate("2020-03-06");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周五".equals(w));
		
		d=DataParser.parseDate("2020-03-07");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周六".equals(w));
		
		d=DataParser.parseDate("2020-03-08");
		w=DateUtil.getChineseWeek(d, true);
		assertTrue("周日".equals(w));
		
		
		Date d2=DateUtil.getSameDayByWeek(d1,2);
		w=DateUtil.getChineseWeek(d2, true);
		assertTrue("周日".equals(w));
		
	}
	
	@Test
	public void test_x()
	{
		
	}
	
	
	@Test
	public void test_fns()
	{
		Date d1=DataParser.parseDate("2020-04-16");
		Date d2=DataParser.parseDate("2020-04-17");
		assertTrue(DateUtil.isInSameWeek(d1, d2));
		
		d1=DataParser.parseDate("2020-04-16");
		d2=DataParser.parseDate("2020-04-20");
		
		assertTrue(!DateUtil.isInSameWeek(d1, d2));
	}
	
	
	@Test
	public void test_weekend()
	{
		Date d1=DataParser.parseDate("2020-04-16");
		Date d2=DataParser.parseDate("2020-04-17");
		
		assertTrue(!DateUtil.isWeekEnd(d1));
		assertTrue(!DateUtil.isWeekEnd(d2));
		
		d1=DataParser.parseDate("2020-04-18");
		d2=DataParser.parseDate("2020-04-19");
		
		assertTrue(DateUtil.isWeekEnd(d1));
		assertTrue(DateUtil.isWeekEnd(d2));
	}
	
	
	
}
