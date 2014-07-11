package com.beef.dataorigin.web.dao;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.poi.hssf.record.chart.BeginRecord;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.util.ExcelUtil;
import com.beef.dataorigin.web.data.DODataImportResult;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;

public class DODataImportExportDao {
	private final static Logger logger = Logger.getLogger(DODataImportExportDao.class);
	
	private final static int DEFAULT_MAX_COL = 512;
	
	private static final short DEFAULT_BG_COLOR_ERROR = IndexedColors.RED.index;
	private static final short DEFAULT_BG_COLOR_DATA_ROW_INSERTED = IndexedColors.YELLOW.index;
	private static final short DEFAULT_BG_COLOR_DATA_ROW_UPDATED = IndexedColors.GREEN.index;
	
	private static HashMap<String, Short> _bgColorMap;
	static {
		_bgColorMap = new HashMap<String, Short>();
		
		_bgColorMap.put("AQUA".toLowerCase(), Short.valueOf(IndexedColors.AQUA.getIndex()));
		_bgColorMap.put("BLACK".toLowerCase(), Short.valueOf(IndexedColors.BLACK.getIndex()));
		_bgColorMap.put("BLUE".toLowerCase(), Short.valueOf(IndexedColors.BLUE.getIndex()));
		_bgColorMap.put("BLUE_GREY".toLowerCase(), Short.valueOf(IndexedColors.BLUE_GREY.getIndex()));
		_bgColorMap.put("BROWN".toLowerCase(), Short.valueOf(IndexedColors.BROWN.getIndex()));
		_bgColorMap.put("CORAL".toLowerCase(), Short.valueOf(IndexedColors.CORAL.getIndex()));
		_bgColorMap.put("CORNFLOWER_BLUE".toLowerCase(), Short.valueOf(IndexedColors.CORNFLOWER_BLUE.getIndex()));
		_bgColorMap.put("DARK_BLUE".toLowerCase(), Short.valueOf(IndexedColors.DARK_BLUE.getIndex()));
		_bgColorMap.put("DARK_GREEN".toLowerCase(), Short.valueOf(IndexedColors.DARK_GREEN.getIndex()));
		_bgColorMap.put("DARK_RED".toLowerCase(), Short.valueOf(IndexedColors.DARK_RED.getIndex()));
		_bgColorMap.put("DARK_TEAL".toLowerCase(), Short.valueOf(IndexedColors.DARK_TEAL.getIndex()));
		_bgColorMap.put("DARK_YELLOW".toLowerCase(), Short.valueOf(IndexedColors.DARK_YELLOW.getIndex()));
		_bgColorMap.put("GOLD".toLowerCase(), Short.valueOf(IndexedColors.GOLD.getIndex()));
		_bgColorMap.put("GREEN".toLowerCase(), Short.valueOf(IndexedColors.GREEN.getIndex()));
		_bgColorMap.put("INDIGO".toLowerCase(), Short.valueOf(IndexedColors.INDIGO.getIndex()));
		_bgColorMap.put("LAVENDER".toLowerCase(), Short.valueOf(IndexedColors.LAVENDER.getIndex()));
		_bgColorMap.put("LEMON_CHIFFON".toLowerCase(), Short.valueOf(IndexedColors.LEMON_CHIFFON.getIndex()));
		_bgColorMap.put("LIGHT_BLUE".toLowerCase(), Short.valueOf(IndexedColors.LIGHT_BLUE.getIndex()));
		_bgColorMap.put("LIGHT_CORNFLOWER_BLUE".toLowerCase(), Short.valueOf(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()));
		_bgColorMap.put("LIGHT_GREEN".toLowerCase(), Short.valueOf(IndexedColors.LIGHT_GREEN.getIndex()));
		_bgColorMap.put("LIGHT_ORANGE".toLowerCase(), Short.valueOf(IndexedColors.LIGHT_ORANGE.getIndex()));
		_bgColorMap.put("LIGHT_TURQUOISE".toLowerCase(), Short.valueOf(IndexedColors.LIGHT_TURQUOISE.getIndex()));
		_bgColorMap.put("LIGHT_YELLOW".toLowerCase(), Short.valueOf(IndexedColors.LIGHT_YELLOW.getIndex()));
		_bgColorMap.put("LIME".toLowerCase(), Short.valueOf(IndexedColors.LIME.getIndex()));
		_bgColorMap.put("MAROON".toLowerCase(), Short.valueOf(IndexedColors.MAROON.getIndex()));
		_bgColorMap.put("OLIVE_GREEN".toLowerCase(), Short.valueOf(IndexedColors.OLIVE_GREEN.getIndex()));
		_bgColorMap.put("ORANGE".toLowerCase(), Short.valueOf(IndexedColors.ORANGE.getIndex()));
		_bgColorMap.put("ORCHID".toLowerCase(), Short.valueOf(IndexedColors.ORCHID.getIndex()));
		_bgColorMap.put("PALE_BLUE".toLowerCase(), Short.valueOf(IndexedColors.PALE_BLUE.getIndex()));
		_bgColorMap.put("PINK".toLowerCase(), Short.valueOf(IndexedColors.PINK.getIndex()));
		_bgColorMap.put("PLUM".toLowerCase(), Short.valueOf(IndexedColors.PLUM.getIndex()));
		_bgColorMap.put("RED".toLowerCase(), Short.valueOf(IndexedColors.RED.getIndex()));
		_bgColorMap.put("ROSE".toLowerCase(), Short.valueOf(IndexedColors.ROSE.getIndex()));
		_bgColorMap.put("ROYAL_BLUE".toLowerCase(), Short.valueOf(IndexedColors.ROYAL_BLUE.getIndex()));
		_bgColorMap.put("SEA_GREEN".toLowerCase(), Short.valueOf(IndexedColors.SEA_GREEN.getIndex()));
		_bgColorMap.put("SKY_BLUE".toLowerCase(), Short.valueOf(IndexedColors.SKY_BLUE.getIndex()));
		_bgColorMap.put("TEAL".toLowerCase(), Short.valueOf(IndexedColors.TEAL.getIndex()));
		_bgColorMap.put("TURQUOISE".toLowerCase(), Short.valueOf(IndexedColors.TURQUOISE.getIndex()));
		_bgColorMap.put("VIOLET".toLowerCase(), Short.valueOf(IndexedColors.VIOLET.getIndex()));
		_bgColorMap.put("WHITE".toLowerCase(), Short.valueOf(IndexedColors.WHITE.getIndex()));
		_bgColorMap.put("YELLOW".toLowerCase(), Short.valueOf(IndexedColors.YELLOW.getIndex()));
		
	}
	
