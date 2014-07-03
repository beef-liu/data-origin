package com.beef.dataorigin.setting.meta;

import java.util.ArrayList;
import java.util.List;

import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.setting.meta.data.MetaSearchCondition;

public class MetaDataUISetting {
	private String _dataTableName = "";
	
	private String _dataClassName = "";

	private MetaSearchCondition _searchCondition;
	
	private List<MetaDataField> _fieldList = new ArrayList<MetaDataField>();

	public final static String defaultFileNameOfTable(String tableName) {
		return "DataUISetting_" + tableName.toLowerCase() + ".xml";
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

	public MetaSearchCondition getSearchCondition() {
		return _searchCondition;
	}

	public void setSearchCondition(MetaSearchCondition searchCondition) {
		_searchCondition = searchCondition;
	}

	public List<MetaDataField> getFieldList() {
		return _fieldList;
	}

	public void setFieldList(List<MetaDataField> fieldList) {
		_fieldList = fieldList;
	}
	
}
