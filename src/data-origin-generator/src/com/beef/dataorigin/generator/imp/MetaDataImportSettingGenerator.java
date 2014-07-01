package com.beef.dataorigin.generator.imp;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import MetoXML.XmlSerializer;

import com.beef.dataorigin.generator.DataOriginGenerator;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;

public class MetaDataImportSettingGenerator {
	public static void generateAll(Connection conn, List<DBTable> _dbTableList, File outputDir) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		
		for(int i = 0; i < _dbTableList.size(); i++) {
			generateTable(conn, _dbTableList.get(i), outputDir);
		}
	}
	
	public static void generateTable(Connection conn, DBTable dbTable, File outputDir) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		MetaDataImportSetting setting = new MetaDataImportSetting();
		
		setting.setDataTableName(dbTable.getTableName());
		setting.setDataClassName(dbTable.getTableName());
		
		//field
		List<MetaDataField> fieldList = new ArrayList<MetaDataField>();
		List<DBColumn> dbColList = dbTable.getColumns();
		MetaDataField dataField = null;
		DBColumn dbCol = null;
		for(int i = 0; i < dbColList.size(); i++) {
			dbCol = dbColList.get(i);
			dataField = createMetaDataFieldByDBColumn(dbCol);
			
			fieldList.add(dataField);
		}
		
		setting.setFieldList(fieldList);
		
		
		//output xml
		String fileName = MetaDataImportSetting.defaultFileNameOfTable(dbTable.getTableName());
		File file = new File(outputDir, fileName);

		XmlSerializer xmlSer = new XmlSerializer();
		xmlSer.Serialize(file.getAbsolutePath(), setting, MetaDataImportSetting.class, DataOriginGenerator.DefaultCharset);
	}
	
	private static MetaDataField createMetaDataFieldByDBColumn(DBColumn dbCol) {
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
		
		dataField.setFieldDispFormat(dispFormat);
		
		//DispFormat -------------------------------------------------
		String fieldValidateRegex = "";
		
		dataField.setFieldValidateRegex(fieldValidateRegex);
		
		
		return dataField; 
	}
	
}
