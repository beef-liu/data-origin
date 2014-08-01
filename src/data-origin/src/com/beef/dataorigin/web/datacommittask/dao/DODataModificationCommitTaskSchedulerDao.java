package com.beef.dataorigin.web.datacommittask.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.beef.dataorigin.web.data.DODataModificationCommitTaskBundle;

public class DODataModificationCommitTaskSchedulerDao {
	private final static Logger logger = Logger.getLogger(DODataModificationCommitTaskSchedulerDao.class);
	
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

	
}