	private static short getExcelBGColor(String colorName, short defaultColor) {
		if(colorName == null || colorName.length() == 0) {
			return defaultColor;
		}
		
		Short color = _bgColorMap.get(colorName.toLowerCase());
		if(color == null) {
			return defaultColor;
		} else {
			return color.shortValue();
		}
	}
	
	public static DODataImportResult importDataExcel(
			Connection conn,
			InputStream inputExcel,
			String originalFileName,
			boolean isXLSX, int sheetIndex,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			List<DataImportColValue> colValueAssignList
			) throws IOException {
		int beginCol = 0;
		int maxCol = DEFAULT_MAX_COL;
		int beginRow = 0;

		Workbook workbook = ExcelUtil.createWorkbook(inputExcel, isXLSX);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		
		List<List<Object>> allRowList = ExcelUtil.readRowsAutoDetectEndCol(sheet, beginCol, maxCol, beginRow);
	
		return importDataExcel(conn, originalFileName, sheet, allRowList, dataImportSetting, dbTable, colValueAssignList);
	}
	
	public static DODataImportResult importDataExcel(
			Connection conn,
			Sheet sheet,
			String originalFileName,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			List<DataImportColValue> colValueAssignList
			) throws IOException {
		int beginCol = 0;
		int maxCol = DEFAULT_MAX_COL;
		int beginRow = 0;
		
		List<List<Object>> allRowList = ExcelUtil.readRowsAutoDetectEndCol(sheet, beginCol, maxCol, beginRow);
	
		return importDataExcel(conn, originalFileName, sheet, allRowList, dataImportSetting, dbTable, colValueAssignList);
	}
	
