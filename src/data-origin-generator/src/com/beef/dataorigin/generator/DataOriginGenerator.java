package com.beef.dataorigin.generator;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import MetoXML.XmlSerializer;
import MetoXML.Base.XmlParseException;

import com.beef.dataorigin.context.DataOriginDirManager;
import com.beef.dataorigin.generator.imp.settings.DBDataClassGenerator;
import com.beef.dataorigin.generator.imp.settings.MetaDataImportSettingGenerator;
import com.beef.dataorigin.generator.imp.settings.MetaDataUISettingGenerator;
import com.beef.dataorigin.generator.imp.web.WebGenerator;
import com.beef.dataorigin.generator.util.DataOriginGeneratorUtil;
import com.beef.dataorigin.setting.DataOriginSetting;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.modeldriven.util.db.mysql.MysqlTableInfoUtil;
import com.salama.util.ResourceUtil;

public class DataOriginGenerator {
	private final static Logger logger = Logger.getLogger(DataOriginGenerator.class);
	
	public final static Charset DefaultCharset = Charset.forName("utf-8");

//	public final static String DIR_SRC = "src";
//	public final static String DIR_WEBAPP = "webapp";
//	public final static String DIR_WEB_INF = "WEB-INF";

	//public final static String PROP_KEY_DB_DATA_JAVA_PACKAGE = "dbdata.java.package";
	
	///////////////////////// private variables //////////////////////////////
	private DataOriginGeneratorContext _generatorContext;
	
	public static void main(String[] args) {
		try {
			DataOriginGenerator generator = new DataOriginGenerator();

			if(args == null || args.length == 0) {
				generator.generateAllSettings();
			} else {
				String taskType = args[0];
				if(taskType.equals("web")) {
					
					boolean isOverwrite = false;
					if(args.length >= 2 && args[1].equals("overwrite")) {
						isOverwrite = true;
					}
					
					generator.generateWeb(isOverwrite);
				}
			}
		} catch(Throwable e) {
			logger.error(null, e);
		}
	}
	
	protected DataOriginGenerator() throws ClassNotFoundException, SQLException {
		_generatorContext = new DataOriginGeneratorContext();
	}
	
	
	protected void generateAllSettings() throws ResourceNotFoundException, ParseErrorException, Exception {
		generateDBTableDefinition();
		
		generateDBDataClass();
		
		generateMeta();
		
	}
	
	protected void generateWeb(boolean isOverwrite) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException, XmlParseException, InstantiationException, NoSuchMethodException {
		WebGenerator.generateAll(_generatorContext, isOverwrite);
	}
	
	
	protected void generateDBTableDefinition() throws ClassNotFoundException, SQLException, IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		//output db tables xml
		String dbTableName;
		DBTable dbTable;
		File dbTableFile; 
		for(int i = 0; i < _generatorContext.getDbTableNameList().size(); i++) {
			dbTable = _generatorContext.getDbTableList().get(i);
			dbTableName = dbTable.getTableName();
			
			dbTableFile = new File(_generatorContext.getDataOriginDirManager().getDbTablesDir(), dbTableName + ".xml");
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(dbTableFile.getAbsolutePath(), dbTable, DBTable.class, DefaultCharset);
		}
	}
	
	protected void generateDBDataClass() throws ResourceNotFoundException, ParseErrorException, Exception {
		logger.debug("DBDataClassGenerator.generateAll() --------");
		DBDataClassGenerator.generateAll(_generatorContext);
	}
	
	protected void generateMeta() throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SQLException {
		//copy default DataOriginSetting.xml
		String dataOriginSettingName = DataOriginSetting.class.getSimpleName() + ".xml";
		File dataOriginSettingDest = new File(_generatorContext.getDataOriginDirManager().getBaseDir(), dataOriginSettingName);
		if(!dataOriginSettingDest.exists()) {
			File dataOriginSettingSrc = new File(new File(_generatorContext.getTemplateDir(), DataOriginGeneratorContext.DIR_DATA_ORIGIN_BASE), dataOriginSettingName);
			DataOriginGeneratorUtil.copyFile(dataOriginSettingSrc, dataOriginSettingDest);
		}
		
		//generate Data import setting
		logger.debug("MetaDataImportSettingGenerator.generateAll() --------");
		MetaDataImportSettingGenerator.generateAll(_generatorContext);

		logger.debug("MetaDataUISettingGenerator.generateAll() --------");
		MetaDataUISettingGenerator.generateAll(_generatorContext);
	}
	

}
