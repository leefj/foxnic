package com.github.foxnic.dao.excel.wrapper;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

public class CellWrapper {

    private WorkBookWrapper bookWrapper = null;

    public WorkBookWrapper book() {
        return bookWrapper;
    }
    private SheetWrapper sheetWrapper;

    public SheetWrapper sheet() {
        return sheetWrapper;
    }

    private RowWrapper rowWrapper;

    private Cell cell;

    public  CellWrapper (WorkBookWrapper bookWrapper, SheetWrapper sheetWrapper, RowWrapper rowWrapper,Cell cell) {
        this.bookWrapper=bookWrapper;
        this.sheetWrapper=sheetWrapper;
        this.rowWrapper=rowWrapper;
        this.cell = cell;
    }

    public CellWrapper value(Object value) {
        if (value == null) {
            value="";
        }
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Short) {
            cell.setCellValue((Short) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Date) {
            cell.setCellValue(value.toString());
        } else if (value instanceof java.util.Date) {
            cell.setCellValue(value.toString());
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof BigInteger) {
            cell.setCellValue(((BigInteger) value).longValue());
        } else {
            cell.setCellValue(value.toString());
        }
        return this;
    }


    private StyleWrapper style = null;

    public StyleWrapper style() {
        if(style==null) {
            CellStyle cellStyle=this.book().createCellStyle();
            style=new StyleWrapper(this.bookWrapper,cellStyle);
            styleRaw(cellStyle);
        }
        return style;
    }


    public CellWrapper styleRaw(CellStyle style) {
        cell.setCellStyle(style);
        return this;
    }

    public CellWrapper applyBorder(BorderStyle borderStyle) {
        CellStyle style=cell.getCellStyle();
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderTop(borderStyle);
        style.setBorderRight(borderStyle);
        return this;
    }

    public CellWrapper applyBorder() {
        return applyBorder(BorderStyle.THIN);
    }



}
