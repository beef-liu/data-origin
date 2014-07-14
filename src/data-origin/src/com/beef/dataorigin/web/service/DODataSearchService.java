package com.beef.dataorigin.web.service;

import java.sql.Connection;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;

public class DODataSearchService {
	private final static Logger logger = Logger.getLogger(DODataSearchService.class);

	public final static int MAX_PAGE_SIZE = 500;
	
	protected String searchDataCount(
		String tableName,
		String searchConditionXml
		) {
		Connection conn = null;
		try {
			
			DOSearchCondition searchCondition = (DOSearchCondition) XmlDeserializer.stringToObject(
					searchConditionXml, DOSearchCondition.class, DataOriginWebContext.getDataOriginContext());
			
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			return Integer.toString(DODataDao.searchDataCountBySearchCondition(conn, tableName, searchCondition));
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
	
	protected String searchData(
			int beginIndex, int pageSize,
			String tableName,
			String searchConditionXml,
			String orderByFields) {
		Connection conn = null;
		try {
			if(pageSize > MAX_PAGE_SIZE) {
				return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorSearchPageSizeExceedMax);
			}
			
			DOSearchCondition searchCondition = (DOSearchCondition) XmlDeserializer.stringToObject(
					searchConditionXml, DOSearchCondition.class, DataOriginWebContext.getDataOriginContext());
			
			String[] orderByFieldArray = DODataDaoUtil.splitByDelim(orderByFields, ",");
			
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			return DODataDao.searchDataXmlBySearchCondition(conn, beginIndex, pageSize, tableName, searchCondition, orderByFieldArray);
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
}
