package com.github.foxnic.dao.excel;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.AbstractSet.ExcelDataReader;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
 

/***
 * Excel读取器
 * 
 * @author fangjieli
 */
public class ExcelReader extends ExcelDataReader {

	private File file;
	private InputStream inputStream = null;
	private Workbook workbook;
	
	/**
	 * 构造器
	 * @param file Excel文件
	 * */
	public ExcelReader(File file) {
		this.file = file;
	}

	/**
	 * 构造器
	 * @param input Excel输入流
	 * */
	public ExcelReader(InputStream input) {
		this.inputStream = input;
	}

	private void readFileIf() throws Exception {

		Exception ex=null;
		if (workbook == null && file != null) {
			try {
				workbook = WorkbookFactory.create(file);
			} catch (Exception e) {
				ex=e;
				Logger.debug("Excel 读取错误", e);
			}
		}
		if (workbook == null && inputStream != null) {
			try {
				workbook = WorkbookFactory.create(inputStream);
			} catch (Exception e) {
				ex=e;
				Logger.debug("Excel 读取错误", e);
			}
		}

		if (workbook == null) {
			throw new Exception("无法读取Excel文件");
		}
		
		if(ex!=null) {
			Logger.exception(ex);
		}
	}
 
	
	/**
	 * 从数据行读取结构，把第N行作为列名(字段名)
	 * @param sheetName  读取的Sheet名称
	 * @param rowIndex  行序号
	 * @return ExcelStructure
	 * @exception Exception 读取异常
	 */
	public ExcelStructure readStructure(String sheetName,int rowIndex) throws Exception {
		readFileIf();
		Sheet sheet = workbook.getSheet(sheetName);
		return readStructure(sheet,rowIndex);
	}
	
	/**
	 * 从数据行读取结构，把第N行作为列明(字段名)
	 * 读取指定序号的Sheet,第一个sheet的序号为0
	 * @param sheetIndex  读取的Sheet序号
	 * @param rowIndex  行序号
	 * @return ExcelStructure
	 * @exception Exception 读取异常
	 */
	public ExcelStructure readStructure(int sheetIndex,int rowIndex) throws Exception {
		readFileIf();
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		return readStructure(sheet,rowIndex);
	}

	private ExcelStructure readStructure(Sheet sheet, int rowIndex) throws Exception {
		readFileIf();
		ExcelStructure es = new ExcelStructure();
		es.setDataRowBegin(rowIndex+1);
		Row row=sheet.getRow(rowIndex-1);
		Cell cell=null;
		String value=null;
		
		for (int i = 0; i < row.getLastCellNum(); i++) {
			cell=row.getCell(i);
			if(cell==null) continue;
			value=cell.getStringCellValue();
			if(StringUtil.hasContent(value))
			{
				value=value.trim();
				String charIndex=ExcelStructure.toExcel26(i+1);
				Logger.info(charIndex+" -> "+value);
				es.addColumn(charIndex,value);
			}
		}
		return es;
	}

	/**
	 * 读取指定名称的Sheet
	 * @param sheetName  读取的Sheet名称
	 * @param es  ExcelStructure
	 * @return 记录集
	 * @exception Exception 读取异常
	 */
	public RcdSet read(String sheetName, ExcelStructure es) throws Exception {
		readFileIf();
		Sheet sheet = workbook.getSheet(sheetName);
		return readSheet(sheet, es,null);
	}

	/**
	 * 读取指定序号的Sheet,第一个sheet的序号为0
	 * @param sheetIndex  读取的Sheet序号
	 * @param es  ExcelStructure
	 * @return 记录集
	 * @exception Exception 读取异常
	 */
	public RcdSet read(int sheetIndex, ExcelStructure es) throws Exception {
		readFileIf();
		if(sheetIndex<=0) sheetIndex=0;
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		return readSheet(sheet, es,null);
	}
	
	
	/**
	 * 读取指定名称的Sheet
	 * @param sheetName  读取的Sheet序号
	 * @param es  ExcelStructure
	 * @param handler  数据处理器
	 * @exception Exception 读取异常
	 */
	public void read(String sheetName, ExcelStructure es,RowDataHandler handler) throws Exception {
		readFileIf();
		Sheet sheet = workbook.getSheet(sheetName);
		readSheet(sheet, es,handler);
	}

