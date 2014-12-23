package ${basePackage}.service;

import com.beef.dataorigin.web.service.DODataSearchService;

public class DODataModificationCommitTaskDataSearchService extends DODataSearchService {

	public String searchDataCount(String searchConditionXml) {
		String tableName = "dodatamodificationcommittask";
		return super.searchDataCount(tableName, searchConditionXml);
	}
	
	public String searchData(int beginIndex, int pageSize, 
			String searchConditionXml, String orderByFields) {
		String tableName = "dodatamodificationcommittask";
		return super.searchData(beginIndex, pageSize, tableName, searchConditionXml,
				orderByFields);
	}
	
}
