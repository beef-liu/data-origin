package ${basePackage}.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;

import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.test.ws.service.DOUploadFileMetaDataDetailService;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DOUploadFileMeta;
import com.beef.dataorigin.web.service.DODataDetailService;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;

public class DOUploadFileMetaDataDetailService extends DODataDetailService {
	private final static Logger logger = Logger.getLogger(DOUploadFileMetaDataDetailService.class);

	public String findDataByPK(String dataXml) {
		String tableName = "douploadfilemeta";
		return super.findDataByPK(tableName, dataXml);
	}
	
	public String deleteDataByPK(String dataXml) {
		String tableName = "douploadfilemeta";
		return super.deleteDataByPK(tableName, dataXml);
	}

	public String updateDataByPK(String dataXml) {
		String tableName = "douploadfilemeta";
		return updateDataByPK(tableName, dataXml);
	}
	
	public String insertData(String dataXml) {
		String tableName = "douploadfilemeta";
		return super.insertData(tableName, dataXml);
	}
	
	public String deleteDataByPKList(String dataListXml) {
		String tableName = "douploadfilemeta";
		return super.deleteDataByPKList(tableName, dataListXml);
	}

	@Override
	protected String updateDataByPK(String tableName, String dataXml) {
		Connection conn = null;
		boolean autoCommit = false;
		try {
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);

			DOUploadFileMeta data = (DOUploadFileMeta) XmlDeserializer.stringToObject(
					dataXml, DOUploadFileMeta.class, DataOriginWebContext.getDataOriginContext());
			
			int updCnt = updateDataByPK(conn, data);
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
	
	private final static String SQL_UPDATE_BY_PK = "update DOUploadFileMeta set content_type = ?, file_tag = ?, update_time = ? where file_id = ?";
	protected static int updateDataByPK(Connection conn, DOUploadFileMeta data) throws SQLException {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(SQL_UPDATE_BY_PK);
			
			int index = 1;
			stmt.setString(index++, data.getContent_type());
			stmt.setString(index++, data.getFile_tag());
			stmt.setLong(index++, data.getUpdate_time());
			stmt.setString(index++, data.getFile_id());
			
			return stmt.executeUpdate();
		} finally {
			try {
				stmt.close();
			} catch(Throwable e) {
			}
		}
	}
	
}