	/**
	 * 读取指定序号的Sheet,第一个sheet的序号为0
	 * @param sheetIndex  读取的Sheet序号
	 * @param es  ExcelStructure
	 * @param handler  数据处理器
	 * @exception Exception 读取异常
	 */
	public void read(int sheetIndex, ExcelStructure es,RowDataHandler handler) throws Exception {
		readFileIf();
		if(sheetIndex<=0) sheetIndex=0;
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		readSheet(sheet, es,handler);
	}
 
	private RcdSet readUseOPCInternal(int sheetIndex, ExcelStructure es,RowDataHandler handler) throws Exception {
		OPCExcelReader ocpExcelReader=new OPCExcelReader(this,handler);
		return ocpExcelReader.readSheet(this.file.getAbsolutePath(), es, sheetIndex);
	}
	
	/**
	 * 读取指定序号的Sheet,第一个 sheet 的序号为 0
	 * 实际测试结果，性能高于常规读取方式
	 * @param sheetIndex  读取的Sheet序号
	 * @param es  ExcelStructure
	 * @return 记录集
	 * @exception Exception 读取异常
	 */
	public RcdSet readUseOPC(int sheetIndex, ExcelStructure es) throws Exception {
		return readUseOPCInternal(sheetIndex, es, null);
	}
 
	
	/**
	 * 读取指定序号的Sheet,第一个sheet的序号为0
	 * 实际测试结果，性能高于常规读取方式
	 * @param sheetIndex  读取的Sheet序号
	 * @param es  ExcelStructure
	 * @param handler  数据处理器
	 * @exception Exception 读取异常
	 */
	public void readUseOPC(int sheetIndex, ExcelStructure es,RowDataHandler handler) throws Exception {
		readUseOPCInternal(sheetIndex, es, handler);
	}
	
	/**
	 * 读取第一个Sheet
	 * 实际测试结果，性能高于常规读取方式
	 * @param es  ExcelStructure
	 * @param handler  数据处理器
	 * @exception Exception 读取异常
	 */
	public void readUseOPC(ExcelStructure es,RowDataHandler handler) throws Exception {
		readUseOPC(0,es,handler);
	}

	/**
	 * 读取第一个Sheet
	 * @param es  ExcelStructure
	 * @return 记录集
	 * @exception Exception 读取异常
	 */
	public RcdSet read(ExcelStructure es) throws Exception {
		return read(0, es);
	}
	
	/**
	 * 读取第一个Sheet
	 * @param es  ExcelStructure
	 * @param handler  数据处理器
	 * @exception Exception 读取异常
	 */
	public void read(ExcelStructure es,RowDataHandler handler) throws Exception {
		read(0, es,handler);
	}

	
	private RcdSet readSheet(Sheet sheet, ExcelStructure es,RowDataHandler handler) {
		RcdSet rs = new RcdSet();

		// 生成与设置Meta
		QueryMetaData meta =this.applyMetaDataDetail(sheet.getSheetName(), es);
		this.applyRawMetaData(rs, meta);
 
		int lastRowNum = sheet.getLastRowNum();
		//如果是Sheet内无数据
		if(lastRowNum<=0)
		{
			return rs;
		}
		int i = es.getDataRowBegin();
		if (i < 1) {
			i = 1;
		}

		i = i - 1;

		Row row = null;
		Cell cell = null;
		Object value = null;
		ExcelColumn column = null;
		Rcd r=null;
		int totalRow=lastRowNum - es.getDataRowEndDelta();
		while (i <= totalRow) {
			row = sheet.getRow(i);
			// 跳过空行
			if (row == null) {
				i++;
				continue;
			}
			//
			if(i%1000==0)
			{
				Logger.info("read excel "+i+" rows , total "+(lastRowNum - es.getDataRowEndDelta())+" rows");
			}
			 
			r = new Rcd(rs);
			rs.add(r);
			 
			int ce=es.getDataColumnEnd();
			if(ce<=0) ce=row.getLastCellNum();
			ce=Math.min(ce, row.getLastCellNum());
			for (int c = es.getDataColumnBegin(); c <=ce; c++) {
				cell = row.getCell(c - 1);
				column = es.getColumn(c);
				if (column == null) {
					continue;
				}
				value = readCellValue(cell,column,i);
				r.setValue(column.getField(), value);
			}
			
			 
			if(isBlank(r, meta.getColumnLabels()))
			{
				rs.remove(rs.size()-1);
			}
			
			
			if(handler!=null && rs.size()>=handler.getLimitSize())
			{
				try {
					handler.process(rs,i-rs.size(),totalRow);
				} catch (Exception e) {
					Logger.exception("处理单元格数据异常", e);
				}
				
				//重置记录集
				rs=new RcdSet();
				// 生成与设置Meta
				meta =this.applyMetaDataDetail(sheet.getSheetName(), es);
				this.applyRawMetaData(rs, meta);
			}
 
			i++;
		}
		
		//last ones
		if(handler!=null)
		{
			try {
				handler.process(rs,i-rs.size(),totalRow);
			} catch (Exception e) {
				Logger.exception("处理单元格数据异常", e);
			}
		}
		
		
		return rs;
	}
	
