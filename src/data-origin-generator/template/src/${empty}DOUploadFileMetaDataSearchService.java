package ${basePackage}.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hwpf.model.ListData;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.data.DOUploadFileMeta;
import com.beef.dataorigin.web.service.DODataSearchService;
import com.beef.dataorigin.web.upload.DOUploadFileService;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;

public class DOUploadFileMetaDataSearchService extends DODataSearchService {
	private final static Logger logger = Logger.getLogger(DOUploadFileMetaDataSearchService.class);

	public String searchDataCount(String searchConditionXml) {
		String tableName = "douploadfilemeta";
		return super.searchDataCount(tableName, searchConditionXml);
	}
	
	public String searchData(int beginIndex, int pageSize, 
			String searchConditionXml, String orderByFields) {
		String tableName = "douploadfilemeta";
		
		Connection conn = null;
		try {
			if(pageSize > MAX_PAGE_SIZE) {
				return DOServiceMsgUtil.getDefinedMsgXml(DOServiceMsgUtil.ErrorSearchPageSizeExceedMax);
			}
			
			DOSearchCondition searchCondition = null;
			if(searchConditionXml == null || searchConditionXml.length() == 0) {
				searchCondition = new DOSearchCondition(); 
			} else {
				searchCondition = (DOSearchCondition) XmlDeserializer.stringToObject(
						searchConditionXml, DOSearchCondition.class, DataOriginWebContext.getDataOriginContext());
			}
			
			String[] orderByFieldArray = DODataDaoUtil.splitByDelim(orderByFields, ",");
			
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			List listData = DODataDao.searchDataListBySearchCondition(
					conn, beginIndex, pageSize, tableName, searchCondition, orderByFieldArray);
			
			return XmlSerializer.objectToString(listData, ArrayList.class);
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
