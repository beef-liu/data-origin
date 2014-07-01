package com.beef.dataorigin.setting.meta.data;

import java.util.ArrayList;
import java.util.List;

public class MetaDataField {
	private String _fieldName = "";
	
	private String _fieldDispName = "";

	private int _fieldDispMaxLength = 0;
	
	private String _fieldDispFormat = "";
	
	private String _fieldValidateRegex = "";

	/**
	 * Option list for select or checkbox
	 */
	private List<MetaOptionPair> _valueLabelList = new ArrayList<MetaOptionPair>();
	
	public List<MetaOptionPair> getValueLabelList() {
		return _valueLabelList;
	}

	public void setValueLabelList(List<MetaOptionPair> valueLabelList) {
		_valueLabelList = valueLabelList;
	}

	public String getFieldName() {
		return _fieldName;
	}

	public void setFieldName(String fieldName) {
		_fieldName = fieldName;
	}

	public String getFieldDispName() {
		return _fieldDispName;
	}

	public void setFieldDispName(String fieldDispName) {
		_fieldDispName = fieldDispName;
	}

	public int getFieldDispMaxLength() {
		return _fieldDispMaxLength;
	}

	public void setFieldDispMaxLength(int fieldDispMaxLength) {
		_fieldDispMaxLength = fieldDispMaxLength;
	}

	public String getFieldDispFormat() {
		return _fieldDispFormat;
	}

	public void setFieldDispFormat(String fieldDispFormat) {
		_fieldDispFormat = fieldDispFormat;
	}

	public String getFieldValidateRegex() {
		return _fieldValidateRegex;
	}

	public void setFieldValidateRegex(String fieldValidateRegex) {
		_fieldValidateRegex = fieldValidateRegex;
	}

	
}
