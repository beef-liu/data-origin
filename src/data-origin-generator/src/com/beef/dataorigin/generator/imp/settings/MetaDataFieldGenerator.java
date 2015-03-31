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
		} else {
			if(colType.startsWith("text")) {
				colMaxLen = 65535;
			} else if (colType.startsWith("tinytext")) {
				colMaxLen = 255;
			} else if (colType.startsWith("mediumtext")) {
				colMaxLen = 16777215;
			} else if (colType.startsWith("longtext")) {
				colMaxLen = Integer.MAX_VALUE;
			} else if (colType.startsWith("tinyint")) {
				colMaxLen = 4;
			} else if (colType.startsWith("smallint")) {
				colMaxLen = 6;
			} else if (colType.startsWith("mediumint")) {
				colMaxLen = 8;
			} else if (colType.startsWith("int")) {
				colMaxLen = 10;
			} else if (colType.startsWith("bigint")) {
				colMaxLen = 20;
			} else if (colType.startsWith("float")) {
				colMaxLen = 255;
			} else if (colType.startsWith("double")) {
				colMaxLen = 255;
			} else if (colType.startsWith("decimal")) {
				colMaxLen = 255;
			} else if (colType.startsWith("datetime")) {
				colMaxLen = 20;
			} else if (colType.startsWith("date")) {
				colMaxLen = 10;
			} else if (colType.startsWith("time")) {
				colMaxLen = 8;
			} else if (colType.startsWith("timestamp")) {
				colMaxLen = 14;
			} else if (colType.startsWith("year")) {
				colMaxLen = 4;
			} else {
				colMaxLen = 65535;
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
