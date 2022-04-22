package com.github.foxnic.dao.excel.wrapper;

import com.github.foxnic.dao.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class RowWrapper {

    private WorkBookWrapper bookWrapper = null;

    public WorkBookWrapper book() {
        return bookWrapper;
    }
    private SheetWrapper sheetWrapper;

    public SheetWrapper sheet() {
        return sheetWrapper;
    }

    private Row row;

    public  RowWrapper (WorkBookWrapper bookWrapper, SheetWrapper sheetWrapper, Row row) {
        this.bookWrapper=bookWrapper;
        this.sheetWrapper=sheetWrapper;
        this.row=row;
    }

    public CellWrapper cell(int index,boolean create) {
        Cell cell=row.getCell(index);
        if(cell==null && create) {
            cell = row.createCell(index);
        }
        if(cell==null) return null;
        return new CellWrapper(this.bookWrapper,this.sheetWrapper,this,cell);
    }

    public RowWrapper height(int charWidth) {
        row.setHeight((short) ExcelUtil.calcPixel(charWidth));
        return this;
    }


}
