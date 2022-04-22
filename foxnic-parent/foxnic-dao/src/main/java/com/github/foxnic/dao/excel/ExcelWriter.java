package com.github.foxnic.dao.excel;

import com.github.foxnic.commons.lang.ColorUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.excel.wrapper.CellWrapper;
import com.github.foxnic.dao.excel.wrapper.RowWrapper;
import com.github.foxnic.dao.excel.wrapper.SheetWrapper;
import com.github.foxnic.dao.excel.wrapper.WorkBookWrapper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Clob;
import java.util.HashMap;


/**
 * Excel写入器
 *
 * @author fangjieli
 */
public class ExcelWriter {


    private String workBookName;

    public void setWorkBookName(String workBookName) {
        this.workBookName = workBookName;
    }

    public String getWorkBookName() {
        return workBookName;
    }


    private WorkBookWrapper book = null;

    /**
     * 构造器
     *
     * @param version 指定 Eexcel 版本
     */
    public ExcelWriter(Version version) {
        book = new WorkBookWrapper(version);
    }

    /**
     * 构造器，默认版本 Excel 2007
     */
    public ExcelWriter() {
        this(Version.V2007);
    }

    /**
     * 保存到磁盘
     *
     * @param path 保存的路径
     */
    public void save(String path) {
        this.save(new File(path));
    }

    /**
     * 保存到数据流
     *
     * @param stream 输出流
     */
    public void save(OutputStream stream) {

        try {
            book.write(stream);
            stream.flush();
        } catch (IOException e) {
            Logger.exception(e);
        }
    }


