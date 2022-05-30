package com.github.foxnic.dao.excel.wrapper;

import com.github.foxnic.dao.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

public class SheetWrapper {

    private WorkBookWrapper bookWrapper = null;

    public WorkBookWrapper book() {
        return bookWrapper;
    }
    private Sheet sheet;

    public  SheetWrapper (WorkBookWrapper bookWrapper,Sheet sheet) {
        this.bookWrapper=bookWrapper;
        this.sheet=sheet;
    }

    public SheetWrapper merge(int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress region  = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(region);
        return this;
    }

    public RowWrapper row(int index) {
        return row(index,true);
    }

    public RowWrapper row(int index,boolean create) {
        Row row=sheet.getRow(index);
        if(row==null && create) {
            row = sheet.createRow(index);
        }
        if(row==null) return null;
        return new RowWrapper(this.bookWrapper,this,row);
    }

    public SheetWrapper merge(String range) {
        CellRangeAddress region = CellRangeAddress.valueOf(range);
        sheet.addMergedRegion(region);
        return this;
    }

    public SheetWrapper columnWidth(String columnIndex,int charWidth) {
        sheet.setColumnWidth(ExcelUtil.fromExcel26(columnIndex),ExcelUtil.calcPixel(charWidth));
        return this;
    }

    public SheetWrapper columnWidth(int columnIndex,int charWidth) {
        sheet.setColumnWidth(columnIndex,ExcelUtil.calcPixel(charWidth));
        return this;
    }

    public SheetWrapper columnWidthRaw(int columnIndex,int rawWidth) {
        sheet.setColumnWidth(columnIndex,rawWidth);
        return this;
    }

    public Integer columnWidth(int columnIndex) {
        return sheet.getColumnWidth(columnIndex);
    }

    public SheetWrapper autoSizeColumn(int columnIndex) {
        sheet.autoSizeColumn(columnIndex);
        return this;
    }

    public SheetWrapper autoSizeColumn(int columnIndex,boolean useMergedCells) {
        sheet.autoSizeColumn(columnIndex,useMergedCells);
        return this;
    }




}
