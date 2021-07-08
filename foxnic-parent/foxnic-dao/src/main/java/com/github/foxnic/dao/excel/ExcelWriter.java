package com.github.foxnic.dao.excel;

import com.github.foxnic.commons.lang.ColorUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Date;
import java.util.HashMap;
 

/**
 * Excel写入器
 * @author fangjieli
 */
public class ExcelWriter {


	private String workBookName;

    public void setWorkBookName(String workBookName) {
    	this.workBookName=workBookName;
    }

	public String getWorkBookName() {
		return workBookName;
	}

	private static class WorkBookWrap
	{
		private Workbook book=null;
 
		public Workbook getWorkBook() {
			return book;
		}
		
		

		WorkBookWrap(Version version)
		{
			if(version==Version.V2007) {
				book=new XSSFWorkbook();
			} else {
				book=new HSSFWorkbook();
			}
		}
		
		@SuppressWarnings("unused")
		public void applyFontColor(Font font,Color color) {
			if(font instanceof XSSFFont) {
				XSSFFont xssfFont=(XSSFFont)font;
				xssfFont.setColor(new XSSFColor(color));
			} else if(font instanceof HSSFFont) {
				HSSFFont hssfFont=(HSSFFont)font;
				HSSFWorkbook book=(HSSFWorkbook)this.book;
				HSSFPalette customPalette = book.getCustomPalette();
		        HSSFColor hssfColor = customPalette.addColor((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
		        hssfFont.setColor(hssfColor.getIndex());
			}
		}
		
		public void applyForegroundColor(CellStyle style,Color color) {
			if(style instanceof XSSFCellStyle) {
				XSSFCellStyle xssfCellStyle=(XSSFCellStyle)style;
				xssfCellStyle.setFillForegroundColor(new XSSFColor(color));
			} else if(style instanceof HSSFCellStyle) {
				HSSFCellStyle hssfCellStyle=(HSSFCellStyle)style;
				HSSFWorkbook book=(HSSFWorkbook)this.book;
				HSSFPalette customPalette = book.getCustomPalette();
		        HSSFColor hssfColor = customPalette.addColor((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
		        hssfCellStyle.setFillForegroundColor(hssfColor.getIndex());
			}
		}
		
		@SuppressWarnings("unused")
		public void applyBackgroundColor(CellStyle style,Color color) {
			if(style instanceof XSSFCellStyle) {
				XSSFCellStyle xssfCellStyle=(XSSFCellStyle)style;
				xssfCellStyle.setFillBackgroundColor(new XSSFColor(color));
			} else if(style instanceof HSSFCellStyle) {
				HSSFCellStyle hssfCellStyle=(HSSFCellStyle)style;
				HSSFWorkbook book=(HSSFWorkbook)this.book;
				HSSFPalette customPalette = book.getCustomPalette();
		        HSSFColor hssfColor = customPalette.addColor((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
		        hssfCellStyle.setFillBackgroundColor(hssfColor.getIndex());
			}
		}
		
		public void write(OutputStream os) throws IOException {
			book.write(os);
		}
		
		public Sheet createSheet(String sheetname) {
			return book.createSheet(sheetname);
		}
		
		public Sheet getSheet(String sheetname)
		{
			return book.getSheet(sheetname);
		}
		
		public Font createFont()
		{
			return book.createFont();
		}
		
		public CellStyle createCellStyle()
		{
			return book.createCellStyle();
		}
		
	}
	
	private WorkBookWrap book= null;
	
	private ExcelWriteHandler handler=null;
	
	public ExcelWriteHandler getHandler() {
		return handler;
	}

	public void setHandler(ExcelWriteHandler handler) {
		this.handler = handler;
	}
 
	/**
	 * 构造器
	 * @param version 指定 Eexcel 版本
	 * */
	public ExcelWriter(Version version)
	{
		 book = new WorkBookWrap(version);
	}
	
	/**
	 * 构造器，默认版本 Excel 2007
	 * */
	public ExcelWriter()
	{
		this(Version.V2007);
	}
	
	/**
	 * 保存到磁盘
	 * @param path 保存的路径
	 * */
	public void save(String path)
	{
		this.save(new File(path));
	}
	
	/**
	 * 保存到数据流
	 * @param stream 输出流
	 * */
	public void save(OutputStream stream)
	{
	
		try {
			book.write(stream);
			stream.flush();
		} catch (IOException e) {
			Logger.exception(e);
		}
	}
	 
	
	/**
	 * 保存到磁盘
	 * @param file 文件
	 * */
	public void save(File file)
	{
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
	 * @param outputStream 输出流
	 * */
	public void write(OutputStream outputStream)
	{
		try {
			book.write(outputStream);
		} catch (IOException e) {
			Logger.exception(e);
		}
	}
	
	/**
	 * 填充数据到指定sheet
	 * @param rs 记录集
	 * @param sheetname sheet 名称
	 * @return Sheet
	 * */
	public Sheet fillSheet(RcdSet rs,String sheetname)
	{
		return fillSheet(rs, sheetname, null);
	}
	
	/**
	 * 填充数据到指定sheet
	 * @param rs 记录集
	 * @param sheetname sheet 名称
	 * @param structure 结构
	 * @return Sheet
	 * */
	public Sheet fillSheet(RcdSet rs,String sheetname,ExcelStructure structure)
	{
		Sheet  sheet=book.getSheet(sheetname);
		if(sheet==null)
		{
			sheet=book.createSheet(sheetname);
		}
		return fillSheet(rs,sheet,structure);
	}
	
	public Workbook getWorkBook() {
		return book.getWorkBook();
	}
	
 
	
	/**
	 * 填充数据到指定sheet
	 * @param rs 记录集
	 * @param sheet sheet 
	 * @param structure 结构
	 * @return Sheet
	 * */
	private Sheet fillSheet(RcdSet rs,Sheet sheet,ExcelStructure structure)
	{
		int i=0;
		int columnIndex=0;
		
		if(structure==null) structure=ExcelStructure.parse(rs);
 
		HashMap<Integer, CellStyle> headerStyleMap=new HashMap<>();
		HashMap<Integer, CellStyle> cellStyleMap=new HashMap<>();
		//初始化列样式
		for (columnIndex = 0; columnIndex < structure.getColumnCount();  columnIndex++) {
			 ExcelColumn ec=structure.getColumn(columnIndex);
			 
			 //表头
			 Font headerFont = book.createFont();
			 headerFont.setBold(true);
			 if(ec.getTextColor()!=null) {
				 Color color=ColorUtil.toColor(ec.getTextColor());
				 book.applyFontColor(headerFont, color);
			 }
			 CellStyle headerStyle = book.createCellStyle();
			 headerStyle.setFont(headerFont);
			 if(ec.getBackgroundColor()!=null) {
				 headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				 book.applyForegroundColor(headerStyle, ColorUtil.toColor(ec.getBackgroundColor()));
			 }
			 headerStyleMap.put(columnIndex, headerStyle);
			 
			 //表行
			 Font cellFont = book.createFont();
			 cellFont.setBold(false);
			 if(ec.getTextColor()!=null) {
				 Color color=ColorUtil.toColor(ec.getTextColor());
				 book.applyFontColor(cellFont, color);
			 }
			 CellStyle cellStyle = book.createCellStyle();
			 cellStyle.setFont(cellFont);
			 if(ec.getBackgroundColor()!=null) {
				 cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				 book.applyForegroundColor(cellStyle, ColorUtil.toColor(ec.getBackgroundColor()));
			 }
			 cellStyleMap.put(columnIndex, cellStyle);
			 
		}
 
		//构建表头(单行表头)
		int headRowIndex=structure.getDataRowBegin()-2;
		if(headRowIndex>=0)
		{
			Row  head=sheet.createRow(headRowIndex);
			if(handler!=null) {
				handler.handleRow(headRowIndex, null, head);
			}
			String title=null;
			for (columnIndex = 0; columnIndex < structure.getColumnCount();  columnIndex++) {
				 ExcelColumn ec=structure.getColumn(columnIndex);
				 CellStyle headerStyle = headerStyleMap.get(columnIndex); 
				 Cell cell=head.createCell(ec.getIndex());
				 title=ec.getTitle();
				 if(!StringUtil.hasContent(title)) title=ec.getField();
				 setCellValue(cell,title);
				 cell.setCellStyle(headerStyle);
				 if(handler!=null) {
						handler.handleCell(headRowIndex, columnIndex, head,cell,ec.getField());
				}
			}
		}
		
		
		
		
		
		//导出数据行
		Object value=null;
		i=headRowIndex+1;
		
		for (Rcd r : rs) {
			Row  row=sheet.createRow(i);
			if(handler!=null) {
				handler.handleRow(i, r, row);
			}
			for (columnIndex = 0; columnIndex < structure.getColumnCount();  columnIndex++) {
				 ExcelColumn ec=structure.getColumn(columnIndex);
				 CellStyle cellStyle = cellStyleMap.get(columnIndex); 
				 Cell cell=row.createCell(ec.getIndex());
				 value=r.getValue(ec.getField());
				 if(value instanceof Clob) value=DataParser.parseString(value);
				 setCellValue(cell,value);
				 cell.setCellStyle(cellStyle);
				 if(handler!=null) {
						handler.handleCell(headRowIndex, columnIndex, row,cell,ec.getField());
				}
			}
			i++;
		}
 
		//调整列宽
		for (columnIndex = 0; columnIndex < structure.getColumnCount();  columnIndex++) {
			 ExcelColumn ec=structure.getColumn(columnIndex);
			 sheet.autoSizeColumn(ec.getIndex());
			 sheet.setColumnWidth(ec.getIndex(), sheet.getColumnWidth(ec.getIndex())+512);
		}
 
		return sheet;
	}
	
	private void setCellValue(Cell cell,Object value)
	{
		if(value==null) {
			return;
		}
		if(value instanceof String)
		{
			cell.setCellValue((String)value);
		}
		else if(value instanceof Short)
		{
			cell.setCellValue((Short)value);
		}
		else if(value instanceof Integer)
		{
			cell.setCellValue((Integer)value);
		}
		else if(value instanceof Long)
		{
			cell.setCellValue((Long)value);
		}
		else if(value instanceof Float)
		{
			cell.setCellValue((Float)value);
		}
		else if(value instanceof Double)
		{
			cell.setCellValue((Double)value);
		}
		else if(value instanceof Date)
		{
			cell.setCellValue(value.toString());
		}
		else if(value instanceof java.util.Date)
		{
			cell.setCellValue(value.toString());
		}
		else if(value instanceof BigDecimal)
		{
			cell.setCellValue(((BigDecimal)value).doubleValue());
		}
		else if(value instanceof BigInteger)
		{
			cell.setCellValue(((BigInteger)value).longValue());
		}
		else
		{
			cell.setCellValue(value.toString());
		}
	}
	
	
}
