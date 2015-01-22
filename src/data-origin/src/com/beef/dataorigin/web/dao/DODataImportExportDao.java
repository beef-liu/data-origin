package com.beef.dataorigin.web.dao;

import java.awt.Color;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataImportSetting;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.util.ExcelUtil;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.data.DODataExportResult;
import com.beef.dataorigin.web.data.DODataImportColMetaInfo;
import com.beef.dataorigin.web.data.DODataImportResult;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.data.DOSearchConditionItem;
import com.beef.dataorigin.web.datacommittask.dao.DODataModificationCommitTaskSchedulerDao;
import com.beef.dataorigin.web.datacommittask.dao.DODataModificationCommitTaskSchedulerDao.DataModificationCommitTaskModType;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOSqlParamUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;

public class DODataImportExportDao {
	private final static Logger logger = Logger.getLogger(DODataImportExportDao.class);
	
	public final static int DEFAULT_MAX_COL = 512;
	
	private static final short DEFAULT_BG_COLOR_ERROR = IndexedColors.ORANGE.index;
	private static final short DEFAULT_BG_COLOR_DATA_ROW_INSERTED = IndexedColors.LIGHT_YELLOW.index;
	private static final short DEFAULT_BG_COLOR_DATA_ROW_UPDATED = IndexedColors.LIME.index;
	
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
	
