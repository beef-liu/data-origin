package com.beef.dataorigin.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.beef.dataorigin.web.dao.DODataImportExportDao;
import com.beef.dataorigin.web.dao.DODataImportExportDao.DataImportColValue;
import com.beef.dataorigin.web.data.DOColValue;
import com.beef.dataorigin.web.data.DODataExportResult;
import com.beef.dataorigin.web.data.DODataImportCheckSheetResult;
import com.beef.dataorigin.web.data.DODataImportCheckTitleRowResult;
import com.beef.dataorigin.web.data.DODataImportColMetaInfo;
import com.beef.dataorigin.web.data.DODataImportResult;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;
import com.salama.service.core.net.http.ContentTypeHelper;
import com.salama.service.core.net.http.MultipartFile;
import com.salama.service.core.net.http.MultipartRequestWrapper;

public class DODataImportExportService {
	private final static Logger logger = Logger.getLogger(DODataImportExportService.class);
	private final static SimpleDateFormat DateFormatYmdHMS = new SimpleDateFormat("yyyyMMddHHmmss");
	
	protected String exportDataExcel(
			RequestWrapper request, ResponseWrapper response, 
			String tableName,
			String searchConditionXml) {
		String outputTempFileName = DOServiceUtil.newDataId() + ".xlsx";
		File outputTempFile = getTempExcelFile(request, outputTempFileName);

		DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
		MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
		
		Connection conn = null;
		OutputStream outputExcel = null;
		try {
			DOSearchCondition searchCondition = (DOSearchCondition) XmlDeserializer.stringToObject(
					searchConditionXml, DOSearchCondition.class, DataOriginWebContext.getDataOriginContext());			
			
			boolean isXLSX = true;
			File templateFile = new File(
					DataOriginWebContext.getDataOriginContext().getDataOriginDirManager().getTemplateXlsDir(),
					tableName.toLowerCase() + "_data_list.xlsx"
					);
			
			Workbook workbook = ExcelUtil.createWorkbook(templateFile, isXLSX);
			outputExcel = new FileOutputStream(outputTempFile);

			conn = DOServiceUtil.getOnEditingDBConnection();
			DODataExportResult exportResult = DODataImportExportDao.exportDataExcel(
					conn, workbook, outputExcel, dataImportSetting, dbTable, 
					searchCondition);

			exportResult.setExportResultFile(outputTempFileName);
			
			return XmlSerializer.objectToString(exportResult, DODataExportResult.class);
		} catch(Throwable e) {
			logger.error(null, e);
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
			try {
				outputExcel.close();
			} catch(Throwable e) {
			}
		}
	}
	
	protected void downloadTempExcel(
			RequestWrapper request, ResponseWrapper response,
			String tableName,
			String fileName) {
		try {
			File downloadTargetFile = getTempExcelFile(request, fileName);
			
			response.setContentLength((int)downloadTargetFile.length());
			response.setContentType(ContentTypeHelper.ApplicationMsExcel);
			String saveAsFileName = tableName + DateFormatYmdHMS.format(new Date()) + ".xlsx";
			response.setDownloadFileName(request, response, saveAsFileName);
			
			response.writeFile(downloadTargetFile);
		} catch(Throwable e) {
			logger.error(null, e);
		}
	}
	
	/**
	 * Upload file to check sheet count
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
		OutputStream outputExcel = null;
		String saveToFileName = newTempExcelFileName();
		File saveToFile = getTempExcelFile(request, saveToFileName);
		try {
			inputExcel = excelMultiFile.multiFile.getInputStream();
			outputExcel = new FileOutputStream(saveToFile);

			//copy file
			DODataDaoUtil.copy(inputExcel, outputExcel);
		} catch(Throwable e) {
			logger.error(null, e);
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				inputExcel.close();
			} catch(Throwable e) {
			}
			try {
				outputExcel.close();
			} catch(Throwable e) {
			}
		}
		
		try {
			inputExcel = new FileInputStream(saveToFile);
			
			Workbook workBook = ExcelUtil.createWorkbook(inputExcel, excelMultiFile.isXLSX);
			
			int sheetCount = workBook.getNumberOfSheets();

			DODataImportCheckSheetResult checkSheetResult = new DODataImportCheckSheetResult();
			checkSheetResult.setSheetCount(sheetCount);
			checkSheetResult.setImportFile(saveToFileName);
			
			checkSheetResult.setSheetNameList(new ArrayList<String>());
			for(int i = 0; i < sheetCount; i++) {
				checkSheetResult.getSheetNameList().add(workBook.getSheetName(i));
			}
			
			return XmlSerializer.objectToString(checkSheetResult, DODataImportCheckSheetResult.class) ;
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
			String tableName, String fileName,
			int sheetIndex) {
		/*
		//get file and check file name
		ExcelMultiFile excelMultiFile = new ExcelMultiFile((MultipartRequestWrapper)request);
		if(excelMultiFile.multiFile == null) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel) ;
		}
		if(!excelMultiFile.isXLSorXLSX) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel);
		}

		//String originFileName =  excelMultiFile.multiFile.getOriginalFilename();
		*/


