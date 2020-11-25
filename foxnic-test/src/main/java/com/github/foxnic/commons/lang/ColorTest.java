package com.github.foxnic.commons.lang;

import java.awt.Color;

import org.junit.Test;

public class ColorTest {

	@Test
	public void testColor() throws Exception {
		
		String hex=ColorUtil.toHex(255, 255, 255);
		
		Color color=ColorUtil.toColor(hex);
		String hex2=ColorUtil.toHex(color.getRed(), color.getGreen(),color.getBlue());
		System.out.println();
	}
	
	
}
