package com.beef.dataorigin.generator.junittest;

import com.beef.dataorigin.web.service.DODataSearchService;

public class TemplateDataSearchService extends DODataSearchService {

	public String searchDataCount(String searchConditionXml) {
		String tableName = "";
		return super.searchDataCount(tableName, searchConditionXml);
	}
	
	public String searchData(int beginIndex, int pageSize, 
			String searchConditionXml, String orderByFields) {
		String tableName = "";
		return super.searchData(beginIndex, pageSize, tableName, searchConditionXml,
				orderByFields);
	}
	
}
