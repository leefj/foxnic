package com.github.foxnic.dao.excel;

import com.github.foxnic.commons.lang.StringUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelUtil {

    public static void applyBorder(Cell cell) {
        CellStyle style=cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    public static int calcPixel(int charWidth) {
        return 256*charWidth+512;
    }

    public static void merge(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress region  = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(region);
    }

    public static void merge(Sheet sheet, String range) {
        CellRangeAddress region = CellRangeAddress.valueOf(range);
        sheet.addMergedRegion(region);
    }

    /**
     * 把整数转换成Excel的英文字母列序号
     * @param n 数字序号
     * @return 字符序号
     * */
    public static String toExcel26(int n){
        n=n+1;
        String s = "";
        while (n > 0){
            int m = n % 26;
            if (m == 0) {
                m = 26;
            }
            s = (char)(m + 64) + s;
            n = (n - m) / 26;
        }
        return s.toUpperCase();
    }

    /**
     * 英文字母列序号把整数转换成Excel的
     * @param s 字符序号
     * @return 数字序号
     * */
    public static int fromExcel26(String s){
        if(StringUtil.isBlank(s)) {
            return 0;
        }
        int n = 0;
        for (int i = s.length() - 1, j = 1; i >= 0; i--, j *= 26){
            char c = s.charAt(i);
            if (c < 'A' || c > 'Z') {
                return 0;
            }
            n += ((int)c - 64) * j;
        }
        return n-1;
    }
}
