package com.beef.dataorigin.web.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;

public class DODataDetailService {
	private final static Logger logger = Logger.getLogger(DODataDetailService.class);

	/**
	 * 
	 * @param tableName
	 * @param dataXml Only need primary keys
	 * @return xml of data,  empty if data is not found
	 */
	protected String findDataByPK(
			String tableName, String dataXml
			) {
		Connection conn = null;
		try {
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataImportSetting.getDataClassName());
			
			Object dataPK = XmlDeserializer.stringToObject(
					dataXml, dataClass, DataOriginWebContext.getDataOriginContext());
			
			//query DB
			conn = DOServiceUtil.getOnEditingDBConnection();
			Object data = DODataDao.searchDataByPK(conn, tableName, dataPK);
			
			//convert to xml
			if(data != null) {
				return XmlSerializer.objectToString(data, dataClass);
			} else {
				return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorDataDetailDataNotExist);
			}
		} catch(Throwable e) {
			logger.error(null, e);
			
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
		
	} 

	
	/**
	 * 
	 * @param tableName
	 * @param dataXml Only need primary keys
	 * @return
	 */
	protected String deleteDataByPK(String tableName, String dataXml) {
		Connection conn = null;
		boolean autoCommit = false;
		try {
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataImportSetting.getDataClassName());
			
			Object dataPK = XmlDeserializer.stringToObject(
					dataXml, dataClass, DataOriginWebContext.getDataOriginContext());
			
			//update DB
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);
			
			int updCnt = DODataDao.deleteDataByPK(conn, tableName, dataPK);
			
			if(updCnt > 0) {
				return "success";
			} else {
				return "fail";
			}
		} catch(Throwable e) {
			logger.error(null, e);
			
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.setAutoCommit(autoCommit);;
			} catch(Throwable e) {
			}
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	/**
	 * 
	 * @param tableName
	 * @param dataListXml
	 * @return count of deleted data
	 */
	protected String deleteDataByPKList(String tableName, String dataListXml) {
		Connection conn = null;
		boolean autoCommit = false;
		try {
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataImportSetting.getDataClassName());
			
			List dataList = (List) XmlDeserializer.stringToObject(
					dataListXml, ArrayList.class, DataOriginWebContext.getDataOriginContext());
			
			//update DB
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);
			
			int updCnt = DODataDao.deleteDataByPKList(conn, tableName, dataList);
			
			return String.valueOf(updCnt);
		} catch(Throwable e) {
			logger.error(null, e);
			
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.setAutoCommit(autoCommit);;
			} catch(Throwable e) {
			}
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	/**
	 * 
	 * @param tableName
	 * @param dataXml
	 * @return "success" or "fail"
	 */
	protected String updateDataByPK(
			String tableName, String dataXml
			) {
		Connection conn = null;
		boolean autoCommit = false;
		try {
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataImportSetting.getDataClassName());
			
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);

			Object data = XmlDeserializer.stringToObject(
					dataXml, dataClass, DataOriginWebContext.getDataOriginContext());
			
			int updCnt = DODataDao.updateDataByPK(conn, tableName, data);
			if(updCnt > 0) {
				return "success";
			} else {
				return "fail";
			}
		} catch(Throwable e) {
			logger.error(null, e);
			
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.setAutoCommit(autoCommit);;
			} catch(Throwable e) {
			}
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	/**
	 * 
	 * @param tableName
	 * @param dataXml
	 * @return "success" or "fail"
	 */
	protected String insertData(
			String tableName, String dataXml
			) {
		Connection conn = null;
		boolean autoCommit = false;
		try {
			MetaDataImportSetting dataImportSetting = DataOriginWebContext.getDataOriginContext().getMetaDataImportSetting(tableName);
			
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataImportSetting.getDataClassName());
			
			conn = DOServiceUtil.getOnEditingDBConnection();

			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);

			Object data = XmlDeserializer.stringToObject(
					dataXml, dataClass, DataOriginWebContext.getDataOriginContext());
			
			int updCnt = DODataDao.insertData(conn, tableName, data);
			if(updCnt > 0) {
				return "success";
			} else {
				return "fail";
			}
		} catch(Throwable e) {
			logger.error(null, e);
			
			return DOServiceMsgUtil.makeMsgXml(e);
		} finally {
			try {
				conn.setAutoCommit(autoCommit);;
			} catch(Throwable e) {
			}
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
}
