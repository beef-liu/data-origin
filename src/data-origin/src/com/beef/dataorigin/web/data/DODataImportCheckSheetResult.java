package com.beef.dataorigin.web.data;

import java.util.List;

public class DODataImportCheckSheetResult {
	private int _sheetCount = 0;
	
	private String _importFile = "";
	
	private List<String> _sheetNameList = null;

	public int getSheetCount() {
		return _sheetCount;
	}

	public void setSheetCount(int sheetCount) {
		_sheetCount = sheetCount;
	}

	public String getImportFile() {
		return _importFile;
	}

	public void setImportFile(String importFile) {
		_importFile = importFile;
	}

	public List<String> getSheetNameList() {
		return _sheetNameList;
	}

	public void setSheetNameList(List<String> sheetNameList) {
		_sheetNameList = sheetNameList;
	}
	
}