	/**
	 * 记录集是否为空
	 * @param r 记录
	 * @param cols 列清单
	 * @return 是否为空
	 * */
	boolean isBlank(Rcd r,String[] cols)
	{
		int emptys=0;
		Object value=null;
	 
		for (String label : cols) {
			value=r.getValue(label);
			if(value==null)
			{
				emptys++;
			}
			else
			{
				if(value instanceof String)
				{
					if(!StringUtil.hasContent((String)value))
					{
						emptys++;
					}
				}
			}
		}
		return emptys>=cols.length;
	}

	
	
	private FormulaEvaluator evaluator = null;
	

	private Object readCellValue(Cell cell,ExcelColumn column,int rowIndex) {

		if (cell == null) {
			return null;
		}

		Object value = null;
		CellType cellType = cell.getCellTypeEnum();

		// 读取字符串数据
		if (cellType == CellType.STRING) {
			try {
				value = cell.getStringCellValue();
			} catch (Exception e) {}
			if (value == null) {
				try {
					RichTextString rich = cell.getRichStringCellValue();
					if (rich != null) {
						value = rich.getString();
					}
				} catch (Exception e) {}
			}
		} else if (cellType == CellType.NUMERIC) {

			try {
				if(DateUtil.isCellDateFormatted(cell)) { 
					value = cell.getDateCellValue();
				} else {
					value = cell.getNumericCellValue();
				}
			} catch (Exception e) {}

		} else if (cellType == CellType.FORMULA) {
 
			if(value==null) {
				try {
			        value =cell.getNumericCellValue();    
			    } catch (Exception e) {}
			}
			
			if(value==null) {
				if(evaluator==null) evaluator=workbook.getCreationHelper().createFormulaEvaluator();
				try {
					value=evaluator.evaluate(cell);
				} catch (Exception e) {}
			}
			
			if(value==null) {
				try {
			        value =cell.getNumericCellValue();    
			    } catch (Exception e) {}
			}
			
			if (value == null) {
				try {
					value = cell.getStringCellValue();
				} catch (Exception e) {}
			}
			
			if (value == null) {
				try {
					RichTextString rich = cell.getRichStringCellValue();
					if (rich != null) {
						value=rich.getString();
					}
				} catch (Exception e) {}
			}
			
			if (value == null) {
				try {
					 value=cell.getBooleanCellValue();
				} catch (Exception e) {}
			}
 
			
		} else if (cellType == CellType.BOOLEAN) {
			try {
				value = cell.getBooleanCellValue();
			} catch (Exception e) {}
		} else if (cellType == CellType._NONE) {
			value = null;
		} else if (cellType == CellType.BLANK) {
			value = "";
		} else if (cellType == CellType.ERROR) {
			value = "DATA-ERROR";
		}
		
		value=column.toTypedValue(rowIndex, value);

		return value;

	}

}
