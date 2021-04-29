package com.github.foxnic.dao.excel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

 

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.DateUtil;

/**
 * Excel 数据列描述
 * @author 李方捷
 * */
public class ExcelColumn  {
	
	private int index=-1;
 
	private Class dataType=null;
	
	private String field=null;
	
	private Validator validator=null;
	
	/**
	 * 获得校验器
	 * @return 校验器
	 * */
	public Validator getValidator() {
		return validator;
	}
	
	/**
	 * 设置校验器
	 * @param validator 校验器
	 * @return ExcelColumn，当前对象
	 * */
	public ExcelColumn setValidator(Validator validator) {
		this.validator = validator;
		return this;
	}
	
	/**
	 * @return 字段名
	 * */
	public String getField() {
		return field;
	}
	
	/**
	 * 设置字段名
	 * @param field 字段名
	 * */
	public void setField(String field) {
		this.field = field;
	}
	
	/**
	 * @return 是否允许空值
	 * */
	public boolean isAllowBlank() {
		return allowBlank;
	}
	
	/**
	 * 设置是否允许空值
	 * @param allowBlank 是否允许空值
	 * @return ExcelColumn
	 * */
	public ExcelColumn setAllowBlank(boolean allowBlank) {
		this.allowBlank = allowBlank;
		return this;
	}
 
	private boolean allowBlank=true;
	
	/**
	 * @return 数字列序号
	 * */
	public int getIndex() {
		return index;
	}
	
	/**
	 * 设置数字列序号
	 * @param index 数字列序号
	 * */
	public ExcelColumn setIndex(int index) {
		this.index = index;
		return this;
	}
	
	/**
	 * @return 字符列序号
	 * */
	public String getCharIndex() {
		return ExcelStructure.toExcel26(index);
	}
	
	/**
	 * 设置字符列序号
	 * @param charIndex 字符列序号
	 * */
	public ExcelColumn setCharIndex(String charIndex) {
		this.index = ExcelStructure.fromExcel26(charIndex);
		return this;
	}
	
	/**
	 * @return 数据类型
	 * */
	public Class getDataType() {
		return dataType;
	}
	
	/**
	 * 设置数据类型
	 * @param dataType 数据类型
	 * */
	public void setDataType(Class dataType) {
		this.dataType = dataType;
	}
	
	private List<ValidateResult> validateResults=new ArrayList<>();
 
	/**
	 * 获得校验结果
	 * @return 校验结果
	 * */
	public List<ValidateResult> getValidateResults() {
		return this.validateResults;
	}
	
	
	private static Calendar calendar = new GregorianCalendar(1900, 0, -1);
	
	/**
	 * 转换值，并校验
	 * @param rowIndex 列序号
	 * @param value 值
	 * @return 转换后的值
	 * */
	public Object toTypedValue(int rowIndex,Object value)
	{
		//校验非空
		if(!this.isAllowBlank() && value==null)
		{
			ValidateResult vr=new ValidateResult(this,rowIndex,"不允许为空");
			validateResults.add(vr);
		}
		
		//类型校验
		Object newValue = value;
		
		if(this.dataType!=null && value!=null)
		{
			BigDecimal num=DataParser.parseBigDecimal(value);
			//特殊类型处理
			if(num!=null && this.dataType.equals(Date.class))
			{
				int day=num.intValue();
				
				Date date = calendar.getTime();
				date = DateUtil.addDays(date, day);
				
				int hour=(int)((num.doubleValue()-day)*24);
				int minute=(int)(((num.doubleValue()-day)*24*60)%60);
				int second=(int)(((num.doubleValue()-day)*24*3600)%60);
				int ms=(int)(((num.doubleValue()-day)*24*3600*1000)%1000);
				
				date = DateUtil.addHours(date, hour);
				date = DateUtil.addMinutes(date, minute);
				date = DateUtil.addSeconds(date, second);
				date = DateUtil.addMilliseconds(date, ms);
				
				newValue=date;
				 
			}
			
			
			newValue=DataParser.parse(dataType, newValue);
			if(newValue==null)
			{
				ValidateResult vr=new ValidateResult(this,rowIndex,"无法转换为 "+this.dataType.getName()+" 类型");
				validateResults.add(vr);
			}
		}
		
		//使用校验器校验
		if(this.validator!=null)
		{
			List<ValidateResult> vrs=this.validator.validate(this, rowIndex, newValue);
			if(vrs!=null)
			{
				this.validateResults.addAll(vrs);
			}
		}
		return newValue;
	}

	
	
	private int charWidth=8;

	/**
	 * @return 列宽
	 * */
	public int getCharWidth() {
		return charWidth;
	}
	
	/**
	 * 列宽，以字符个数为单位
	 * @param charWidth 列宽
	 * */
	public ExcelColumn setCharWidth(int charWidth) {
		this.charWidth = charWidth;
		return this;
	}
	
	private String title;
	
	/**
	 * 抬头
	 * @return 抬头
	 * */
	public String getTitle() {
		return title;
	}
	
	/**
	 * 设置值
	 * @param title 抬头
	 * */
	public ExcelColumn setTitle(String title) {
		this.title = title;
		return this;
	}
	
	
	private  String backgroundColor=null;
	
	public  String getBackgroundColor() {
		return backgroundColor;
	}

	public  ExcelColumn setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public  String getTextColor() {
		return textColor;
	}

	public  ExcelColumn setTextColor(String textColor) {
		this.textColor = textColor;
		return this;
	}

	private  String textColor=null;
 
	
}
