package com.beef.dataorigin.web.data;

import java.io.Serializable;

public class DODataModificationCommitTaskBundle implements Serializable {

	private String _table_name = "";

	public String getTable_name() {
		return _table_name;
	}

	public void setTable_name(String value) {
		_table_name = value;
	}

	private long _schedule_commit_time = 0;

	public long getSchedule_commit_time() {
		return _schedule_commit_time;
	}

	public void setSchedule_commit_time(long value) {
		_schedule_commit_time = value;
	}

	private int _task_bundle_status = 0;

	public int getTask_bundle_status() {
		return _task_bundle_status;
	}

	public void setTask_bundle_status(int value) {
		_task_bundle_status = value;
	}

	private int _data_row_count_of_total = 0;

	public int getData_row_count_of_total() {
		return _data_row_count_of_total;
	}

	public void setData_row_count_of_total(int value) {
		_data_row_count_of_total = value;
	}

	private int _data_row_count_of_did_commit = 0;

	public int getData_row_count_of_did_commit() {
		return _data_row_count_of_did_commit;
	}

	public void setData_row_count_of_did_commit(int value) {
		_data_row_count_of_did_commit = value;
	}

	private long _commit_start_time = 0;

	public long getCommit_start_time() {
		return _commit_start_time;
	}

	public void setCommit_start_time(long value) {
		_commit_start_time = value;
	}

	private long _commit_finish_time = 0;

	public long getCommit_finish_time() {
		return _commit_finish_time;
	}

	public void setCommit_finish_time(long value) {
		_commit_finish_time = value;
	}

	private long _update_time = 0;

	public long getUpdate_time() {
		return _update_time;
	}

	public void setUpdate_time(long value) {
		_update_time = value;
	}

}
