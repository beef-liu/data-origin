package com.beef.dataorigin.web.service;

import com.beef.dataorigin.web.service.DODataDetailService;

public class DOAdminDataDetailService extends DODataDetailService {

	public String findDataByPK(String dataXml) {
		String tableName = "doadmin";
		return super.findDataByPK(tableName, dataXml);
	}
	
	public String deleteDataByPK(String dataXml) {
		String tableName = "doadmin";
		return super.deleteDataByPK(tableName, dataXml);
	}

	public String updateDataByPK(String dataXml) {
		String tableName = "doadmin";
		return super.updateDataByPK(tableName, dataXml);
	}
	
	public String insertData(String dataXml) {
		String tableName = "doadmin";
		return super.insertData(tableName, dataXml);
	}
	
	public String deleteDataByPKList(String dataListXml) {
		String tableName = "doadmin";
		return super.deleteDataByPKList(tableName, dataListXml);
	}
	
}
