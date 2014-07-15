package com.beef.dataorigin.web.data;

public class DODataImportResult {
	private String _tableName;
	private String _tableComment;
	
	private int _totalCount = 0;
	private int _insertedCount = 0;
	private int _updatedCount = 0;
	private int _errorCount = 0;
	
	private String _importResultFile;

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

	public int getInsertedCount() {
		return _insertedCount;
	}

	public void setInsertedCount(int insertedCount) {
		_insertedCount = insertedCount;
	}

	public int getUpdatedCount() {
		return _updatedCount;
	}

	public void setUpdatedCount(int updatedCount) {
		_updatedCount = updatedCount;
	}

	public int getErrorCount() {
		return _errorCount;
	}

	public void setErrorCount(int errorCount) {
		_errorCount = errorCount;
	}

	public String getImportResultFile() {
		return _importResultFile;
	}

	public void setImportResultFile(String importResultFile) {
		_importResultFile = importResultFile;
	}

	
}
