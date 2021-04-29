package com.github.foxnic.dao.excel;

import java.util.List;

public abstract class Validator {
	
	public abstract List<ValidateResult> validate(ExcelColumn column,int rowIndex,Object value);
	 
}
