package ${basePackage}.service;

import java.sql.Connection;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DODataModificationCommitTaskBundle;
import com.beef.dataorigin.web.datacommittask.dao.DODataModificationCommitTaskSchedulerDao.DataModificationCommitTaskModType;
import com.beef.dataorigin.web.service.DODataDetailService;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;

public class DODataModificationCommitTaskBundleDataDetailService extends DODataDetailService {
	private final static Logger logger = Logger.getLogger(DODataModificationCommitTaskBundleDataDetailService.class);

	public String findDataByPK(String dataXml) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.findDataByPK(tableName, dataXml);
	}
	
	public String modifyDataCommitTaskBundleScheduleTime(String table_name, long schedule_commit_time, long newSchedule_commit_time) {
		boolean isAllowModify = DataOriginWebContext.getDataCommitTaskScheduler().isAllowModifyDataCommitTaskBundleScheduleTime(
				table_name, schedule_commit_time);
		if(!isAllowModify) {
			return DOServiceMsgUtil.getDefinedMsgXml(
					DOServiceMsgUtil.ErrorDataModificationCommitTaskBundleNotAllowModifyExecuteSoon);
		}
		
		Connection conn = null;
		boolean autoCommit = false;
		try {
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);

			int updCnt = DataOriginWebContext.getDataCommitTaskScheduler().modifyDataCommitTaskBundleScheduleTime(
					conn, table_name, schedule_commit_time, newSchedule_commit_time);
			
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
	/*
	public String deleteDataByPK(String dataXml) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.deleteDataByPK(tableName, dataXml);
	}
	public String updateDataByPK(String dataXml) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.updateDataByPK(tableName, dataXml);
	}
	/*
	public String insertData(String dataXml) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.insertData(tableName, dataXml);
	}

	public String deleteDataByPKList(String dataListXml) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.deleteDataByPKList(tableName, dataListXml);
	}
	
	@Override
	protected String updateDataByPK(
			String tableName, String dataXml
			) {
		Connection conn = null;
		boolean autoCommit = false;
		try {
			conn = DOServiceUtil.getOnEditingDBConnection();
			
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);

			DODataModificationCommitTaskBundle data = (DODataModificationCommitTaskBundle) XmlDeserializer.stringToObject(
					dataXml, DODataModificationCommitTaskBundle.class, DataOriginWebContext.getDataOriginContext());
			
			int updCnt = DODataDao.updateDataByPK(conn, tableName, data);
			if(updCnt > 0) {
				createDataCommitTask(conn, tableName, data, DataModificationCommitTaskModType.ModTypeUpdate);
				
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
	*/
	
}
