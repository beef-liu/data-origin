package com.beef.dataorigin.web.service;

import com.beef.dataorigin.web.service.DODataSearchService;

public class DOUploadFileMetaDataSearchService extends DODataSearchService {

	public String searchDataCount(String searchConditionXml) {
		String tableName = "douploadfilemeta";
		return super.searchDataCount(tableName, searchConditionXml);
	}
	
	public String searchData(int beginIndex, int pageSize, 
			String searchConditionXml, String orderByFields) {
		String tableName = "douploadfilemeta";
		return super.searchData(beginIndex, pageSize, tableName, searchConditionXml,
				orderByFields);
	}
	
}
