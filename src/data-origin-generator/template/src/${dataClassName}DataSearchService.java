package ${basePackage}.service;

import com.beef.dataorigin.web.service.DODataSearchService;

public class ${dataClassName}DataSearchService extends DODataSearchService {

	public String searchDataCount(String searchConditionXml) {
		String tableName = "${tableName}";
		return super.searchDataCount(tableName, searchConditionXml);
	}
	
	public String searchData(int beginIndex, int pageSize, 
			String searchConditionXml, String orderByFields) {
		String tableName = "${tableName}";
		return super.searchData(beginIndex, pageSize, tableName, searchConditionXml,
				orderByFields);
	}
	
}