	protected static DODataImportResult importDataExcel(
			Connection conn,
			String originalFileName,
			Sheet sheet, int beginCol,
			List<List<Object>> allRowList,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			List<DataImportColValue> colValueAssignList
			) throws MalformedPatternException {
		DODataImportResult dataImportResult = new DODataImportResult();
		dataImportResult.setOriginalFileName(originalFileName);
		dataImportResult.setTableName(dbTable.getTableName());
		dataImportResult.setTableComment(dbTable.getComment());
		dataImportResult.setTotalCount(0);
		dataImportResult.setInsertedCount(0);
		dataImportResult.setUpdatedCount(0);
		dataImportResult.setErrorCount(0);

		List<DataImportColMeta> colMataList = findoutDataImportColMetaListOfExcelTitleRow(
				dataImportSetting, dbTable, allRowList.get(0));
		PatternCompiler compiler = new Perl5Compiler();
		List<Pattern> verifyPatternList = new ArrayList<Pattern>();
		DataImportColMeta colMeta = null;
		for(int i = 0; i < colMataList.size(); i++) {
			colMeta = colMataList.get(i);
			if(colMeta.dbCol != null && colMeta.metaDataField != null
					&& colMeta.metaDataField.getFieldValidateRegex() != null
					&& colMeta.metaDataField.getFieldValidateRegex().length() > 0) {
				verifyPatternList.add(compiler.compile(colMeta.metaDataField.getFieldValidateRegex()));
			} else {
				verifyPatternList.add(null);
			}
		}

		int endCol = beginCol + colMataList.size();
		boolean isDuplicatedKey = false;
		boolean isValidRow = false;
		int updateCnt = 0;
		boolean isUpdate = false;
		String errorMsg = null;

		CellStyle cellStyleOfError = sheet.getWorkbook().createCellStyle();
		cellStyleOfError.setFillBackgroundColor(getExcelBGColor(dataImportSetting.getBgColorError(), DEFAULT_BG_COLOR_ERROR));

		CellStyle cellStyleOfInserted = sheet.getWorkbook().createCellStyle();
		cellStyleOfInserted.setFillBackgroundColor(getExcelBGColor(dataImportSetting.getBgColorDataRowInserted(), DEFAULT_BG_COLOR_DATA_ROW_INSERTED));

		CellStyle cellStyleOfUpdated = sheet.getWorkbook().createCellStyle();
		cellStyleOfUpdated.setFillBackgroundColor(getExcelBGColor(dataImportSetting.getBgColorDataRowUpdated(), DEFAULT_BG_COLOR_DATA_ROW_UPDATED));
		
		for(int i = 1; i < allRowList.size(); i++) {
			//verify data row
			isValidRow = verifyDataRowFormat(
					sheet, cellStyleOfError, colMataList, 
					verifyPatternList, allRowList.get(i), i, beginCol);
			if(!isValidRow) {
				continue;
			}
			
			//update to DB ------------------
			isDuplicatedKey = false;
			updateCnt = 0;
			isUpdate = false;
			errorMsg = null;
			try {
				updateCnt = insertOneRow(conn, dbTable, colMataList, allRowList.get(i), colValueAssignList);
			} catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
				if("23000".equals(e.getSQLState())) {
					//duplicated key
					isDuplicatedKey = true;
				} else {
					errorMsg = DOServiceMsgUtil.getStackTrace(e);
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			} catch(MySQLIntegrityConstraintViolationException e) {
				if("23000".equals(e.getSQLState())) {
					//duplicated key
					isDuplicatedKey = true;
				} else {
					errorMsg = DOServiceMsgUtil.getStackTrace(e);
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			} catch(Throwable e) {
				errorMsg = DOServiceMsgUtil.getStackTrace(e);
				logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
			}
			
			if(isDuplicatedKey) {
				//duplicated key, then update
				try {
					isUpdate = true;
					updateCnt = updateOneRow(conn, dbTable, colMataList, allRowList.get(i), colValueAssignList);
					
					if(updateCnt == 0) {
						errorMsg = DOServiceMsgUtil.getDefinedMsg(DOServiceMsgUtil.ErrorDataImportUpdateFailDataNotExist);
					}
				} catch(Throwable e) {
					errorMsg = DOServiceMsgUtil.getStackTrace(e);
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			}
			
			if(updateCnt == 0) {
				//fail
				setCellStyleToRow(sheet.getRow(i), cellStyleOfError, beginCol, endCol);
				sheet.getRow(i).getCell(endCol + 1).setCellValue(errorMsg);
				sheet.getRow(i).getCell(endCol + 1).setCellStyle(cellStyleOfError);
			} else {
				//success
				if(isUpdate) {
					setCellStyleToRow(sheet.getRow(i), cellStyleOfUpdated, beginCol, endCol);
				} else {
					setCellStyleToRow(sheet.getRow(i), cellStyleOfInserted, beginCol, endCol);
				}
			}
		}
		
		return dataImportResult;
	}
	
	private static void setCellStyleToRow(
			Row row,
			CellStyle cellStyle,
			int beginCol, int endCol) {
		for(int i = beginCol; i <= endCol; i++) {
			row.getCell(i).setCellStyle(cellStyle);
		}
	}
	
	protected static boolean verifyDataRowFormat(
			Sheet sheet, CellStyle cellStyleOfError,
			List<DataImportColMeta> colMataList, List<Pattern> verifyPatternList,  
			List<Object> excelRow, 
			int rowIndex, int beginCol) {
		int i;
		int colIndex = 0;
		int colForErrorMsg = beginCol + colMataList.size() + 1;
		StringBuilder errorMsg = new StringBuilder();
		DataImportColMeta colMeta = null;
		Object dbVal;
		String dbValStr;
		String verifyRegexStr;
		boolean isValidCol = false;
		Row curRow = sheet.getRow(rowIndex);
		for(i = 0; i < colMataList.size(); i++) {
			colIndex = beginCol + i;
			colMeta = colMataList.get(i);
			
			if(colMeta.dbCol == null) {
				continue;
			}
			verifyRegexStr = colMeta.metaDataField.getFieldValidateRegex();
			if(verifyRegexStr == null || verifyRegexStr.length() == 0) {
				continue;
			}
			
			dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.dbCol);
			if(dbVal.getClass() == String.class) {
				dbValStr = (String) dbVal; 
			} else {
				dbValStr = String.valueOf(dbVal);
			}
			
			//verify
			isValidCol = DODataDaoUtil.isFormatOfPattern(verifyPatternList.get(i), dbValStr);
			if(!isValidCol) {
				//is invalid cell, make color, and output error msg
				curRow.getCell(colIndex).setCellStyle(cellStyleOfError);
				
				errorMsg.append(colMeta.dbCol.getName() + "(" + colMeta.dbCol.getComment() + ")" + ":" + colMeta.metaDataField.getFieldValidateComment())
					.append("\n");
			}
		}
		
		//set error msg
		if(errorMsg.length() > 0) {
			curRow.getCell(colForErrorMsg).setCellValue(errorMsg.toString());
			return false;
		} else {
			return true;
		}
	}
	
	
	protected static int insertOneRow(
			Connection conn,
			DBTable dbTable,
			List<DataImportColMeta> colMataList, List<Object> excelRow,
			List<DataImportColValue> colValueAssignList) throws SQLException {
		PreparedStatement pstmt = null;

		try {
			String tableName = dbTable.getTableName();
			
			int index;
			int i;
			StringBuilder sql = new StringBuilder();
			sql.append("insert into ").append(tableName).append(" (");

			DataImportColMeta colMeta = null;
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);
				
				if(colMeta.dbCol == null) {
					continue;
				}
				
				if(index == 0) {
					sql.append(colMeta.dbCol.getName());
				} else {
					sql.append(",").append(colMeta.dbCol.getName());
				}
				
				index++;
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					if(index == 0) {
						sql.append(importColVal.dbCol.getName());
					} else {
						sql.append(",").append(importColVal.dbCol.getName());
					}
					
					index++;
				}
			}
			
