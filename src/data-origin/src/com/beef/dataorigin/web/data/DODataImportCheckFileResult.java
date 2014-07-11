package com.beef.dataorigin.web.data;

import java.util.List;

public class DODataImportCheckFileResult {
	public final static String RESULT_SUCCESS = "success";
	public final static String RESULT_FAIL = "fail";
	
	private String _result;
	
	/* if not success, then has some cases:
	 * 1. Some title is unknown column, need to be confirmed
	 * 2. Prompt user whether these unassigned columns need to be assigned fixed value
	 * 3. Prompt user columns which are primary keys can not be empty
	 */
	
	private List<String> _colTitleList;
	
	/**
	 * column meta info of every col, _colMetaList.get(i).getDbCol() == null when the title of the column can not be decided which DB column belong to
	 */
	private List<DODataImportColMetaInfo> _colMetaList;
	
	private List<DODataImportColMetaInfo> _lackingColMetaList;

	public String getResult() {
		return _result;
	}

	public void setResult(String result) {
		_result = result;
	}

	public List<DODataImportColMetaInfo> getColMetaList() {
		return _colMetaList;
	}

	public void setColMetaList(List<DODataImportColMetaInfo> colMetaList) {
		_colMetaList = colMetaList;
	}

	public List<DODataImportColMetaInfo> getLackingColMetaList() {
		return _lackingColMetaList;
	}

	public void setLackingColMetaList(
			List<DODataImportColMetaInfo> lackingColMetaList) {
		_lackingColMetaList = lackingColMetaList;
	}

	public List<String> getColTitleList() {
		return _colTitleList;
	}

	public void setColTitleList(List<String> colTitleList) {
		_colTitleList = colTitleList;
	}
	
}
