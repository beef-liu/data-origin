package com.beef.dataorigin.web.dao;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.poi.hssf.record.chart.BeginRecord;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.util.ExcelUtil;
import com.beef.dataorigin.web.data.DODataImportResult;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;

public class DODataImportExportDao {
	private final static Logger logger = Logger.getLogger(DODataImportExportDao.class);
	
	private final static int DEFAULT_MAX_COL = 512;
	
	public static Color BG_COLOR_ROW_INSERTED = Color.yellow; 
	public static Color BG_COLOR_ROW_UPDATED = Color.green; 
	
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
			) {
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
		int col = 0;
		DataImportColMeta colMeta = null;
		for(int i = 0; i < colMataList.size(); i++) {
			col = beginCol + i;
			
			colMeta = colMataList.get(i);
			if(colMeta.dbCol != null && colMeta.metaDataField != null
					&& colMeta.metaDataField.getFieldValidateRegex() != null
					&& colMeta.metaDataField.getFieldValidateRegex().length() > 0) {
				verifyPatternList.add(compiler.compile(colMeta.metaDataField.getFieldValidateRegex()));
			} else {
				verifyPatternList.add(null);
			}
		}
		
		boolean isDuplicatedKey = false;
		for(int i = 1; i < allRowList.size(); i++) {
			isDuplicatedKey = false;
			try {
				insertOneRow(conn, dbTable, colMataList, allRowList.get(i), colValueAssignList);
			} catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
				if("23000".equals(e.getSQLState())) {
					//duplicated key
					isDuplicatedKey = true;
				} else {
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			} catch(MySQLIntegrityConstraintViolationException e) {
				if("23000".equals(e.getSQLState())) {
					//duplicated key
					isDuplicatedKey = true;
				} else {
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			} catch(Throwable e) {
				logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
			}
			if(isDuplicatedKey) {
				//duplicated key, then update
			}
		}
		
		return dataImportResult;
	}
	
	protected static boolean verifyDataRowFormat(
			Sheet sheet, CellStyle cellStyleOfError,
			List<DataImportColMeta> colMataList, List<Pattern> verifyPatternList,  
			List<Object> excelRow, 
			int rowIndex, int beginCol) {
		int i;
		int colIndex = 0;
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
				curRow.getCell(colIndex).setCellStyle(cellStyleOfError);
			}
		}
		
		return true;
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
		} catch(SQLException e) {
			throw e;
		} catch(Exception e) {
			logger.error("insertData()", e);
			return 0;
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