			sql.append(") values (");
			
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.dbCol == null) {
					continue;
				}

				if(index == 0) {
					sql.append("?");
				} else {
					sql.append(",?");
				}
				
				index++;
			}
			if(colValueAssignList != null) {
				for(i = 0; i < colValueAssignList.size(); i++) {
					if(index == 0) {
						sql.append("?");
					} else {
						sql.append(",?");
					}
					
					index++;
				}
			}

			sql.append(")");

			logger.debug("insertOneRow():" + sql.toString());
			
			pstmt = conn.prepareStatement(sql.toString());

			index = 1;
			Object dbVal = null;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.dbCol == null) {
					continue;
				}

				dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.dbCol);
				pstmt.setObject(index++, dbVal);
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					pstmt.setObject(index++, importColVal.dbVal);
				}
			}
			
		 	return pstmt.executeUpdate();
		} finally {
			try {
				pstmt.close();
			} catch(Exception e) {
			}
		}
	}

	protected static int updateOneRow(
			Connection conn,
			DBTable dbTable,
			List<DataImportColMeta> colMataList, List<Object> excelRow,
			List<DataImportColValue> colValueAssignList) throws SQLException {
		PreparedStatement pstmt = null;

		try {
			String tableName = dbTable.getTableName();
			
			int index;
			int i;
			StringBuilder sql = new StringBuilder();
			sql.append("update ").append(tableName).append(" set ");

			DataImportColMeta colMeta = null;
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);
				
				if(colMeta.dbCol == null) {
					continue;
				}
				if(colMeta.dbCol.isPrimaryKey()) {
					continue;
				}
				
				if(index > 0) {
					sql.append(",");
				}
				sql.append(colMeta.dbCol.getName()).append(" = ? ");
				
				index++;
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					if(importColVal.dbCol.isPrimaryKey()) {
						continue;
					}
					
					if(index > 0) {
						sql.append(",");
					}
					sql.append(importColVal.dbCol.getName()).append(" = ? ");
					
					index++;
				}
			}
			
			sql.append(" where ");
			
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.dbCol == null) {
					continue;
				}
				if(!colMeta.dbCol.isPrimaryKey()) {
					continue;
				}

				if(index > 0) {
					sql.append(" and ");
				}
				sql.append(colMeta.dbCol.getName()).append(" = ?");
				
				index++;
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					if(!importColVal.dbCol.isPrimaryKey()) {
						continue;
					}
					
					if(index > 0) {
						sql.append(" and ");
					}
					sql.append(importColVal.dbCol.getName()).append(" = ? ");
					
					index++;
				}
			}

			logger.debug("updateOneRow():" + sql.toString());
			
			pstmt = conn.prepareStatement(sql.toString());

			index = 1;
			Object dbVal = null;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.dbCol == null) {
					continue;
				}
				if(colMeta.dbCol.isPrimaryKey()) {
					continue;
				}

				dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.dbCol);
				pstmt.setObject(index++, dbVal);
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);

					if(importColVal.dbCol.isPrimaryKey()) {
						continue;
					}
					
					pstmt.setObject(index++, importColVal.dbVal);
				}
			}

			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.dbCol == null) {
					continue;
				}
				if(!colMeta.dbCol.isPrimaryKey()) {
					continue;
				}

				dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.dbCol);
				pstmt.setObject(index++, dbVal);
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);

					if(!importColVal.dbCol.isPrimaryKey()) {
						continue;
					}
					
					pstmt.setObject(index++, importColVal.dbVal);
				}
			}
		
		 	return pstmt.executeUpdate();
		} finally {
			try {
				pstmt.close();
			} catch(Exception e) {
			}
		}
	}
	
	protected static Object getDBValueFromExcelValue(Object excelCellVal, DBColumn dbCol) {
		String colType = dbCol.getColumnType().toLowerCase();
		
		if(excelCellVal == null) {
			return null;
		} else {
			if(excelCellVal.getClass() == String.class) {
				return excelCellVal;
			} else if(excelCellVal.getClass() == Double.class) {
				if(colType.startsWith("int")) {
					return Integer.valueOf(((Double)excelCellVal).intValue());
				} else if(colType.startsWith("bigint")) {
					return Long.valueOf(((Double)excelCellVal).longValue());
				} else if(colType.indexOf("char") >= 0) {
					return String.valueOf(excelCellVal);
				} else {
					return excelCellVal;
				}
			} else if(excelCellVal.getClass() == Date.class) {
				//TODO
				return String.valueOf(excelCellVal); 
			} else if(excelCellVal.getClass() == Boolean.class) {
				if(colType.indexOf("int") >= 0) {
					if(((Boolean)excelCellVal).booleanValue()) {
						return 1;
					} else {
						return 0;
					}
				} else {
					return String.valueOf(excelCellVal);
				}
			} else {
				return String.valueOf(excelCellVal);
			}
		}
	}
	
	protected static List<DataImportColMeta> findoutDataImportColMetaListOfExcelTitleRow(
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			List<Object> titleRow) {
		List<DataImportColMeta> colMetaList = new ArrayList<DODataImportExportDao.DataImportColMeta>();
		
		MetaDataField dataField = null;
		DBColumn dbCol = null;
		DataImportColMeta importColMeta = null;
		
		Object title = null;
		for(int j = 0; j < titleRow.size(); j++) {
			title = titleRow.get(j);
			
			importColMeta = new DataImportColMeta();
			
			dataField = findoutMetaDataFieldByExcelTitle(dataImportSetting, title);
			if(dataField != null) {
				importColMeta.metaDataField = dataField;

				dbCol = findoutDBColumnByColName(dbTable, dataField.getFieldName());
				if(dbCol != null) {
					importColMeta.dbCol = dbCol;
				}
			}
			
			colMetaList.add(importColMeta);
		}
		
		return colMetaList;
	}
	
	protected static MetaDataField findoutMetaDataFieldByExcelTitle(MetaDataImportSetting dataImportSetting, Object title) {
		if(title == null) {
			return null;
		}
		
		String titleStr = String.valueOf(title);
		MetaDataField dataField = null;
		for(int i = 0; i < dataImportSetting.getFieldList().size(); i++) {
			dataField = dataImportSetting.getFieldList().get(i);
			
			if(dataField.getFieldDispName().toLowerCase().equals(titleStr)) {
				return dataField;
			}
		}
		
		return null;
	}
	
	protected static DBColumn findoutDBColumnByColName(DBTable dbTable, String colName) {
		DBColumn dbCol = null;
		for(int i = 0; i < dbTable.getColumns().size(); i++) {
			dbCol = dbTable.getColumns().get(i);
			
			if(dbCol.getName().equalsIgnoreCase(colName)) {
				return dbCol;
			}
		}
		
		return null;
	}

	protected static class DataImportColMeta {
		public DBColumn dbCol = null;
		public MetaDataField metaDataField = null;
	}
	
	public static class DataImportColValue extends DataImportColMeta {
		public Object dbVal = null;
	}
	
}
