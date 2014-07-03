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
import com.beef.dataorigin.setting.meta.MetaDataUISetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.setting.meta.data.MetaSearchCondition;
import com.beef.dataorigin.setting.meta.data.MetaSearchConditionItem;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;

public class MetaDataUISettingGenerator {
	private final static int DEFAULT_SEARCH_CONDITION_COUNT = 5;
	
	public static void generateAll(DataOriginGeneratorContext generatorContext) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		
		for(int i = 0; i < generatorContext.getDbTableList().size(); i++) {
			generateTable(generatorContext, generatorContext.getDbTableList().get(i));
		}
	}
	
	public static void generateTable(DataOriginGeneratorContext generatorContext, DBTable dbTable) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		File outputDir = generatorContext.getDataOriginDirManager().getMetaDataUISettingDir();
		
		MetaDataUISetting setting = new MetaDataUISetting();
		
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
		
		//search condition
		MetaSearchCondition searchCondition = new MetaSearchCondition();
		MetaSearchConditionItem searchConditionItem;
		for(int i = 0; i < dbColList.size(); i++) {
			if(i >= DEFAULT_SEARCH_CONDITION_COUNT) {
				break;
			}

			dbCol = dbColList.get(i);
			
			searchConditionItem = new MetaSearchConditionItem();
			searchConditionItem.setFieldName(dbCol.getName());
			searchConditionItem.setInputType("text");
			
			searchCondition.getSearchConditionItemList().add(searchConditionItem);
		}
		setting.setSearchCondition(searchCondition);
		
		//output xml
		String fileName = MetaDataUISetting.defaultFileNameOfTable(dbTable.getTableName());
		File file = new File(outputDir, fileName);

		XmlSerializer xmlSer = new XmlSerializer();
		xmlSer.Serialize(file.getAbsolutePath(), setting, MetaDataUISetting.class, DataOriginGenerator.DefaultCharset);
	}
	
}
