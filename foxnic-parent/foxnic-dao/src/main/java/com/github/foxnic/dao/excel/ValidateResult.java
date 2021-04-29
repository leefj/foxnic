package com.github.foxnic.dao.excel;

public class ValidateResult {

	public ExcelColumn column=null;
	public int rowIndex=-1;
	public String message=null;
	
	public ValidateResult(ExcelColumn column,int rowIndex,String message)
	{
		this.column=column;
		this.rowIndex=rowIndex;
		this.message=message;
	}
	
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	
}
