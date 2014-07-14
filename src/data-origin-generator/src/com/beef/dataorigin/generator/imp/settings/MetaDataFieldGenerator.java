package com.beef.dataorigin.generator.imp.settings;

import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.salama.modeldriven.util.db.DBColumn;

public class MetaDataFieldGenerator {
	public static MetaDataField createMetaDataFieldByDBColumn(DBColumn dbCol) {
		MetaDataField dataField = new MetaDataField();
		
		dataField.setFieldName(dbCol.getName());
		dataField.setFieldDispName(dbCol.getComment());

		//colMaxLen -------------------------------------------------
		int colMaxLen = 0;
		//e.g., char(32)
		String colType = dbCol.getColumnType().toLowerCase();
		int index0 = colType.indexOf('(');
		if(index0 > 0) {
			int index1 = colType.indexOf(')', index0);
			
			if(index1 > 0) {
				String maxLenStr = colType.substring(index0 + 1, index1);
				int index2 = maxLenStr.indexOf(','); 
				if(index2 > 0) {
					maxLenStr = maxLenStr.substring(0, index2);
				}
				
				if(maxLenStr.length() > 0) {
					colMaxLen = Integer.parseInt(maxLenStr.trim()); 
				}
			}
		}
		dataField.setFieldDispMaxLength(colMaxLen);
		
		//DispFormat -------------------------------------------------
		String dispFormat = "";
		
		if(colType.startsWith("bigint")) {
			String colNameLower = dbCol.getName().toLowerCase();
			if(colNameLower.contains("time")) {
				dispFormat = DODataDaoUtil.FORMAT_DATE_YMD_HMS_MINUS;
			} else if(colNameLower.contains("date")) {
				dispFormat = DODataDaoUtil.FORMAT_DATE_YMD_MINUS;
			}
		}
		dataField.setFieldDispFormat(dispFormat);
		
		//FieldValidateRegex -------------------------------------------------
		StringBuilder validateRegex = new StringBuilder();
		StringBuilder validateComment = new StringBuilder();
		validateComment.append("Only");
		if(colType.startsWith("char")) {
			validateRegex.append("[a-zA-Z0-9 \\\\_\\-\\!\\~`\\@\\#\\$%\\^&\\*\\(\\)\\{\\}\\+\\=\\|\\[\\]'\":;\\,\\.\\/\\<\\>\\?]");
			validateComment.append(" letters, numbers, punctuations,");
		} else {
			validateRegex.append(".");
		}
		validateComment.append(" length");
		validateRegex.append("{");
		if(dbCol.isNullable()) {
			validateRegex.append("0");
		} else {
			validateRegex.append("1");
			validateComment.append(" > 0 and");
		}
		validateRegex.append(",").append(String.valueOf(colMaxLen));
		validateComment.append(" < " + colMaxLen);
		validateRegex.append("}");

		validateComment.append(" are allowed");
		
		dataField.setFieldValidateRegex(validateRegex.toString());
		dataField.setFieldValidateComment(validateComment.toString());

		return dataField; 
	}
}