		File excelImportFile = getTempExcelFile(request, fileName);
		boolean isXLSX = fileName.endsWith(".xlsx");
		
		//check columns --------------------------------------
		try {
			Workbook workBook = ExcelUtil.createWorkbook(excelImportFile, isXLSX);
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
			DODataImportCheckTitleRowResult checkResult = new DODataImportCheckTitleRowResult();
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
			
			return XmlSerializer.objectToString(checkResult, DODataImportCheckTitleRowResult.class);
		} catch(Throwable e) {
			logger.error(null, e);
			return DOServiceMsgUtil.makeMsgXml(e);
		}
	}
	
	protected String importDataExcel(
			RequestWrapper request, ResponseWrapper response, 
			String tableName, String fileName,
			int sheetIndex, 
			String colValueListXml) {
		/*
		//get file and check file name
		ExcelMultiFile excelMultiFile = new ExcelMultiFile((MultipartRequestWrapper)request);
		if(excelMultiFile.multiFile == null) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel) ;
		}
		if(!excelMultiFile.isXLSorXLSX) {
			return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataImportMustExcel);
		}
		//String originFileName =  excelMultiFile.multiFile.getOriginalFilename();
		*/

		File excelImportFile = getTempExcelFile(request, fileName);
		boolean isXLSX = fileName.endsWith(".xlsx");
		
		//assigned col values
		Connection conn = null;
		boolean isAutoCommit = false;
		try {
			
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
					if(colVal.getColValue() == null || colVal.getColValue().length() == 0) {
						continue;
					}
					
					impColVal = new DataImportColValue();
					impColVal.setDbCol(mDbTable.getColumnMap().get(colVal.getColName()));
					impColVal.setMetaDataField(mDataImportSetting.getFieldMap().get(colVal.getColName()));
					impColVal.setDbVal(colVal.getColValue());
					
					importColValueList.add(impColVal);
				}
			}
	
			//update DB
			Workbook workBook = ExcelUtil.createWorkbook(excelImportFile, isXLSX);
			Sheet sheet = workBook.getSheetAt(sheetIndex);
			
			conn = DOServiceUtil.getOnEditingDBConnection();
			isAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);
			DODataImportResult dataImportResult = DODataImportExportDao.importDataExcel(
					conn, sheet, 
					//originFileName, 
					dataImportSetting, 
					dbTable, importColValueList);
			
			//save file which error indicated
			String resultFileName = getResultExcelFileName(fileName); 
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
				conn.setAutoCommit(isAutoCommit);
			} catch(Throwable e) {
			} 
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}

	protected String newTempExcelFileName() {
		return DOServiceUtil.newDataId() + ".xlsx";
	}
	
	protected String getResultExcelFileName(String importFileName) {
		int index = importFileName.lastIndexOf('.');
		
		if(index > 0) {
			return importFileName.substring(0, index) + "_result" + importFileName.substring(index);
		} else {
			return importFileName + "_result";
		}
	}
	
	protected InputStream getErrorResultExcelInputStream(RequestWrapper request, String fileName) throws FileNotFoundException {
		return new FileInputStream(getTempExcelFile(request, fileName));
	}
	
	protected OutputStream getErrorResultExcelOutputStream(RequestWrapper request, String fileName) throws FileNotFoundException {
		return new FileOutputStream(getTempExcelFile(request, fileName));
	}
	
	protected File getTempExcelFile(RequestWrapper request, String fileName) {
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
