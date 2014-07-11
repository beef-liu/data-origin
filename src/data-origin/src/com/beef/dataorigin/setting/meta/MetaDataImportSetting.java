package com.beef.dataorigin.setting.meta;

import java.util.ArrayList;
import java.util.List;

import com.beef.dataorigin.setting.meta.data.MetaDataField;

public class MetaDataImportSetting {
	private String _dataTableName = "";
	
	private String _dataClassName = "";
	
	private String _bgColorError = "red";
	private String _bgColorDataRowInserted = "yellow";
	private String _bgColorDataRowUpdated = "green";
	
	private List<MetaDataField> _fieldList = new ArrayList<MetaDataField>();

	public final static String defaultFileNameOfTable(String tableName) {
		return "DataImportSetting_" + tableName.toLowerCase() + ".xml";
	}
	
	public String getDataTableName() {
		return _dataTableName;
	}

	public void setDataTableName(String dataTableName) {
		_dataTableName = dataTableName;
	}

	public String getDataClassName() {
		return _dataClassName;
	}

	public void setDataClassName(String dataClassName) {
		_dataClassName = dataClassName;
	}

	public List<MetaDataField> getFieldList() {
		return _fieldList;
	}

	public void setFieldList(List<MetaDataField> fieldList) {
		_fieldList = fieldList;
	}

	public String getBgColorError() {
		return _bgColorError;
	}

	public void setBgColorError(String bgColorError) {
		_bgColorError = bgColorError;
	}

	public String getBgColorDataRowInserted() {
		return _bgColorDataRowInserted;
	}

	public void setBgColorDataRowInserted(String bgColorDataRowInserted) {
		_bgColorDataRowInserted = bgColorDataRowInserted;
	}

	public String getBgColorDataRowUpdated() {
		return _bgColorDataRowUpdated;
	}

	public void setBgColorDataRowUpdated(String bgColorDataRowUpdated) {
		_bgColorDataRowUpdated = bgColorDataRowUpdated;
	}

	
}
