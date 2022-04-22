package com.github.foxnic.dao.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;

class OPCExcelReader extends DefaultHandler {

	private SharedStringsTable sst;
	private String lastContents;
	private boolean nextIsString;

	private static final String CONST_ROW="row";
	private static final String CONST_T="t";
	private static final String CONST_V="v";
	private static final String CONST_C="c";
	private static final String CONST_R="r";
	private static final String CONST_S="s";


	private int sheetIndex = -1;
	private List<String> rowlist = new ArrayList<String>();
	private int curRow = 0;
	private int curCol = 0;
	private String col = "";
	private Map<String,String> map = new HashMap<>();

	private ExcelStructure structure;
	private RcdSet rs=null;
	private ExcelReader owner = null;
	private RowDataHandler handler = null;

	public OPCExcelReader(ExcelReader owner,RowDataHandler handler)
	{
		this.owner=owner;
		this.handler=handler;
	}

	/**
	 * 读取第一个工作簿的入口方法
	 *
	 * @param path
	 */
	public RcdSet readSheet(String path,ExcelStructure es,int sheetIndex) throws Exception {
		if(sheetIndex<0) sheetIndex=0;
		sheetIndex=sheetIndex+1;
		String sheetName="sheet-"+sheetIndex;

		this.structure=es;


		rs = new RcdSet();
		// 生成与设置Meta
		QueryMetaData meta = this.owner.applyMetaDataDetail(sheetName, es);
		this.owner.applyRawMetaData(rs, meta);


		OPCPackage pkg = OPCPackage.open(path);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		InputStream sheet = r.getSheet("rId"+sheetIndex);

		InputSource sheetSource = new InputSource(sheet);
		parser.parse(sheetSource);

		sheet.close();

		//按结构设置删除尾行
		int i=structure.getDataRowEndDelta();
		while(i>0)
		{
			rs.remove(rs.size()-1);
			i--;
		}

		//last ones
		if(handler!=null)
		{
			try {
				handler.process(rs,i-rs.size(),-1);
			} catch (Exception e) {
				Logger.exception("处理单元格数据异常", e);
			}
		}

		return rs;
	}


	/**
	 * 该方法自动被调用，每读一行调用一次，在方法中写自己的业务逻辑即可
	 *
	 * @param sheetIndex 工作簿序号
	 * @param curRow     处理到第几行
	 * @param rowList    当前数据行的数据集合
	 */
	public void optRow(int sheetIndex, int curRow, List<String> rowList) {
//		String temp = "";
//		for (String str : rowList) {
//			temp += str + "_";
//		}
//		System.out.println();
	}

	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		this.sst = sst;
		parser.setContentHandler(this);
		return parser;
	}

	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		// c => 单元格
		if (CONST_C.equals(name)) {
			col = attributes.getValue(CONST_R);
			// 如果下一个元素是 SST 的索引，则将nextIsString标记为true
			String cellType = attributes.getValue(CONST_T);
			if (cellType != null && cellType.equals(CONST_S)) {
				nextIsString = true;
			} else {
				nextIsString = false;
			}
		}
		// 置空
		lastContents = "";
	}

	public void endElement(String uri, String localName, String name) throws SAXException {
		// 根据SST的索引值的到单元格的真正要存储的字符串
		// 这时characters()方法可能会被调用多次
		if (nextIsString) {
			try {
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
				nextIsString = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
		// 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
		if (CONST_V.equals(name)) {
			String value = lastContents.trim();
			rowlist.add(curCol, value);
			curCol++;
			map.put(col, value);
		} else {
			// 如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
			if (CONST_ROW.equals(name)) {
				optRow(sheetIndex, curRow, rowlist);
				if(this.structure!=null)
				{
					outMap(map, curRow);
				}
				rowlist.clear();
				curRow++;
				curCol = 0;
			}
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		// 得到单元格内容的值
		lastContents += new String(ch, start, length);
	}



	/**
	 * 测试输出
	 *
	 * @param map1
	 * @param row
	 */
	private void outMap(Map map1, int row) {

		row = row + 1;

		if(row<structure.getDataRowBegin()) return;


		Rcd r=new Rcd(rs);

		int begin=structure.getColumnReadBeginIndex();
		int end=structure.getColumnReadEndIndex();
		String columnName=null;
		Object value=null;
		ExcelColumn column = null;
		for ( int c=begin; c<=end; c++ ) {
			column = structure.getColumn(c);
			if (column == null) {
				continue;
			}
			columnName=ExcelUtil.toExcel26(c);
			value=(String)map1.get(columnName+row);

			value=column.toTypedValue(row, value);

			r.setValue(column.getField(), value);
		}

		if(!owner.isBlank(r, rs.getMetaData().getColumnLabels()))
		{
			rs.add(r);
			if(rs.size()%1000==0)
			{
				Logger.info("read excel "+rs.size()+" rows");
			}
		}

		//
		if(handler!=null && rs.size()>=handler.getLimitSize())
		{
			try {
				handler.process(rs,row-rs.size(),-1);
			} catch (Exception e) {
				Logger.exception("处理单元格数据异常", e);
			}
			QueryMetaData meta=rs.getMetaData();
			//重置记录集
			rs=new RcdSet();
			// 生成与设置Meta
			this.owner.applyRawMetaData(rs, meta);
		}


	}

}
