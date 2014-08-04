package com.beef.dataorigin.test.ws.service;

import com.beef.dataorigin.web.service.DODataSearchService;

public class DODataModificationCommitTaskBundleDataSearchService extends DODataSearchService {

	public String searchDataCount(String searchConditionXml) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.searchDataCount(tableName, searchConditionXml);
	}
	
	public String searchData(int beginIndex, int pageSize, 
			String searchConditionXml, String orderByFields) {
		String tableName = "dodatamodificationcommittaskbundle";
		return super.searchData(beginIndex, pageSize, tableName, searchConditionXml,
				orderByFields);
	}
	
}
