package com.beef.dataorigin.test.ws.service;

import com.beef.dataorigin.web.service.DODataDetailService;

public class DODataModificationCommitTaskDataDetailService extends DODataDetailService {

	public String findDataByPK(String dataXml) {
		String tableName = "dodatamodificationcommittask";
		return super.findDataByPK(tableName, dataXml);
	}
	
	public String deleteDataByPK(String dataXml) {
		String tableName = "dodatamodificationcommittask";
		return super.deleteDataByPK(tableName, dataXml);
	}

	public String updateDataByPK(String dataXml) {
		String tableName = "dodatamodificationcommittask";
		return super.updateDataByPK(tableName, dataXml);
	}
	
	public String insertData(String dataXml) {
		String tableName = "dodatamodificationcommittask";
		return super.insertData(tableName, dataXml);
	}
	
	public String deleteDataByPKList(String dataListXml) {
		String tableName = "dodatamodificationcommittask";
		return super.deleteDataByPKList(tableName, dataListXml);
	}
	
}