    /**
     * 保存到磁盘
     *
     * @param file 文件
     */
    public void save(File file) {
        try {
            file.getParentFile().mkdirs();
            OutputStream outputStream = new FileOutputStream(file);
            save(outputStream);
            outputStream.close();
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * 写入流
     *
     * @param outputStream 输出流
     */
    public void write(OutputStream outputStream) {
        try {
            book.write(outputStream);
        } catch (IOException e) {
            Logger.exception(e);
        }
    }

    /**
     * 填充数据到指定sheet
     *
     * @param rs        记录集
     * @param sheetName sheet 名称
     * @return Sheet
     */
    public SheetWrapper fillSheet(RcdSet rs, String sheetName) {
        return fillSheet(rs, sheetName, null);
    }

    /**
     * 填充数据到指定sheet
     *
     * @param rs        记录集
     * @param sheetName sheet 名称
     * @param structure 结构
     * @return Sheet
     */
    public SheetWrapper fillSheet(RcdSet rs, String sheetName, ExcelStructure structure) {
        SheetWrapper sheet = book.sheet(sheetName,true);
        return fillSheet(rs, sheet, structure);
    }

    public Workbook getWorkBook() {
        return book.getWorkBook();
    }

    public WorkBookWrapper getWorkBookWrap() {
        return book;
    }


    /**
     * 填充数据到指定sheet
     *
     * @param rs        记录集
     * @param sheet     sheet
     * @param structure 结构
     * @return Sheet
     */
    private SheetWrapper fillSheet(RcdSet rs, SheetWrapper sheet, ExcelStructure structure) {
        int i = 0;
        int columnIndex = 0;

        if (structure == null) structure = ExcelStructure.parse(rs);

        HashMap<Integer, CellStyle> headerStyleMap = new HashMap<>();
        HashMap<Integer, CellStyle> cellStyleMap = new HashMap<>();
        //初始化列样式
        for (columnIndex = 0; columnIndex < structure.getColumnCount(); columnIndex++) {
            ExcelColumn ec = structure.getColumn(columnIndex);

            //表头
            Font headerFont = book.createFont();
            headerFont.setBold(true);
            if (ec.getTextColor() != null) {
                Color color = ColorUtil.toColor(ec.getTextColor());
                book.applyFontColor(headerFont, color);
            }
            CellStyle headerStyle = book.createCellStyle();
            headerStyle.setFont(headerFont);
            if (ec.getBackgroundColor() != null) {
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                book.applyForegroundColor(headerStyle, ColorUtil.toColor(ec.getBackgroundColor()));
            }
            headerStyleMap.put(columnIndex, headerStyle);

            //表行
            Font cellFont = book.createFont();
            cellFont.setBold(false);
            if (ec.getTextColor() != null) {
                Color color = ColorUtil.toColor(ec.getTextColor());
                book.applyFontColor(cellFont, color);
            }
            CellStyle cellStyle = book.createCellStyle();
            cellStyle.setFont(cellFont);
            if (ec.getBackgroundColor() != null) {
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                book.applyForegroundColor(cellStyle, ColorUtil.toColor(ec.getBackgroundColor()));
            }
            cellStyleMap.put(columnIndex, cellStyle);

        }

        ExcelColumn.CellWriter defaultCellWriter= structure.getCellWriter();

        //构建表头(单行表头)
        int headRowIndex = structure.getDataRowBegin();
        if (headRowIndex >= 0) {
            RowWrapper head = sheet.row(headRowIndex,true);
            if (structure.getRowHandler() != null) {
                structure.getRowHandler().handle(headRowIndex, structure, null, head);
            }
            String title = null;
            for (columnIndex = 0; columnIndex < structure.getColumnCount(); columnIndex++) {
                ExcelColumn ec = structure.getColumn(columnIndex);
                ExcelColumn.CellWriter cellWriter= ec.getCellWriter();
                if(cellWriter==null) cellWriter=defaultCellWriter;
                CellStyle headerStyle = headerStyleMap.get(columnIndex);
                CellWrapper cell = head.cell(ec.getIndex()+structure.getColumnMoveDelta(),true);
                title = ec.getTitle();
                if (!StringUtil.hasContent(title)) title = ec.getField();
                cell.value(title).styleRaw(headerStyle);
                if (cellWriter != null) {
                    cellWriter.write(structure, ec, headRowIndex, columnIndex, head, cell, "header");
                }
            }
        }


        //导出数据行
        Object value = null;
        i = headRowIndex + 1;

        for (Rcd r : rs) {
            RowWrapper row = sheet.row(i,true);
            if (structure.getRowHandler() != null) {
                structure.getRowHandler().handle(i, structure, r, row);
            }
            for (columnIndex = 0; columnIndex < structure.getColumnCount(); columnIndex++) {
                ExcelColumn ec = structure.getColumn(columnIndex);
                ExcelColumn.CellWriter cellWriter= ec.getCellWriter();
                if(cellWriter==null) cellWriter=defaultCellWriter;
                CellStyle cellStyle = cellStyleMap.get(columnIndex);
                CellWrapper cell = row.cell(ec.getIndex()+structure.getColumnMoveDelta(),true);
                value = r.getValue(ec.getField());
                if (value instanceof Clob) value = DataParser.parseString(value);
                cell.value(value).styleRaw(cellStyle);
                if (cellWriter != null) {
                    cellWriter.write(structure, ec, i, columnIndex, row, cell, "data");
                }
            }
            i++;
        }

        //调整列宽
        for (columnIndex = 0; columnIndex < structure.getColumnCount(); columnIndex++) {
            ExcelColumn ec = structure.getColumn(columnIndex);
            if (ec.getCharWidth()>0) {
                sheet.columnWidth(ec.getIndex()+ structure.getColumnMoveDelta(),   ec.getCharWidth());
            } else {
                sheet.autoSizeColumn(ec.getIndex() + structure.getColumnMoveDelta());
                int w=sheet.columnWidth(ec.getIndex() + structure.getColumnMoveDelta());
                try {
                    sheet.columnWidthRaw(ec.getIndex()+ structure.getColumnMoveDelta(), (int)(w*1.2));
                } catch (Exception e) {}
            }
        }

        return sheet;
    }






}
