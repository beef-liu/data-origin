package com.beef.dataorigin.setting.meta.data;

public class MetaSearchConditionItem {
	public final static String INPUT_TYPE_TEXT = "text";
	public final static String INPUT_TYPE_SELECT = "select";
	public final static String INPUT_TYPE_CHECK_BOX = "checkbox";

	private String _fieldName = "";
	
	private String _inputType = "";
	
	public String getFieldName() {
		return _fieldName;
	}

	public void setFieldName(String fieldName) {
		_fieldName = fieldName;
	}

	public String getInputType() {
		return _inputType;
	}

	public void setInputType(String inputType) {
		_inputType = inputType;
	}

	
	
}
