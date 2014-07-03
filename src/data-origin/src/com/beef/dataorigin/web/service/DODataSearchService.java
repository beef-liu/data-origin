package com.beef.dataorigin.web.service;

import java.sql.Connection;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DODataServiceError;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.service.clouddata.core.AppContext;
import com.salama.service.clouddata.core.AppServiceContext;

public class DODataSearchService <DataType> {
	private final static Logger logger = Logger.getLogger(DODataSearchService.class);
	
	public final static int MAX_PAGE_SIZE = 500;
	
	public String searchData(
			int beginIndex, int pageSize,
			String tableName,
			String searchConditionXml,
			String orderByFields) {
		AppContext appContext = AppServiceContext.getAppContext();
		
		Connection conn = null;
		try {
			
			DOSearchCondition searchCondition = (DOSearchCondition) XmlDeserializer.stringToObject(
					searchConditionXml, DOSearchCondition.class, DataOriginWebContext.getDataOriginContext());
			
			String[] orderByFieldArray = DODataDaoUtil.splitByDelim(orderByFields, ",");
			
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			return DODataDao.searchDataXmlBySearchCondition(conn, beginIndex, pageSize, tableName, searchCondition, orderByFieldArray);
		} catch(Throwable e) {
			logger.error(null, e);
			
			try {
				DODataServiceError error = new DODataServiceError(e);
				return appContext.objectToXml(error, DODataServiceError.class);
			} catch (Throwable e1) {
				throw new RuntimeException(e1);
			}
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	} 
}
