package com.github.foxnic.dao.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.github.foxnic.dao.data.Rcd;

public abstract class ExcelWriteHandler {
	
	public abstract void handleRow(int rowIndex,Rcd r,Row row);

	public abstract void handleCell(int rowIndex, int cellIndex,Row head,Cell cell,String field);
}
