package com.beef.dataorigin.generator.junittest;

import com.beef.dataorigin.web.service.DODataDetailService;

public class TemplateDataDetailService extends DODataDetailService {
	
	public String findDataByPK(String dataXml) {
		String tableName = "";
		return super.findDataByPK(tableName, dataXml);
	}
	
	public String deleteDataByPK(String dataXml) {
		String tableName = "";
		return super.deleteDataByPK(tableName, dataXml);
	}

	public String updateDataByPK(String dataXml) {
		String tableName = "";
		return super.updateDataByPK(tableName, dataXml);
	}
	
	public String insertData(String dataXml) {
		String tableName = "";
		return super.insertData(tableName, dataXml);
	}

	public String deleteDataByPKList(String dataXmlList) {
		String tableName = "";
		return super.deleteDataByPKList(tableName, dataXmlList);
	}
}
