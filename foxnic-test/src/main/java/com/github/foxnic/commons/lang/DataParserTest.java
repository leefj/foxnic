package com.github.foxnic.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.github.foxnic.commons.support.pojo.Demo;
 
/**
 * @author LeeFangJie
 */
public class DataParserTest {

	@Test
	public void testParseMap() throws Exception {
		
	}
	
	@Test
	public void testParseList() throws Exception {
 
		Field f=Demo.class.getDeclaredField("strList");
		Method m=Demo.class.getDeclaredMethod("setStrList",List.class);
		Parameter p=m.getParameters()[0];
		List<Object> a=DataParser.parseList(f, "1,3");
		List<Object> b=DataParser.parseList(p, "1,3");
		System.out.println();
		
		assertTrue(a!=null);
		assertTrue(a.size()==2);
		assertTrue("3".equals(a.get(1)));
		
		assertTrue(b!=null);
		assertTrue(b.size()==2);
		assertTrue("3".equals(b.get(1)));
	}
	
	@Test
	public void testParseArr() {
		
		String[] ax0=DataParser.parse(String[].class, "6");
		assertTrue(ax0!=null);
		assertTrue(ax0.length==1);
		assertTrue("6".equals(ax0[0]));
		
		Integer[] ax=DataParser.parse(Integer[].class, "6");
		assertTrue(ax!=null);
		assertTrue(ax.length==1);
		assertTrue(6==ax[0]);
		
 
		Integer[] a=DataParser.parse(Integer[].class, "1,3");
		assertTrue(a!=null);
		assertTrue(a.length==2);
		assertTrue(3==a[1]);
		
		String[] b=DataParser.parse(String[].class, "1,3");
		assertTrue(b!=null);
		assertTrue(b.length==2);
		assertTrue("3".equals(b[1]));
		
		String[] c=DataParser.parse(String[].class, "[1,3]");
		assertTrue(c!=null);
		assertTrue(c.length==2);
		assertTrue("3".equals(c[1]));
	}

	@Test
	public void testParseBoolean() {
		
		 
		
		boolean b=false;
		b=DataParser.parseBoolean("Y");
		assertEquals(b, true);
	}

	 

	@Test
	public void testParseInteger() {
		Integer i=DataParser.parseInteger("30.4");
		assertTrue(i==30);
		i=DataParser.parseInteger("30");
		assertTrue(i==30);
		i=DataParser.parseInteger("30L");
		assertTrue(i==null);
		i=DataParser.parseInteger("30.8");
		assertTrue(i==30);
		
		
		boolean num=DataParser.isNumberType(1);
		assertTrue(num);
		
		num=DataParser.isNumberType(int.class);
		assertTrue(num);
		
		num=DataParser.isNumberType(float.class);
		assertTrue(num);
		
		num=DataParser.isNumberType(BigDecimal.class);
		assertTrue(num);
		
		num=DataParser.isNumberType(AtomicInteger.class);
		assertTrue(num);
	}

	@Test
	public void testParseBigInteger() {
		BigInteger i=DataParser.parseBigInteger("30.6");
		assertTrue(i.intValue()==30);
		i=DataParser.parseBigInteger("30");
		assertTrue(i.intValue()==30);
		i=DataParser.parseBigInteger("30L");
		assertTrue(i==null);
		i=DataParser.parseBigInteger("30.8");
		assertTrue(i.intValue()==30);
	}

	@Test
	public void testParseFloat() {
		Float i=DataParser.parseFloat("30.6");
		assertTrue(Math.abs(i-30.6)<0.001);
		i=DataParser.parseFloat("30");
		assertTrue(Math.abs(i-30)<0.001);
		i=DataParser.parseFloat("30L");
		assertTrue(i==null);
		i=DataParser.parseFloat("30.8");
		assertTrue(Math.abs(i-30.8)<0.001);
		
		i=DataParser.parseFloat(30.8);
		assertTrue(Math.abs(i-30.8)<0.001);
		
		i=DataParser.parseFloat(30.8f);
		assertTrue(Math.abs(i-30.8)<0.001);
		
	 
		
	}

