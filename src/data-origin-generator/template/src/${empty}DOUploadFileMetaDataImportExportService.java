package ${basePackage}.service;

import com.beef.dataorigin.web.service.DODataImportExportService;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;

public class DOUploadFileMetaDataImportExportService extends DODataImportExportService {

	public String exportDataExcel(RequestWrapper request,ResponseWrapper response,
			String searchConditionXml) {
		String tableName = "douploadfilemeta";
		return super.exportDataExcel(request, response, tableName, searchConditionXml);
	}

	public void downloadTempExcel(RequestWrapper request,
			ResponseWrapper response, String fileName) {
		String tableName = "douploadfilemeta";
		super.downloadTempExcel(request, response, tableName, fileName);
	}
	
	public String checkDataExcelSheetCount(RequestWrapper request,
			ResponseWrapper response) {
		return super.checkDataExcelSheetCount(request, response);
	}

	public String checkDataExcelTitleRow(RequestWrapper request,
			ResponseWrapper response,
			String fileName,
			int sheetIndex) {
		String tableName = "douploadfilemeta";
		return super.checkDataExcelTitleRow(request, response, tableName, fileName, sheetIndex);
	}
	
	public String importDataExcel(RequestWrapper request,
			ResponseWrapper response,
			String fileName,
			int sheetIndex,
			String colValueListXml) {
		String tableName = "douploadfilemeta";
		return super.importDataExcel(request, response, tableName, fileName, sheetIndex, colValueListXml);
	}
	
}
