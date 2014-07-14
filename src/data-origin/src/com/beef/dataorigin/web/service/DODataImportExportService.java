package com.beef.dataorigin.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataImportSetting;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.util.ExcelUtil;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.dao.DODataImportExportDao;
import com.beef.dataorigin.web.dao.DODataImportExportDao.DataImportColValue;
import com.beef.dataorigin.web.data.DOColValue;
import com.beef.dataorigin.web.data.DODataImportCheckFileResult;
import com.beef.dataorigin.web.data.DODataImportColMetaInfo;
import com.beef.dataorigin.web.data.DODataImportResult;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;
import com.salama.service.core.net.http.MultipartFile;
import com.salama.service.core.net.http.MultipartRequestWrapper;

public class DODataImportExportService {
	private final static Logger logger = Logger.getLogger(DODataImportExportService.class);
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param tableName
	 * @param sheetIndex
	 * @return "ok" if there is only 1 sheet.
	 */
	protected String checkDataExcelSheetCount(
			RequestWrapper request, ResponseWrapper response) {
		//get file and check file name
		ExcelMultiFile excelMultiFile = new ExcelMultiFile((MultipartRequestWrapper)request);
		if(excelMultiFile.multiFile == null) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel) ;
		}
		if(!excelMultiFile.isXLSorXLSX) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel);
		}

		//check columns --------------------------------------
		InputStream inputExcel = null;
		try {
			inputExcel = excelMultiFile.multiFile.getInputStream();

			Workbook workBook = ExcelUtil.createWorkbook(inputExcel, excelMultiFile.isXLSX);
			
			int sheetCount = workBook.getNumberOfSheets();
			if(sheetCount == 1) {
				return "ok";
			} else {
				return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportSheetMoreThanOne);
			}
		} catch(Throwable e) {
			logger.error(null, e);
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				inputExcel.close();
			} catch(Throwable e) {
			}
		}
		
	}
	
	protected String checkDataExcelTitleRow(
			RequestWrapper request, ResponseWrapper response, 
			String tableName,
			int sheetIndex) {
		//get file and check file name
		ExcelMultiFile excelMultiFile = new ExcelMultiFile((MultipartRequestWrapper)request);
		if(excelMultiFile.multiFile == null) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel) ;
		}
		if(!excelMultiFile.isXLSorXLSX) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel);
		}

		String originFileName =  excelMultiFile.multiFile.getOriginalFilename();

		//check columns --------------------------------------
		InputStream inputExcel = null;
		try {
			inputExcel = excelMultiFile.multiFile.getInputStream();

			Workbook workBook = ExcelUtil.createWorkbook(inputExcel, excelMultiFile.isXLSX);
			Sheet sheet = workBook.getSheetAt(sheetIndex);
			
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			MMetaDataImportSetting mDataImportSetting = DataOriginWebContext.getDataOriginContext().getMMetaDataImportSetting(tableName);
			DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
			MDBTable mDbTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
			DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(tableName);
			
			List<Object> titleList = ExcelUtil.readRowAutoDetectEndCol(sheet, 0, DODataImportExportDao.DEFAULT_MAX_COL, 0);
			List<DODataImportColMetaInfo> colMetaList = DODataImportExportDao.findoutDataImportColMetaListOfExcelTitleRow(
					dataImportSetting, dbTable, titleList);
			
			//make check result
			DODataImportCheckFileResult checkResult = new DODataImportCheckFileResult();
			checkResult.setColMetaList(colMetaList);
			
			List<String> titleStrList = new ArrayList<String>();
			for(int i = 0; i < titleList.size(); i++) {
				titleStrList.add(String.valueOf(titleList.get(i)));
			}
			checkResult.setColTitleList(titleStrList);
			
			//column which need input 
			List<DODataImportColMetaInfo> lackingColMetaList = new ArrayList<DODataImportColMetaInfo>();
			boolean isExists = false;
			DBColumn dbCol;
			DODataImportColMetaInfo colMetaInfo;
			int i, k;
			for(i = 0; i < dbTable.getColumns().size(); i++) {
				dbCol = dbTable.getColumns().get(i);
				
				isExists = false;
				for(k = 0; k < colMetaList.size(); k++) {
					colMetaInfo = colMetaList.get(k);
					
					if(colMetaInfo.getDbCol() != null && colMetaInfo.getDbCol().getName().equals(dbCol.getName())) {
						isExists = true;
						break;
					}
				}
				
				if(!isExists) {
					colMetaInfo = new DODataImportColMetaInfo();
					colMetaInfo.setDbCol(dbCol);
					colMetaInfo.setMetaDataField(mDataImportSetting.getFieldMap().get(dbCol.getName()));
					
					lackingColMetaList.add(colMetaInfo);
				}
			}
			checkResult.setLackingColMetaList(lackingColMetaList);
			
			return XmlSerializer.objectToString(checkResult, DODataImportCheckFileResult.class);
		} catch(Throwable e) {
			logger.error(null, e);
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				inputExcel.close();
			} catch(Throwable e) {
			}
		}
	}
	
	protected String importDataExcel(
			RequestWrapper request, ResponseWrapper response, 
			String tableName,
			int sheetIndex, 
			String colValueListXml) {
		//get file and check file name
		ExcelMultiFile excelMultiFile = new ExcelMultiFile((MultipartRequestWrapper)request);
		if(excelMultiFile.multiFile == null) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel) ;
		}
		if(!excelMultiFile.isXLSorXLSX) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel);
		}
		
		String originFileName =  excelMultiFile.multiFile.getOriginalFilename();

		
		//assigned col values
		Connection conn = null;
		InputStream inputExcel = null;
		try {
			inputExcel = excelMultiFile.multiFile.getInputStream();
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			MMetaDataImportSetting mDataImportSetting = DataOriginWebContext.getDataOriginContext().getMMetaDataImportSetting(tableName);
			DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
			MDBTable mDbTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
			DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(tableName);

			List<DataImportColValue> importColValueList = null;
			if(colValueListXml != null && colValueListXml.length() > 0) {
				List<DOColValue> colValList = (List) XmlDeserializer.stringToObject(
						colValueListXml, ArrayList.class, DataOriginWebContext.getDataOriginContext());
				importColValueList = new ArrayList<DODataImportExportDao.DataImportColValue>();
				DOColValue colVal;
				DataImportColValue impColVal;
				for(int i = 0; i < colValList.size(); i++) {
					colVal = colValList.get(i);
					
					impColVal = new DataImportColValue();
					impColVal.setDbCol(mDbTable.getColumnMap().get(colVal.getColName()));
					impColVal.setMetaDataField(mDataImportSetting.getFieldMap().get(colVal.getColName()));
					impColVal.setDbVal(colVal.getColValue());
					
					importColValueList.add(impColVal);
				}
			}
	
			//update DB
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			Workbook workBook = ExcelUtil.createWorkbook(inputExcel, excelMultiFile.isXLSX);
			Sheet sheet = workBook.getSheetAt(sheetIndex);
			
			DODataImportResult dataImportResult = DODataImportExportDao.importDataExcel(
					conn, sheet, 
					originFileName, dataImportSetting, 
					dbTable, importColValueList);
			
			//save file which error indicated
			String resultFileName = newErrorResultExcelFileName();
			OutputStream output = null;
			
			try {
				output = getErrorResultExcelOutputStream(request, resultFileName);
				workBook.write(output);
				
				dataImportResult.setImportResultFile(resultFileName);
			} catch(Throwable e) {
				logger.error(null, e);
			} finally {
				output.close();
			}
			
			
			return XmlSerializer.objectToString(dataImportResult, DODataImportResult.class);
		} catch(Throwable e) {
			logger.error(null, e);
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
			try {
				inputExcel.close();
			} catch(Throwable e) {
			}
		}
	}

	protected String newErrorResultExcelFileName() {
		return DOServiceUtil.newDataId() + ".xlsx";
	}
	
	protected InputStream getErrorResultExcelInputStream(RequestWrapper request, String fileName) throws FileNotFoundException {
		return new FileInputStream(getErrorResultExcelFile(request, fileName));
	}
	
	protected OutputStream getErrorResultExcelOutputStream(RequestWrapper request, String fileName) throws FileNotFoundException {
		return new FileOutputStream(getErrorResultExcelFile(request, fileName));
	}
	
	protected File getErrorResultExcelFile(RequestWrapper request, String fileName) {
		String dirPath = request.getServletContext().getRealPath("/WEB-INF/tempxls");
		File dir = new File(dirPath);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		File file = new File(dir, fileName);
		
		return file;
	} 

	protected static class ExcelMultiFile {
		public MultipartFile multiFile = null;
		public boolean isXLSorXLSX = false;
		public boolean isXLSX = false;
		
		public ExcelMultiFile(MultipartRequestWrapper multiRequest) {
			Enumeration<String> enumFileNames = multiRequest.getFileNames();
			String fileName = null;
			if(enumFileNames.hasMoreElements()) {
				fileName = enumFileNames.nextElement();
			} else {
				return;
			}
			
			isXLSX = false;
			multiFile = multiRequest.getFile(fileName);
			String originFileNameLower = multiFile.getOriginalFilename().toLowerCase();
			if(originFileNameLower.endsWith(".xls")) {
				isXLSX = false;
				isXLSorXLSX = true;
			} else if(originFileNameLower.endsWith(".xlsx")) {
				isXLSX = true;
				isXLSorXLSX = true;
			} else {
			}
		}
	}
	
	
}
