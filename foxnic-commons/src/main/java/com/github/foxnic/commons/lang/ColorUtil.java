package com.github.foxnic.commons.lang;

import java.awt.Color;

public class ColorUtil {

	public static Color toColor(String hex) {
		
		if(StringUtil.isBlank(hex)) {
			return null;
		}
		hex=StringUtil.removeFirst(hex, "#");
		if(hex.length()==6) {
			hex="FF"+hex;
		}
		String str1 = hex.substring(0, 2);
		String str2 = hex.substring(2, 4);
		String str3 = hex.substring(4, 6);
		String str4 = hex.substring(6, 8);
		int alpha = Integer.parseInt(str1, 16);
		int red = Integer.parseInt(str2, 16);
		int green = Integer.parseInt(str3, 16);
		int blue = Integer.parseInt(str4, 16);
		Color color = new Color(red, green, blue, alpha);
		return color;
	}
	
	public static String toHex(int red, int green, int blue) {
        return "#"+ intToHexValue(red) + intToHexValue(green) + intToHexValue(blue);
    }
	
	public static String toHex(int red, int green, int blue,int alpha) {
        return "#"+intToHexValue(alpha) + intToHexValue(red) + intToHexValue(green) + intToHexValue(blue);
    }

	public static String toHex(Color color) {
        return "#"+intToHexValue(color.getAlpha()) + intToHexValue(color.getRed()) + intToHexValue(color.getGreen()) + intToHexValue(color.getBlue());
    }

    private static String intToHexValue(int number) {
        String result = Integer.toHexString(number & 0xff);
        while (result.length() < 2) {
            result = "0" + result;
        }
        return result.toUpperCase();
    }
//	public static Color toColor(String colorStr){
//		if(StringUtil.isBlank(colorStr)) {
//			return null;
//		}
//		colorStr=StringUtil.removeFirst(colorStr, "#");
//		colorStr=colorStr.trim();
//		colorStr = colorStr.substring(4);
//		Color color =  new Color(Integer.parseInt(colorStr, 16)) ;
//		return color;
//	}

//	public static String toHex(int r, int g, int b) {
//		String rFString, rSString, gFString, gSString, bFString, bSString, result;
//		int red, green, blue;
//		int rred, rgreen, rblue;
//		red = r / 16;
//		rred = r % 16;
//		if (red == 10)
//			rFString = "A";
//		else if (red == 11)
//			rFString = "B";
//		else if (red == 12)
//			rFString = "C";
//		else if (red == 13)
//			rFString = "D";
//		else if (red == 14)
//			rFString = "E";
//		else if (red == 15)
//			rFString = "F";
//		else
//			rFString = String.valueOf(red);
//
//		if (rred == 10)
//			rSString = "A";
//		else if (rred == 11)
//			rSString = "B";
//		else if (rred == 12)
//			rSString = "C";
//		else if (rred == 13)
//			rSString = "D";
//		else if (rred == 14)
//			rSString = "E";
//		else if (rred == 15)
//			rSString = "F";
//		else
//			rSString = String.valueOf(rred);
//
//		rFString = rFString + rSString;
//
//		green = g / 16;
//		rgreen = g % 16;
//
//		if (green == 10)
//			gFString = "A";
//		else if (green == 11)
//			gFString = "B";
//		else if (green == 12)
//			gFString = "C";
//		else if (green == 13)
//			gFString = "D";
//		else if (green == 14)
//			gFString = "E";
//		else if (green == 15)
//			gFString = "F";
//		else
//			gFString = String.valueOf(green);
//
//		if (rgreen == 10)
//			gSString = "A";
//		else if (rgreen == 11)
//			gSString = "B";
//		else if (rgreen == 12)
//			gSString = "C";
//		else if (rgreen == 13)
//			gSString = "D";
//		else if (rgreen == 14)
//			gSString = "E";
//		else if (rgreen == 15)
//			gSString = "F";
//		else
//			gSString = String.valueOf(rgreen);
//
//		gFString = gFString + gSString;
//
//		blue = b / 16;
//		rblue = b % 16;
//
//		if (blue == 10)
//			bFString = "A";
//		else if (blue == 11)
//			bFString = "B";
//		else if (blue == 12)
//			bFString = "C";
//		else if (blue == 13)
//			bFString = "D";
//		else if (blue == 14)
//			bFString = "E";
//		else if (blue == 15)
//			bFString = "F";
//		else
//			bFString = String.valueOf(blue);
//
//		if (rblue == 10)
//			bSString = "A";
//		else if (rblue == 11)
//			bSString = "B";
//		else if (rblue == 12)
//			bSString = "C";
//		else if (rblue == 13)
//			bSString = "D";
//		else if (rblue == 14)
//			bSString = "E";
//		else if (rblue == 15)
//			bSString = "F";
//		else
//			bSString = String.valueOf(rblue);
//		bFString = bFString + bSString;
//		result = "#" + rFString + gFString + bFString;
//		return result;
//
//	}

}
