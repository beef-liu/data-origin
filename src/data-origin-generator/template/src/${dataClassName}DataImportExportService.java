package ${basePackage}.service;

import com.beef.dataorigin.web.service.DODataImportExportService;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;

public class ${dataClassName}DataImportExportService extends DODataImportExportService {

	public String checkDataExcelSheetCount(RequestWrapper request,
			ResponseWrapper response) {
		return super.checkDataExcelSheetCount(request, response);
	}
	
	public String checkDataExcelTitleRow(RequestWrapper request,
			ResponseWrapper response, int sheetIndex) {
		String tableName = "${tableName}";
		return super.checkDataExcelTitleRow(request, response, tableName, sheetIndex);
	}
	
	public String importDataExcel(RequestWrapper request,
			ResponseWrapper response, int sheetIndex,
			String colValueListXml) {
		String tableName = "${tableName}";
		return super.importDataExcel(request, response, tableName, sheetIndex,
				colValueListXml);
	}
	
}
