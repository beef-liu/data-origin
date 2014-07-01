package com.beef.dataorigin.context.data;

import java.util.HashMap;
import java.util.Map;

import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.setting.meta.data.MetaSearchCondition;

public class MMetaDataUISetting {
	private String _dataTableName = "";
	
	private String _dataClassName = "";

	private MetaSearchCondition _searchCondition;
	
	private Map<String, MetaDataField> _fieldMap = new HashMap<String, MetaDataField>();

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

	public Map<String, MetaDataField> getFieldMap() {
		return _fieldMap;
	}

	public void setFieldMap(Map<String, MetaDataField> fieldMap) {
		_fieldMap = fieldMap;
	}
	
}
