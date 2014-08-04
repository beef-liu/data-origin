package com.beef.dataorigin.web.data;

import java.io.Serializable;

public class DODataModificationCommitTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4269285485943363121L;
	
	private String _task_id = "";

	public String getTask_id() {
		return _task_id;
	}

	public void setTask_id(String value) {
		_task_id = value;
	}

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

	private String _sql_primary_key = "";

	public String getSql_primary_key() {
		return _sql_primary_key;
	}

	public void setSql_primary_key(String value) {
		_sql_primary_key = value;
	}

	private int _mod_type = 0;

	public int getMod_type() {
		return _mod_type;
	}

	public void setMod_type(int value) {
		_mod_type = value;
	}

	private long _commit_time = 0;

	public long getCommit_time() {
		return _commit_time;
	}

	public void setCommit_time(long value) {
		_commit_time = value;
	}

	private int _retried_count = 0;

	public int getRetried_count() {
		return _retried_count;
	}

	public void setRetried_count(int value) {
		_retried_count = value;
	}

	private int _max_retry = 0;

	public int getMax_retry() {
		return _max_retry;
	}

	public void setMax_retry(int value) {
		_max_retry = value;
	}

	private int _commit_status = 0;

	public int getCommit_status() {
		return _commit_status;
	}

	public void setCommit_status(int value) {
		_commit_status = value;
	}

	private String _error_msg = "";

	public String getError_msg() {
		return _error_msg;
	}

	public void setError_msg(String value) {
		_error_msg = value;
	}

	private long _update_time = 0;

	public long getUpdate_time() {
		return _update_time;
	}

	public void setUpdate_time(long value) {
		_update_time = value;
	}

	private String _update_admin = "";

	public String getUpdate_admin() {
		return _update_admin;
	}

	public void setUpdate_admin(String value) {
		_update_admin = value;
	}

}
