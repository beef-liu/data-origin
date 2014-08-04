package com.beef.dataorigin.web.datacommittask.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.beef.dataorigin.web.data.DODataModificationCommitTask;
import com.beef.dataorigin.web.data.DODataModificationCommitTaskBundle;
import com.salama.service.clouddata.util.dao.QueryDataDao;

public class DODataModificationCommitTaskSchedulerDao {
	private final static Logger logger = Logger.getLogger(DODataModificationCommitTaskSchedulerDao.class);
	
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
			+ " where task_id = ?"
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
			stmt.setString(index++, dataCommitTask.getTask_id());

			return stmt.executeUpdate();
		} finally {
			try {
				stmt.close();
			} catch(Throwable e) {
				logger.error(null, e);
			}
		}
	}
	
}
