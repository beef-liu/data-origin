package com.beef.dataorigin.web.datacommittask;

import com.beef.dataorigin.context.data.MDBTable;

public interface DODataModificationCommitTaskCallback {
	public static enum DataUpdateType {
		Delete, Update, Insert
	}; 
	
	public void didSuccessOfDataCommit(MDBTable mDBTable, String sqlPrimaryKey, Object data, DataUpdateType updateType);
	
	public void didFailOfDataCommit(MDBTable mDBTable, String sqlPrimaryKey, Object data, DataUpdateType updateType);
	
}
