package com.beef.dataorigin.web.data;

public class DODataExportResult {
	private String _tableName;
	
	private String _tableComment;
	
	private int _totalCount;
	
	private String _exportResultFile;

	public String getTableName() {
		return _tableName;
	}

	public void setTableName(String tableName) {
		_tableName = tableName;
	}

	public String getTableComment() {
		return _tableComment;
	}

	public void setTableComment(String tableComment) {
		_tableComment = tableComment;
	}

	public int getTotalCount() {
		return _totalCount;
	}

	public void setTotalCount(int totalCount) {
		_totalCount = totalCount;
	}

	public String getExportResultFile() {
		return _exportResultFile;
	}

	public void setExportResultFile(String exportResultFile) {
		_exportResultFile = exportResultFile;
	}

	
}
