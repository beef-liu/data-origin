package com.beef.dataorigin.web.datacommittask.dao;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DODataModificationCommitTask;
import com.beef.dataorigin.web.data.DODataModificationCommitTaskBundle;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.service.clouddata.util.dao.QueryDataDao;
import com.salama.service.clouddata.util.dao.UpdateDataDao;

public class DODataModificationCommitTaskSchedulerDao {
	private final static Logger logger = Logger.getLogger(DODataModificationCommitTaskSchedulerDao.class);

	public static enum DataModificationCommitTaskModType {ModTypeInsert, ModTypeUpdate, ModTypeDelete};
	
	public final static int TASK_MOD_TYPE_UPDATE = 0;
	public final static int TASK_MOD_TYPE_INSERT = 1;
	public final static int TASK_MOD_TYPE_DELETE = -1;

	public final static int TASK_COMMIT_STATUS_WAIT_TO_COMMIT = 0;
	public final static int TASK_COMMIT_STATUS_SUCCESS = 1;
	public final static int TASK_COMMIT_STATUS_FAIL = -1;
	
	private static final String SQL_FIND_TASK_BUNDLE_WAIT_TO_EXECUTE = "select "
			+ "   `table_name`, schedule_commit_time, count(1) as cnt"
			+ " from DODataModificationCommitTask"
			+ " where commit_status = 0"
			+ " group by `table_name`, schedule_commit_time"
			+ " order by schedule_commit_time limit ?, ?"
			;
	public static List<DODataModificationCommitTaskBundle> findTaskBundleWaitToExecute(Connection conn, int maxCount) throws SQLException {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(SQL_FIND_TASK_BUNDLE_WAIT_TO_EXECUTE);
			
			int index = 1;
			stmt.setInt(index++, 0);
			stmt.setInt(index++, maxCount);
			
			ResultSet rs = stmt.executeQuery();
			
			List<DODataModificationCommitTaskBundle> dataList = new ArrayList<DODataModificationCommitTaskBundle>();
			DODataModificationCommitTaskBundle data;
			while(rs.next()) {
				data = new DODataModificationCommitTaskBundle();
				
				data.setTable_name(rs.getString("table_name"));
				data.setData_row_count_of_total(rs.getInt("cnt"));
				data.setSchedule_commit_time(rs.getLong("schedule_commit_time"));
				data.setData_row_count_of_did_commit(0);
				data.setTask_bundle_status(DODataModificationCommitTaskBundle.SCHEDULE_TASK_STATUS_WAIT_TO_START);
				
				dataList.add(data);
			}
			
			return dataList;
		} finally {
			try {
				stmt.close();
			} catch(Throwable e) {
				logger.error(null, e);
			}
		}
	}


	private static final String SQL_FIND_TASK_TO_EXECUTE = "select * "
			+ " from DODataModificationCommitTask"
			+ " where `table_name` = ? and schedule_commit_time = ? and commit_status = 0 "
			+ " limit ?, ?"
			;
	public static List<DODataModificationCommitTask> findTaskToExecute(
			Connection conn, int maxCount, 
			String table_name, long schedule_commit_time
			) throws SQLException, InstantiationException, InvocationTargetException, IllegalAccessException {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(SQL_FIND_TASK_TO_EXECUTE);
			
			int index = 1;
			stmt.setString(index++, table_name);
			stmt.setLong(index++, schedule_commit_time);
			stmt.setInt(index++, 0);
			stmt.setInt(index++, maxCount);
			
			List<DODataModificationCommitTask> dataList = 
					QueryDataDao.findData(stmt, DODataModificationCommitTask.class);
			
			return dataList;
		} finally {
			try {
				stmt.close();
			} catch(Throwable e) {
				logger.error(null, e);
			}
		}
	}
	
	private static final String SQL_UPDATE_COMMIT_TASK_STATUS = "update `DODataModificationCommitTask` set"
			+ " commit_time = ?, retried_count = retried_count + 1, "
			+ " commit_status = ?, error_msg = ? "
			+ " where `table_name` = ? and schedule_commit_time = ? and sql_primary_key = ?"
			;
	public static int updateDataCommitTaskStatus(
			Connection conn, DODataModificationCommitTask dataCommitTask
			) throws SQLException, InstantiationException, InvocationTargetException, IllegalAccessException {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(SQL_UPDATE_COMMIT_TASK_STATUS);
			
			int index = 1;
			stmt.setLong(index++, dataCommitTask.getCommit_time());
			stmt.setInt(index++, dataCommitTask.getCommit_status());
			stmt.setString(index++, dataCommitTask.getError_msg());
			
			stmt.setString(index++, dataCommitTask.getTable_name());
			stmt.setLong(index++, dataCommitTask.getSchedule_commit_time());
			stmt.setString(index++, dataCommitTask.getSql_primary_key());

			return stmt.executeUpdate();
		} finally {
			try {
				stmt.close();
			} catch(Throwable e) {
				logger.error(null, e);
			}
		}
	}

	public static int createDataCommitTask(
			Connection conn,
			MDBTable mDBTable,
			Object data, 
			DataModificationCommitTaskModType modType,
			long schedule_commit_time,
			String adminId
			) throws SQLException, InstantiationException, InvocationTargetException, IllegalAccessException, IllegalArgumentException, IntrospectionException {
		
		DODataModificationCommitTask dataCommitTask = new DODataModificationCommitTask();
		
		if(modType == DataModificationCommitTaskModType.ModTypeInsert) {
			dataCommitTask.setMod_type(TASK_MOD_TYPE_INSERT);

		} else if(modType == DataModificationCommitTaskModType.ModTypeUpdate) {
			dataCommitTask.setMod_type(TASK_MOD_TYPE_UPDATE);
			
		} else {
			dataCommitTask.setMod_type(TASK_MOD_TYPE_DELETE);
		}

		dataCommitTask.setCommit_status(TASK_COMMIT_STATUS_WAIT_TO_COMMIT);
		dataCommitTask.setSchedule_commit_time(schedule_commit_time);
		
		String sqlPrimaryKey = DODataDao.makeSqlConditionOfPrimaryKey(conn, mDBTable.getTableName(), data);
		dataCommitTask.setSql_primary_key(sqlPrimaryKey);

		dataCommitTask.setTable_name(mDBTable.getTableName());
		dataCommitTask.setUpdate_admin(adminId);
		dataCommitTask.setUpdate_time(System.currentTimeMillis());
		
		//insert or update to DB
		int updCnt = 0;
		try {
			updCnt = UpdateDataDao.insertData(conn, dataCommitTask);
		} catch(SQLException sqle) {
			if(sqle.getClass().getSimpleName().equalsIgnoreCase("MySQLIntegrityConstraintViolationException")) {
				//duplicated key, then update
				updCnt = UpdateDataDao.updateData(conn, 
						mDBTable.getTableName(), dataCommitTask, mDBTable.getPrimaryKeys());
			} else {
				throw sqle;
			}
		}
		
		return updCnt;
	}
	
}
