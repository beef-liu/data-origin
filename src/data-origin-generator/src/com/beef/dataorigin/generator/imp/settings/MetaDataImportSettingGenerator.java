package com.beef.dataorigin.generator.imp.settings;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import MetoXML.XmlSerializer;

import com.beef.dataorigin.generator.DataOriginGenerator;
import com.beef.dataorigin.generator.DataOriginGeneratorContext;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;

public class MetaDataImportSettingGenerator {
	public static void generateAll(DataOriginGeneratorContext generatorContext) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		
		for(int i = 0; i < generatorContext.getDbTableList().size(); i++) {
			generateTable(generatorContext, generatorContext.getDbTableList().get(i));
		}
	}
	
	public static void generateTable(DataOriginGeneratorContext generatorContext, DBTable dbTable) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		File outputDir = generatorContext.getDataOriginDirManager().getMetaDataImportSettingDir();
		
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
			dataField = MetaDataFieldGenerator.createMetaDataFieldByDBColumn(dbCol);
			
			fieldList.add(dataField);
		}
		
		setting.setFieldList(fieldList);
		
		
		//output xml
		String fileName = MetaDataImportSetting.defaultFileNameOfTable(dbTable.getTableName());
		File file = new File(outputDir, fileName);

		XmlSerializer xmlSer = new XmlSerializer();
		xmlSer.Serialize(file.getAbsolutePath(), setting, MetaDataImportSetting.class, DataOriginGenerator.DefaultCharset);
	}
	
}
