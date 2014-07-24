package ${basePackage}.service;

import com.beef.dataorigin.web.service.DODataDetailService;

public class ${dataClassName}DataDetailService extends DODataDetailService {

	public String findDataByPK(String dataXml) {
		String tableName = "${tableName}";
		return super.findDataByPK(tableName, dataXml);
	}
	
	public String deleteDataByPK(String dataXml) {
		String tableName = "${tableName}";
		return super.deleteDataByPK(tableName, dataXml);
	}

	public String updateDataByPK(String dataXml) {
		String tableName = "${tableName}";
		return super.updateDataByPK(tableName, dataXml);
	}
	
	public String insertData(String dataXml) {
		String tableName = "${tableName}";
		return super.insertData(tableName, dataXml);
	}
	
}
