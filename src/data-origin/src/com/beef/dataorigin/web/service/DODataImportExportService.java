package com.beef.dataorigin.web.service;

import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataImportSetting;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.dao.DODataImportExportDao;
import com.beef.dataorigin.web.dao.DODataImportExportDao.DataImportColValue;
import com.beef.dataorigin.web.data.DOColValue;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;
import com.salama.service.core.net.http.MultipartFile;
import com.salama.service.core.net.http.MultipartRequestWrapper;

public class DODataImportExportService {
	private final static Logger logger = Logger.getLogger(DODataImportExportService.class);

	public static String importDataExcel(
			RequestWrapper request, ResponseWrapper response, 
			String tableName,
			int sheetIndex, 
			String colValueListXml) {
		MultipartRequestWrapper multiRequest = (MultipartRequestWrapper)request;
		Enumeration<String> enumFileNames = multiRequest.getFileNames();
		String fileName = null;
		if(enumFileNames.hasMoreElements()) {
			fileName = enumFileNames.nextElement();
		} else {
			return DOServiceMsgUtil.getDefinedMsg(DOServiceMsgUtil.ErrorDataImportMustExcel) ;
		}
		
		boolean isXLSX;
		
		MultipartFile multiFile = multiRequest.getFile(fileName);
		String originFileName =  multiFile.getOriginalFilename();
		String originFileNameLower = originFileName.toLowerCase();
		if(originFileNameLower.endsWith(".xls")) {
			isXLSX = false;
		} else if(originFileNameLower.endsWith(".xlsx")) {
			isXLSX = true;
		} else {
			return DOServiceMsgUtil.getDefinedMsg(DOServiceMsgUtil.ErrorDataImportMustExcel);
		}
		
		//import to DB
		Connection conn = null;
		InputStream inputExcel = null;
		try {
			inputExcel = multiFile.getInputStream();
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
					impColVal.dbCol = mDbTable.getColumnMap().get(colVal.getColName());
					impColVal.metaDataField = mDataImportSetting.getFieldMap().get(colVal.getColName());
					impColVal.dbVal = colVal.getColValue();
					
					importColValueList.add(impColVal);
				}
			}
	
			//update DB
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			DODataImportExportDao.importDataExcel(
					conn, inputExcel, isXLSX, sheetIndex, 
					dataImportSetting, dbTable, importColValueList);
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
	
}
