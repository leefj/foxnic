package com.github.foxnic.dao.excel.wrapper;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.excel.Version;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WorkBookWrapper {
    private Workbook book = null;

    public Workbook getWorkBook() {
        return book;
    }

    public WorkBookWrapper() {
        this(Version.V2007);
    }
    public WorkBookWrapper(Version version) {
        if (version == Version.V2007) {
            book = new XSSFWorkbook();
        } else {
            book = new HSSFWorkbook();
        }
    }

    @SuppressWarnings("unused")
    public void applyFontColor(Font font, Color color) {
        if (font instanceof XSSFFont) {
            XSSFFont xssfFont = (XSSFFont) font;
            xssfFont.setColor(new XSSFColor(color));
        } else if (font instanceof HSSFFont) {
            HSSFFont hssfFont = (HSSFFont) font;
            HSSFWorkbook book = (HSSFWorkbook) this.book;
            HSSFPalette customPalette = book.getCustomPalette();
            HSSFColor hssfColor = customPalette.addColor((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
            hssfFont.setColor(hssfColor.getIndex());
        }
    }

    public void applyForegroundColor(CellStyle style, Color color) {
        if (style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfCellStyle = (XSSFCellStyle) style;
            xssfCellStyle.setFillForegroundColor(new XSSFColor(color));
        } else if (style instanceof HSSFCellStyle) {
            HSSFCellStyle hssfCellStyle = (HSSFCellStyle) style;
            HSSFWorkbook book = (HSSFWorkbook) this.book;
            HSSFPalette customPalette = book.getCustomPalette();
            HSSFColor hssfColor = customPalette.addColor((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
            hssfCellStyle.setFillForegroundColor(hssfColor.getIndex());
        }
    }

    @SuppressWarnings("unused")
    public void applyBackgroundColor(CellStyle style, Color color) {
        if (style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfCellStyle = (XSSFCellStyle) style;
            xssfCellStyle.setFillBackgroundColor(new XSSFColor(color));
        } else if (style instanceof HSSFCellStyle) {
            HSSFCellStyle hssfCellStyle = (HSSFCellStyle) style;
            HSSFWorkbook book = (HSSFWorkbook) this.book;
            HSSFPalette customPalette = book.getCustomPalette();
            HSSFColor hssfColor = customPalette.addColor((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
            hssfCellStyle.setFillBackgroundColor(hssfColor.getIndex());
        }
    }

    public void write(OutputStream os) throws IOException {
        book.write(os);
    }

    public SheetWrapper sheet(String sheetName) {
        return sheet(sheetName,true);
    }
    public SheetWrapper sheet(String sheetName,boolean create) {
        Sheet sheet=book.getSheet(sheetName);
        if(sheet==null && create) {
            sheet = book.createSheet(sheetName);
        }
        if(sheet==null) return null;
        return new SheetWrapper(this,sheet);
    }

    public Font createFont() {
        return book.createFont();
    }

    public CellStyle createCellStyle() {
        return book.createCellStyle();
    }


    public void save(String filePath) {
        save(new File(filePath));
    }

    public void save(File file) {
        try {
            file.getParentFile().mkdirs();
            OutputStream outputStream = new FileOutputStream(file);
            write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            Logger.exception(e);
        }
    }



}
