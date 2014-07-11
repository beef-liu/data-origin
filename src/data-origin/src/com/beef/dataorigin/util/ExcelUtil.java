package com.beef.dataorigin.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

	public static List<String> getAllSheetNameList(InputStream inputExcel,
			boolean isXLSX) throws IOException {
		Workbook workbook = createWorkbook(inputExcel, isXLSX);
		
		List<String> sheetNameList = new ArrayList<String>();
		
		int sheetCount = workbook.getNumberOfSheets();
		
		for(int i = 0; i < sheetCount; i++) {
			sheetNameList.add(workbook.getSheetName(i));
		}
		
		return sheetNameList;
	}
	
	/**
	 * 
	 * @param inputExcel
	 * @param isXLSX
	 * @param sheetIndex
	 * @param beginCol
	 * @param endCol
	 * @param beginRow
	 * @return Object could be String, Boolean, Double, java.util.Date
	 * @throws IOException
	 */
	public static List<List<Object>> readRows(InputStream inputExcel,
			boolean isXLSX, int sheetIndex, int beginCol, int endCol,
			int beginRow) throws IOException {
		Workbook workbook = createWorkbook(inputExcel, isXLSX);

		Sheet sheet = workbook.getSheetAt(sheetIndex);

		int endRow = sheet.getLastRowNum();

		return readRows(sheet, beginCol, endCol, beginRow, endRow);
	}

	/**
	 * 
	 * @param inputExcel
	 * @param isXLSX
	 * @param sheetIndex
	 * @param beginCol
	 * @param maxCol
	 * @param beginRow
	 * @return Object could be String, Boolean, Double, java.util.Date
	 * @throws IOException
	 */
	public static List<List<Object>> readRowsAutoDetectEndCol(
			InputStream inputExcel, boolean isXLSX, int sheetIndex,
			int beginCol, int maxCol, int beginRow) throws IOException {
		Workbook workbook = createWorkbook(inputExcel, isXLSX);

		Sheet sheet = workbook.getSheetAt(sheetIndex);

		return readRowsAutoDetectEndCol(sheet, beginCol, maxCol, beginRow);
	}

	public static List<List<Object>> readRowsAutoDetectEndCol(
			Sheet sheet,
			int beginCol, int maxCol, int beginRow) throws IOException {
		int endRow = sheet.getLastRowNum();
		int endCol = maxCol;
		Row row = null;
		Cell cell = null;
		Object cellVal = null;

		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		
		row = sheet.getRow(beginRow);
		for (int j = 0; j < maxCol; j++) {
			cell = row.getCell(j);
			cellVal = getCellValue(evaluator, cell);
			
			if(cellVal == null || (cellVal.getClass() == String.class && ((String)cellVal).length() == 0)) {
				endCol = j - 1;
				break;
			}
		}

		return readRows(sheet, beginCol, endCol, beginRow, endRow);
	}
	
	public static List<Object> readRowAutoDetectEndCol(Sheet sheet,
			int beginCol, int maxCol, int rowIndex) {
		//int endCol = maxCol;

		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		
		Row row = sheet.getRow(rowIndex);
		Cell cell = null;
		Object cellVal = null;
		List<Object> cellValList = new ArrayList<Object>();
		for (int j = 0; j < maxCol; j++) {
			cell = row.getCell(j);
			cellVal = getCellValue(evaluator, cell);
			
			if(cellVal == null || (cellVal.getClass() == String.class && ((String)cellVal).length() == 0)) {
				//endCol = j - 1;
				break;
			}
			
			cellValList.add(cellVal);
		}
		
		return cellValList;
	}
	
	public static Workbook createWorkbook(InputStream inputExcel, boolean isXLSX) throws IOException {
		if (isXLSX) {
			return new XSSFWorkbook(inputExcel);
		} else {
			return new HSSFWorkbook(inputExcel);
		}
	}

	/**
	 * Include endCol and endRow
	 * 
	 * @param sheet
	 * @param beginCol
	 * @param endCol
	 * @param beginRow
	 * @param endRow
	 * @return Object could be String, Boolean, Double, java.util.Date
	 */
	public static List<List<Object>> readRows(
			Sheet sheet, 
			int beginCol, int endCol, int beginRow, int endRow) {
		List<List<Object>> allRows = new ArrayList<List<Object>>();
		
		Row row = null;

		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		
		int i, j;
		List<Object> cellValList = null;
		for(i = beginRow; i <= endRow; i++) {
			row = sheet.getRow(i);
			
			if(row == null) {
				continue;
			}

			//1 row
			cellValList = readRow(evaluator, row, beginCol, endCol);
			
			allRows.add(cellValList);
		}

		return allRows;
	}
	
	public static List<Object> readRow(
			FormulaEvaluator evaluator,
			Row row,
			int beginCol, int endCol) {
		List<Object> cellValList = new ArrayList<Object>();
		Cell cell = null;
		Object cellVal = null;
		for(int j = beginCol; j <= endCol; j++) {
			cell = row.getCell(j);
			if(cell == null) {
				cellValList.add(null);
			} else {
				cellVal = getCellValue(evaluator, cell);
				cellValList.add(cellVal);
			}
		}
		
		return cellValList;
	}

	/**
	 * 
	 * @param cell
	 * @return Object could be String, Boolean, Double, java.util.Date
	 */
	public static Object getCellValue(
			FormulaEvaluator evaluator,
			Cell cell) {
		if(cell == null) {
			return null;
		}
		
		int cellType = evaluator.evaluateFormulaCell(cell);

		if (cellType == Cell.CELL_TYPE_STRING) {
			
			return cell.getStringCellValue();
		} else if (cellType == Cell.CELL_TYPE_NUMERIC) {
			
			if (DateUtil.isCellDateFormatted(cell)) {
				
				return cell.getDateCellValue();
			} else {
				
				return cell.getNumericCellValue();
			}
		} else if (cellType == Cell.CELL_TYPE_BOOLEAN) {
			
			return cell.getBooleanCellValue();
		} else if (cellType == Cell.CELL_TYPE_BLANK) {
			
			return null;
		} else if (cellType == Cell.CELL_TYPE_ERROR) {
			
			return null;
		} else {
			
			return cell.toString();
		}
	}
	
}