	@Test
	public void testParseDouble() {
		Double i=DataParser.parseDouble("30.6");
		assertTrue(Math.abs(i-30.6)<0.001);
		i=DataParser.parseDouble("30");
		assertTrue(Math.abs(i-30)<0.001);
		i=DataParser.parseDouble("30L");
		assertTrue(i==null);
		i=DataParser.parseDouble("30.8");
		assertTrue(Math.abs(i-30.8)<0.001);
		
		i=DataParser.parseDouble(30.8);
		assertTrue(Math.abs(i-30.8)<0.001);
		
		i=DataParser.parseDouble(30.8f);
		assertTrue(Math.abs(i-30.8)<0.001);
		
		i=DataParser.parseDouble(30.8d);
		assertTrue(Math.abs(i-30.8)<0.001);
		
		
	}

	@Test
	public void testParseBigDecimal() {
		 
		BigDecimal i=DataParser.parseBigDecimal("30.6");
		assertTrue(Math.abs(i.floatValue()-30.6)<0.001);
		i=DataParser.parseBigDecimal("30");
		assertTrue(Math.abs(i.doubleValue()-30)<0.001);
		i=DataParser.parseBigDecimal("30L");
		assertTrue(i==null);
		i=DataParser.parseBigDecimal("30.8");
		assertTrue(Math.abs(i.doubleValue()-30.8)<0.001);
		
		i=DataParser.parseBigDecimal(30.8);
		assertTrue(Math.abs(i.doubleValue()-30.8)<0.001);
		
		i=DataParser.parseBigDecimal(30.8f);
		assertTrue(Math.abs(i.doubleValue()-30.8)<0.001);
		
		i=DataParser.parseBigDecimal(30.8d);
		assertTrue(Math.abs(i.doubleValue()-30.8)<0.001);
	}

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
	public void testDate(String value,String result)
    {
    	Date date=null;
    	String str=null;
    	date=DataParser.parseDate(value);
    	str=sdf.format(date);
    	assertTrue(str.equals(result));
    }
	
	@Test
	public void testString()
    {
    	Date date=null;
    	String str=null;
    	
    	oracle.sql.TIMESTAMP tm=new oracle.sql.TIMESTAMP();
    	str=DataParser.parseString(tm);
    	assertTrue(str.contains("1970"));
    	//System.out.println(str);
//    	str=sdf.format(date);
//    	assertTrue(str.equals(result));
    }
	 
	@Test
	public void testParseDate() {
		
		testDate("2018","2018-01-01 00:00:00");
		testDate("2018-09","2018-09-01 00:00:00");
		testDate("2018/09","2018-09-01 00:00:00");
		testDate("201809","2018-09-01 00:00:00");
		testDate("2018-12-04","2018-12-04 00:00:00");
		testDate("T2018-12-04","2018-12-04 00:00:00");
		
		testDate("T2018-12-04121212","2018-12-04 12:12:12");
		testDate("T2018-12-041212","2018-12-04 12:12:00");
		
		
		
		
	}



	 

	@Test
	public void testParseShort() {
		Short i=DataParser.parseShort("30.4");
		assertTrue(i==30);
		i=DataParser.parseShort("30");
		assertTrue(i==30);
		i=DataParser.parseShort("30L");
		assertTrue(i==null);
		i=DataParser.parseShort("30.8");
		assertTrue(i==30);
	}

	@Test
	public void testParseLong() {
		Long i=DataParser.parseLong("30.4");
		assertTrue(i==30);
		i=DataParser.parseLong("30");
		assertTrue(i==30);
		i=DataParser.parseLong("30L");
		assertTrue(i==null);
		i=DataParser.parseLong("30.8");
		assertTrue(i==30);
	}

	
	

}