	/**
	 * 
	 * @param conn
	 * @param inputExcel template excel
	 * @param originalFileName
	 * @param isXLSX
	 * @param sheetIndex
	 * @param dataImportSetting
	 * @param dbTable
	 * @param colValueAssignList
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	public static DODataExportResult exportDataExcel(
			Connection conn,
			Workbook workbook,
			OutputStream outputExcel,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			DOSearchCondition searchCondition
			) throws IOException, SQLException, ParseException {
		Sheet sheet = workbook.getSheetAt(0);
		
		int beginCol = 0;
		int maxCol = DEFAULT_MAX_COL;
		//Cell cell = null;
		//MetaDataField dataField = null;
		MMetaDataImportSetting mMetaDataImportSetting = DataOriginWebContext.getDataOriginContext().getMMetaDataImportSetting(
				dbTable.getTableName());
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(
				dbTable.getTableName());

		//read title ----------------------------------
		List<Object> titleRowValList = ExcelUtil.readRowAutoDetectEndCol(sheet, beginCol, maxCol, 0);
		List<DODataImportColMetaInfo> colMataList = findoutDataImportColMetaListOfExcelTitleRow(
				dataImportSetting, dbTable, titleRowValList);
		
		DODataExportResult exportResult = new DODataExportResult();
		exportResult.setTableName(dbTable.getTableName());
		exportResult.setTableComment(dbTable.getComment());

		//content -------------------------------------
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select * from " + dbTable.getTableName());
			
			//where conditions -------------------------------------------------------------
			List<Object> colValueListForStmt = new ArrayList<Object>();
			StringBuilder sqlWhereConditions = new StringBuilder();
			DOSearchConditionItem searchConditionItem;
			for(int i = 0; i < searchCondition.getSearchConditionItemList().size(); i++) {
				searchConditionItem = searchCondition.getSearchConditionItemList().get(i);
				
				DODataDao.addSearchConditionItem(
						mMetaDataImportSetting.getFieldMap(), mDBTable, colValueListForStmt, 
						sqlWhereConditions, searchConditionItem);
			}
			
			if(sqlWhereConditions.length() > 0) {
				sql.append(" where ");
				sql.append(sqlWhereConditions);
			}
			
			logger.debug("sql:" + sql.toString());
			
			
			stmt = conn.prepareStatement(sql.toString());
			
			int index = 1;
			for(int i = 0; i < colValueListForStmt.size(); i++) {
				stmt.setObject(index++, colValueListForStmt.get(i));
			}
			
			rs = stmt.executeQuery();

			int rowIndex = 1;
			Row row = null;
			Cell cell = null;
			int col;
			String cellVal;
			String fieldName;
			while(rs.next()) {
				row = sheet.getRow(rowIndex);
				if(row == null) {
					row = sheet.createRow(rowIndex);
				}
				
				DODataImportColMetaInfo colMetaInfo = null;
				for(col = beginCol; col < colMataList.size(); col++) {
					colMetaInfo = colMataList.get(col);
					if(colMetaInfo.getDbCol() == null) {
						continue;
					}
					
					cell = row.getCell(col);
					if(cell == null) {
						cell = row.createCell(col);
					}
					
					fieldName = colMetaInfo.getMetaDataField().getFieldName();
					cellVal = formatDBValueToExcelValue(
							rs.getString(fieldName), 
							mDBTable.getColumnMap().get(fieldName), 
							mMetaDataImportSetting.getFieldMap().get(fieldName));
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(cellVal);
				}
				
				rowIndex ++;
			}

			exportResult.setTotalCount(rowIndex - 1);
		} finally {
			stmt.close();
		}

		workbook.write(outputExcel);
		return exportResult;
	} 
	
	public static DODataImportResult importDataExcel(
			Connection conn,
			Workbook workbook,
			int sheetIndex,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			MDBTable mDBTable,
			List<DataImportColValue> colValueAssignList
			) throws IOException, MalformedPatternException, ParseException, IllegalArgumentException, SQLException, InstantiationException, InvocationTargetException, IllegalAccessException, IntrospectionException {
		int beginCol = 0;
		int maxCol = DEFAULT_MAX_COL;
		int beginRow = 0;

		Sheet sheet = workbook.getSheetAt(sheetIndex);
		
		List<List<Object>> allRowList = ExcelUtil.readRowsAutoDetectEndCol(sheet, beginCol, maxCol, beginRow);
	
		return importDataExcel(conn, 
				//originalFileName, 
				sheet, beginCol, 
				allRowList, dataImportSetting, dbTable, mDBTable, colValueAssignList);
	}
	
	public static DODataImportResult importDataExcel(
			Connection conn,
			Sheet sheet,
			//String originalFileName,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			MDBTable mDBTable,
			List<DataImportColValue> colValueAssignList
			) throws IOException, MalformedPatternException, ParseException, IllegalArgumentException, SQLException, InstantiationException, InvocationTargetException, IllegalAccessException, IntrospectionException {
		int beginCol = 0;
		int maxCol = DEFAULT_MAX_COL;
		int beginRow = 0;
		
		List<List<Object>> allRowList = ExcelUtil.readRowsAutoDetectEndCol(sheet, beginCol, maxCol, beginRow);
	
		return importDataExcel(conn, 
				//originalFileName, 
				sheet, beginCol, 
				allRowList, dataImportSetting, dbTable, mDBTable, colValueAssignList);
	}
	
	protected static DODataImportResult importDataExcel(
			Connection conn,
			//String originalFileName,
			Sheet sheet, int beginCol,
			List<List<Object>> allRowList,
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			MDBTable mDBTable,
			List<DataImportColValue> colValueAssignList
			) throws MalformedPatternException, ParseException, IllegalArgumentException, SQLException, 
			InstantiationException, InvocationTargetException, IllegalAccessException, IntrospectionException {
		
		DODataImportResult dataImportResult = new DODataImportResult();
		//dataImportResult.setOriginalFileName(originalFileName);
		dataImportResult.setTableName(dbTable.getTableName());
		dataImportResult.setTableComment(dbTable.getComment());
		
		dataImportResult.setTotalCount(allRowList.size() - 1);

		int insertedRowCount = 0;
		int updatedRowCount = 0;
		int errorRowCount = 0;
		
		List<DODataImportColMetaInfo> colMataList = findoutDataImportColMetaListOfExcelTitleRow(
				dataImportSetting, dbTable, allRowList.get(0));
		PatternCompiler compiler = new Perl5Compiler();
		List<Pattern> verifyPatternList = new ArrayList<Pattern>();
		DODataImportColMetaInfo colMeta = null;
		for(int i = 0; i < colMataList.size(); i++) {
			colMeta = colMataList.get(i);
			if(colMeta.getDbCol() != null && colMeta.getMetaDataField() != null
					&& colMeta.getMetaDataField().getFieldValidateRegex() != null
					&& colMeta.getMetaDataField().getFieldValidateRegex().length() > 0) {
				verifyPatternList.add(compiler.compile(colMeta.getMetaDataField().getFieldValidateRegex()));
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
		
		//TODO
		String adminId = "";
		long schedule_commit_time = DataOriginWebContext.getDefaultDataModificationCommitScheduleTime();
		//MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(dbTable.getTableName());
		DataModificationCommitTaskModType modType = DataModificationCommitTaskModType.ModTypeInsert;

		CellStyle cellStyleOfError = sheet.getWorkbook().createCellStyle();
		cellStyleOfError.setFillForegroundColor(getExcelBGColor(dataImportSetting.getBgColorError(), DEFAULT_BG_COLOR_ERROR));
		cellStyleOfError.setFillPattern(CellStyle.SOLID_FOREGROUND);

		CellStyle cellStyleOfInserted = sheet.getWorkbook().createCellStyle();
		cellStyleOfInserted.setFillForegroundColor(getExcelBGColor(dataImportSetting.getBgColorDataRowInserted(), DEFAULT_BG_COLOR_DATA_ROW_INSERTED));
		cellStyleOfInserted.setFillPattern(CellStyle.SOLID_FOREGROUND);

		CellStyle cellStyleOfUpdated = sheet.getWorkbook().createCellStyle();
		cellStyleOfUpdated.setFillForegroundColor(getExcelBGColor(dataImportSetting.getBgColorDataRowUpdated(), DEFAULT_BG_COLOR_DATA_ROW_UPDATED));
		cellStyleOfUpdated.setFillPattern(CellStyle.SOLID_FOREGROUND);

		StringBuilder outputSqlConditionOfPrimaryKeys = new StringBuilder();
		
		for(int i = 1; i < allRowList.size(); i++) {
			//verify data row
			isValidRow = verifyDataRowFormat(
					sheet, cellStyleOfError, colMataList, 
					verifyPatternList, allRowList.get(i), i, beginCol);
			if(!isValidRow) {
				errorRowCount ++;
				continue;
			}
			
			//update to DB ------------------
			isDuplicatedKey = false;
			updateCnt = 0;
			isUpdate = false;
			errorMsg = null;
			try {
				updateCnt = insertOneRow(conn, dbTable, colMataList, allRowList.get(i), colValueAssignList);
				
				modType = DataModificationCommitTaskModType.ModTypeInsert;
			} catch(Throwable e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("MySQLIntegrityConstraintViolationException")) {
					//duplicated key
					isDuplicatedKey = true;
				} else {
					errorMsg = DOServiceMsgUtil.getStackTrace(e);
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			}
			
			if(isDuplicatedKey) {
				//duplicated key, then update
				try {
					isUpdate = true;
					updateCnt = updateOneRow(conn, dbTable, colMataList, allRowList.get(i), colValueAssignList);
					
					if(updateCnt == 0) {
						errorMsg = DOServiceMsgUtil.getDefinedMsg(DOServiceMsgUtil.ErrorDataImportUpdateFailDataNotExist).getMsg();
					} else {
						modType = DataModificationCommitTaskModType.ModTypeUpdate;
					}
				} catch(Throwable e) {
					errorMsg = DOServiceMsgUtil.getStackTrace(e);
					logger.error("importDataExcel() Error at line(from 1):" + (i+1), e);
				}
			}
			
			if(updateCnt == 0) {
				//fail
				setCellStyleToRow(sheet.getRow(i), cellStyleOfError, beginCol, endCol);
				Cell cell = sheet.getRow(i).getCell(endCol + 1);
				if(cell == null) {
					cell = sheet.getRow(i).createCell(endCol + 1);
				}
				cell.setCellValue(errorMsg);
				cell.setCellStyle(cellStyleOfError);
				
				errorRowCount++;
			} else {
				makeSqlConditionOfPrimaryKey(
						outputSqlConditionOfPrimaryKeys, dbTable, colMataList, allRowList.get(i), colValueAssignList);
				DODataModificationCommitTaskSchedulerDao.createDataCommitTaskBySqlPK(
						conn, mDBTable, outputSqlConditionOfPrimaryKeys.toString(), modType, schedule_commit_time, adminId);

				//success
				if(isUpdate) {
					setCellStyleToRow(sheet.getRow(i), cellStyleOfUpdated, beginCol, endCol);
					
					updatedRowCount++;
				} else {
					setCellStyleToRow(sheet.getRow(i), cellStyleOfInserted, beginCol, endCol);
					
					insertedRowCount++;
				}
			}
		}

		DODataModificationCommitTaskSchedulerDao.refreshDataCommitTaskBundle(conn, dbTable.getTableName(), schedule_commit_time);
		
		dataImportResult.setInsertedCount(insertedRowCount);
		dataImportResult.setUpdatedCount(updatedRowCount);
		dataImportResult.setErrorCount(errorRowCount);
		
		return dataImportResult;
	}
	
	private static void setCellStyleToRow(
			Row row,
			CellStyle cellStyle,
			int beginCol, int endCol) {
		Cell cell;
		for(int i = beginCol; i <= endCol; i++) {
			cell = row.getCell(i);
			if(cell == null) {
				cell = row.createCell(i);
			}
			cell.setCellStyle(cellStyle);
		}
	}
	
	protected static boolean verifyDataRowFormat(
			Sheet sheet, CellStyle cellStyleOfError,
			List<DODataImportColMetaInfo> colMataList, List<Pattern> verifyPatternList,  
			List<Object> excelRow, 
			int rowIndex, int beginCol) throws ParseException {
		int i;
		int colIndex = 0;
		int colForErrorMsg = beginCol + colMataList.size() + 1;
		StringBuilder errorMsg = new StringBuilder();
		DODataImportColMetaInfo colMeta = null;
		Object dbVal;
		String dbValStr;
		String verifyRegexStr;
		boolean isValidCol = false;
		Row curRow = sheet.getRow(rowIndex);
		for(i = 0; i < colMataList.size(); i++) {
			colIndex = beginCol + i;
			colMeta = colMataList.get(i);
			
			if(colMeta.getDbCol() == null) {
				continue;
			}
			verifyRegexStr = colMeta.getMetaDataField().getFieldValidateRegex();
			if(verifyRegexStr == null || verifyRegexStr.length() == 0) {
				continue;
			}
			
			dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.getDbCol());
			if(dbVal == null) {
				return true;
			} else {
				if(dbVal.getClass() == String.class) {
					dbValStr = (String) dbVal; 
				} else {
					dbValStr = String.valueOf(dbVal);
				}
			}
			
			//verify
			isValidCol = DODataDaoUtil.isFormatOfPattern(verifyPatternList.get(i), dbValStr);
			if(!isValidCol) {
				//is invalid cell, make color, and output error msg
				curRow.getCell(colIndex).setCellStyle(cellStyleOfError);
				
				errorMsg.append(colMeta.getDbCol().getName() + "(" + colMeta.getDbCol().getComment() + ")" + ":" + colMeta.getMetaDataField().getFieldValidateComment())
					.append("\n");
			}
		}
		
		//set error msg
		if(errorMsg.length() > 0) {
			Cell cell = curRow.getCell(colForErrorMsg);
			if(cell == null) {
				cell = curRow.createCell(colForErrorMsg);
			}
			cell.setCellValue(errorMsg.toString());
			return false;
		} else {
			return true;
		}
	}
	
	
	protected static int insertOneRow(
			Connection conn,
			DBTable dbTable,
			List<DODataImportColMetaInfo> colMataList, List<Object> excelRow,
			List<DataImportColValue> colValueAssignList
		) throws SQLException, ParseException {
		PreparedStatement pstmt = null;

		try {
			String tableName = dbTable.getTableName();
			
			int index;
			int i;
			StringBuilder sql = new StringBuilder();
			sql.append("insert into ").append(DOSqlParamUtil.quoteSqlIdentifier(tableName)).append(" (");

			DODataImportColMetaInfo colMeta = null;
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);
				
				if(colMeta.getDbCol() == null) {
					continue;
				}
				
				if(index == 0) {
					sql.append(DOSqlParamUtil.quoteSqlIdentifier(colMeta.getDbCol().getName()));
				} else {
					sql.append(",").append(DOSqlParamUtil.quoteSqlIdentifier(colMeta.getDbCol().getName()));
				}
				
				index++;
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					if(index == 0) {
						sql.append(DOSqlParamUtil.quoteSqlIdentifier(importColVal.getDbCol().getName()));
					} else {
						sql.append(",").append(DOSqlParamUtil.quoteSqlIdentifier(importColVal.getDbCol().getName()));
					}
					
					index++;
				}
			}
			
			sql.append(") values (");
			
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.getDbCol() == null) {
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

				if(colMeta.getDbCol() == null) {
					continue;
				}

				dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.getDbCol());
				pstmt.setObject(index++, dbVal);
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					dbVal = getDBValueFromExcelValue(importColVal.dbVal, importColVal.getDbCol());
					pstmt.setObject(index++, dbVal);
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
	
	protected static void makeSqlConditionOfPrimaryKey(
			StringBuilder outputSqlConditionOfPrimaryKeys,
			DBTable dbTable,
			List<DODataImportColMetaInfo> colMataList, List<Object> excelRow,
			List<DataImportColValue> colValueAssignList
			) throws ParseException {
		if(outputSqlConditionOfPrimaryKeys.length() > 0) {
			outputSqlConditionOfPrimaryKeys.delete(0, outputSqlConditionOfPrimaryKeys.length());
		}
		
		int i;
		Object dbVal = null;
		
		for(i = 0; i < colMataList.size(); i++) {
			DODataImportColMetaInfo colMeta = colMataList.get(i);

			if(colMeta.getDbCol() == null) {
				continue;
			}
			if(!colMeta.getDbCol().isPrimaryKey()) {
				continue;
			}

			dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.getDbCol());
			
			//sql condition
			DODataDao.appendSqlConditionItem(outputSqlConditionOfPrimaryKeys, colMeta.getDbCol(), dbVal);
		}
		if(colValueAssignList != null) {
			DataImportColValue importColVal = null;
			for(i = 0; i < colValueAssignList.size(); i++) {
				importColVal = colValueAssignList.get(i);
				
				if(!importColVal.getDbCol().isPrimaryKey()) {
					continue;
				}
				
				dbVal = getDBValueFromExcelValue(importColVal.dbVal, importColVal.getDbCol());
				
				//sql condition
				DODataDao.appendSqlConditionItem(outputSqlConditionOfPrimaryKeys, importColVal.getDbCol(), dbVal);
			}
		}
		
	}

	protected static int updateOneRow(
			Connection conn,
			DBTable dbTable,
			List<DODataImportColMetaInfo> colMataList, List<Object> excelRow,
			List<DataImportColValue> colValueAssignList
			) throws SQLException, ParseException {
		PreparedStatement pstmt = null;

		try {
			String tableName = dbTable.getTableName();
			
			int index;
			int i;
			StringBuilder sql = new StringBuilder();
			sql.append("update ").append(tableName).append(" set ");

			DODataImportColMetaInfo colMeta = null;
			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);
				
				if(colMeta.getDbCol() == null) {
					continue;
				}
				if(colMeta.getDbCol().isPrimaryKey()) {
					continue;
				}
				
				if(index > 0) {
					sql.append(",");
				}
				sql.append(DOSqlParamUtil.quoteSqlIdentifier(colMeta.getDbCol().getName())).append(" = ? ");
				
				index++;
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					if(importColVal.getDbCol().isPrimaryKey()) {
						continue;
					}
					
					if(index > 0) {
						sql.append(",");
					}
					sql.append(DOSqlParamUtil.quoteSqlIdentifier(importColVal.getDbCol().getName())).append(" = ? ");
					
					index++;
				}
			}
			
			sql.append(" where ");

			index = 0;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.getDbCol() == null) {
					continue;
				}
				if(!colMeta.getDbCol().isPrimaryKey()) {
					continue;
				}

				if(index > 0) {
					sql.append(" and ");
				}
				sql.append(DOSqlParamUtil.quoteSqlIdentifier(colMeta.getDbCol().getName())).append(" = ?");
				
				index++;
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);
					
					if(!importColVal.getDbCol().isPrimaryKey()) {
						continue;
					}
					
					if(index > 0) {
						sql.append(" and ");
					}
					sql.append(DOSqlParamUtil.quoteSqlIdentifier(importColVal.getDbCol().getName())).append(" = ? ");
					
					index++;
				}
			}

			logger.debug("updateOneRow():" + sql.toString());
			
			pstmt = conn.prepareStatement(sql.toString());

			index = 1;
			Object dbVal = null;
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.getDbCol() == null) {
					continue;
				}
				if(colMeta.getDbCol().isPrimaryKey()) {
					continue;
				}

				dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.getDbCol());
				pstmt.setObject(index++, dbVal);
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);

					if(importColVal.getDbCol().isPrimaryKey()) {
						continue;
					}
					
					dbVal = getDBValueFromExcelValue(importColVal.dbVal, importColVal.getDbCol());
					pstmt.setObject(index++, dbVal);
				}
			}
			
			for(i = 0; i < colMataList.size(); i++) {
				colMeta = colMataList.get(i);

				if(colMeta.getDbCol() == null) {
					continue;
				}
				if(!colMeta.getDbCol().isPrimaryKey()) {
					continue;
				}

				dbVal = getDBValueFromExcelValue(excelRow.get(i), colMeta.getDbCol());
				pstmt.setObject(index++, dbVal);
			}
			if(colValueAssignList != null) {
				DataImportColValue importColVal = null;
				for(i = 0; i < colValueAssignList.size(); i++) {
					importColVal = colValueAssignList.get(i);

					if(!importColVal.getDbCol().isPrimaryKey()) {
						continue;
					}
					
					dbVal = getDBValueFromExcelValue(importColVal.dbVal, importColVal.getDbCol());
					pstmt.setObject(index++, dbVal);
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
	
	protected static String formatDBValueToExcelValue(String dbVal, DBColumn dbCol, MetaDataField metaDataField) {
		String colType = dbCol.getColumnType().toLowerCase();
		
		if(dbVal == null) {
			return "";
		} else {
			if(colType.startsWith("bigint") 
					&& metaDataField.getFieldDispFormat() != null
					&& (metaDataField.getFieldDispFormat().startsWith(DODataDaoUtil.FORMAT_DATE_YMD_MINUS)
							|| metaDataField.getFieldDispFormat().startsWith(DODataDaoUtil.FORMAT_DATE_YMD_SLASH)
						) 
				) {
				return DODataDaoUtil.formatUTCToDate(metaDataField.getFieldDispFormat(), Long.parseLong(dbVal));
			} else {
				return dbVal;
			}
		}
	}
	
	protected static Object getDBValueFromExcelValue(Object excelCellVal, DBColumn dbCol) throws ParseException {
		String colType = dbCol.getColumnType().toLowerCase();
		
		if(excelCellVal == null) {
			return null;
		} else {
			if(excelCellVal.getClass() == String.class) {
				if(colType.startsWith("bigint")) {
					if(((String)excelCellVal).length() == 0) {
						return null;
					} else {
						return DODataDaoUtil.parseDateOrNumberToNumber((String)excelCellVal);
					}
				} else {
					return excelCellVal;
				}
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
				if(colType.startsWith("bigint")) {
					return Long.valueOf(((Date)excelCellVal).getTime());
				} else {
					return String.valueOf(excelCellVal); 
				}
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
	
	public static List<DODataImportColMetaInfo> findoutDataImportColMetaListOfExcelTitleRow(
			MetaDataImportSetting dataImportSetting,
			DBTable dbTable,
			List<Object> titleRow) {
		List<DODataImportColMetaInfo> colMetaList = new ArrayList<DODataImportColMetaInfo>();
		
		MetaDataField dataField = null;
		DBColumn dbCol = null;
		DODataImportColMetaInfo importColMeta = null;
		
		Object title = null;
		for(int j = 0; j < titleRow.size(); j++) {
			title = titleRow.get(j);
			
			importColMeta = new DODataImportColMetaInfo();
			
			dataField = findoutMetaDataFieldByExcelTitle(dataImportSetting, title);
			if(dataField != null) {
				importColMeta.setMetaDataField(dataField);

				dbCol = findoutDBColumnByColName(dbTable, dataField.getFieldName());
				if(dbCol != null) {
					importColMeta.setDbCol(dbCol);
				}
			}
			
			colMetaList.add(importColMeta);
		}
		
		return colMetaList;
	}
	
	public static MetaDataField findoutMetaDataFieldByExcelTitle(MetaDataImportSetting dataImportSetting, Object title) {
		if(title == null) {
			return null;
		}
		
		String titleStr = String.valueOf(title).toLowerCase();
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
	
	public static class DataImportColValue extends DODataImportColMetaInfo {
		private Object dbVal = null;

		public Object getDbVal() {
			return dbVal;
		}

		public void setDbVal(Object dbVal) {
			this.dbVal = dbVal;
		}
	}
	
}
 