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

import com.beef.dataorigin.context.DataOriginDirManager;
import com.beef.dataorigin.generator.imp.DBDataClassGenerator;
import com.beef.dataorigin.generator.imp.MetaDataImportSettingGenerator;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.modeldriven.util.db.mysql.MysqlTableInfoUtil;
import com.salama.util.ResourceUtil;

public class DataOriginGenerator {
	private final static Logger logger = Logger.getLogger(DataOriginGenerator.class);
	
	public final static Charset DefaultCharset = Charset.forName("utf-8");
	
	public final static String PROP_KEY_DATA_ORIGIN_BASE_DIR = "data-origin.base.dir";

	public final static String PROP_KEY_DB_DRIVER = "db.driver";
	
	public final static String PROP_KEY_PRODUCTION_DB_URL = "production.db.url";
	public final static String PROP_KEY_PRODUCTION_DB_USER = "production.db.user";
	public final static String PROP_KEY_PRODUCTION_DB_PASSWORD = "production.db.password";
	
	public final static String PROP_KEY_ONEDITING_DB_URL = "onediting.db.url";
	public final static String PROP_KEY_ONEDITING_DB_USER = "onediting.db.user";
	public final static String PROP_KEY_ONEDITING_DB_PASSWORD = "onediting.db.password";

	public final static String PROP_KEY_DB_DATA_JAVA_PACKAGE = "dbdata.java.package";
	
	///////////////////////// private variables //////////////////////////////
	private Properties _dataOriginProperties = null;
	
	private List<String> _dbTableNameList = null;
	private List<DBTable> _dbTableList = new ArrayList<DBTable>();
	
	private DataOriginDirManager _dataOriginDirManager = null;
	
	public static void main(String[] args) {
		try {
			DataOriginGenerator generator = new DataOriginGenerator();
			generator.generateAll();
		} catch(Throwable e) {
			logger.error(null, e);
		}
	}
	
	protected DataOriginGenerator() {
		_dataOriginProperties = ResourceUtil.getProperties("/data-origin-generator.properties");
		makeBaseDir();
	}
	
	private String getValueByPropertyKey(String key) {
		return _dataOriginProperties.getProperty(key);
	}
	
	protected void generateAll() throws ResourceNotFoundException, ParseErrorException, Exception {
		generateDBTableDefinition();
		
		generateDBDataClass();
		
		generateMeta();
	}
	
	protected void makeBaseDir() {
		String baseDirPath = getValueByPropertyKey(PROP_KEY_DATA_ORIGIN_BASE_DIR);
		_dataOriginDirManager = new DataOriginDirManager(new File(baseDirPath));
	}
	
	protected void generateDBTableDefinition() throws ClassNotFoundException, SQLException, IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		Connection conn = null;
		
		try {
			conn = createConnectionOfProductionDB();
			
			_dbTableNameList = MysqlTableInfoUtil.getAllTables(conn);
			
			//output db tables xml
			String dbTableName = null;
			DBTable dbTable = null;
			File dbTableFile = null; 
			for(int i = 0; i < _dbTableNameList.size(); i++) {
				dbTableName = _dbTableNameList.get(i);
				dbTable = MysqlTableInfoUtil.GetTable(conn, dbTableName);
				
				dbTableFile = new File(_dataOriginDirManager.getDbTablesDir(), dbTableName + ".xml");
				XmlSerializer xmlSer = new XmlSerializer();
				xmlSer.Serialize(dbTableFile.getAbsolutePath(), dbTable, DBTable.class, DefaultCharset);
				
				_dbTableList.add(dbTable);
			}
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	protected void generateDBDataClass() throws ResourceNotFoundException, ParseErrorException, Exception {
		Connection conn = null;
		
		try {
			conn = createConnectionOfProductionDB();

			logger.debug("DBDataClassGenerator.generateAll() --------");
			DBDataClassGenerator.generateAll(conn, _dataOriginDirManager.getDbDataClassDir(), getValueByPropertyKey(PROP_KEY_DB_DATA_JAVA_PACKAGE));
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	protected void generateMeta() throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SQLException {
		//generate Data import setting
		Connection conn = null;
		
		try {
			conn = createConnectionOfProductionDB();
			
			logger.debug("MetaDataImportSettingGenerator.generateAll() --------");
			MetaDataImportSettingGenerator.generateAll(conn, _dbTableList, _dataOriginDirManager.getMetaDataImportSettingDir());
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
		
	}
	
	private Connection createConnectionOfProductionDB() throws ClassNotFoundException, SQLException {
		return createConnection(
				getValueByPropertyKey(PROP_KEY_DB_DRIVER), 
				getValueByPropertyKey(PROP_KEY_PRODUCTION_DB_URL), 
				getValueByPropertyKey(PROP_KEY_PRODUCTION_DB_USER), 
				getValueByPropertyKey(PROP_KEY_PRODUCTION_DB_PASSWORD)
				);
	}

	private Connection createConnectionOfOnEditingDB() throws ClassNotFoundException, SQLException {
		return createConnection(
				getValueByPropertyKey(PROP_KEY_DB_DRIVER), 
				getValueByPropertyKey(PROP_KEY_ONEDITING_DB_URL), 
				getValueByPropertyKey(PROP_KEY_ONEDITING_DB_USER), 
				getValueByPropertyKey(PROP_KEY_ONEDITING_DB_PASSWORD)
				);
	}
	
	private static Connection createConnection(
			String dbDriver,
			String dbUrl, String dbUser, String dbPassword) throws ClassNotFoundException, SQLException {
		Class.forName(dbDriver);
		return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
	}

}
