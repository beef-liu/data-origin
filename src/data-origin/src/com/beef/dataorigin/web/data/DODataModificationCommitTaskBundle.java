package com.beef.dataorigin.web.data;

public class DODataModificationCommitTaskBundle {
	public final static int SCHEDULE_TASK_STATUS_WAIT_TO_START = 0;
	public final static int SCHEDULE_TASK_STATUS_DID_START = 1;
	public final static int SCHEDULE_TASK_STATUS_DID_END = 2;
	
	private String _table_name;
	private long _schedule_commit_time = 0;
	private int _task_bundle_status = 0;
	
	private int _data_row_count_of_total = 0;
	private int _data_row_count_of_did_commit = 0;
	public String getTable_name() {
		return _table_name;
	}
	public void setTable_name(String table_name) {
		_table_name = table_name;
	}
	public long getSchedule_commit_time() {
		return _schedule_commit_time;
	}
	public void setSchedule_commit_time(long schedule_commit_time) {
		_schedule_commit_time = schedule_commit_time;
	}
	public int getTask_bundle_status() {
		return _task_bundle_status;
	}
	public void setTask_bundle_status(int task_bundle_status) {
		_task_bundle_status = task_bundle_status;
	}
	public int getData_row_count_of_total() {
		return _data_row_count_of_total;
	}
	public void setData_row_count_of_total(int data_row_count_of_total) {
		_data_row_count_of_total = data_row_count_of_total;
	}
	public int getData_row_count_of_did_commit() {
		return _data_row_count_of_did_commit;
	}
	public void setData_row_count_of_did_commit(int data_row_count_of_did_commit) {
		_data_row_count_of_did_commit = data_row_count_of_did_commit;
	}
	
	
}
