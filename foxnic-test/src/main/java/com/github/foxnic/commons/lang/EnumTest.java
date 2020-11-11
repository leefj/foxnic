package com.github.foxnic.commons.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EnumTest {

	public static enum DAY {
		D1,
		D2;
	}
	
	public static enum MOMEY {
		D1,
		RMB
	}
	
	
	public static enum BIKE {
		D1(1),
		RMB(2);
		
		private int index=0;
		private BIKE(int i)
		{
			this.index=i;
		}
 
	}
	
	
	 
	
	@Test
	public void enumTest()
	{
		DAY d1=DataParser.parseEnum(DAY.D1,DAY.class,DAY.D2,null);
		assertTrue(d1!=null && d1==DAY.D1);
		
		DAY d2=DataParser.parseEnum(MOMEY.D1,DAY.class,DAY.D2,null);
		assertTrue(d2!=null && d2==DAY.D1);
		
		BIKE d3=DataParser.parseEnum(2,BIKE.class,BIKE.D1,"index");
		assertTrue(d3!=null && d3==BIKE.RMB);
	}
	
	
	
}
