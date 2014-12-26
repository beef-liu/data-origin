package com.beef.dataorigin.generator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.beef.dataorigin.context.DataOriginDirManager;
import com.beef.dataorigin.generator.DataOriginGenerator.DataOriginGenerateTaskType;
import com.beef.dataorigin.generator.util.DataOriginGeneratorUtil;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.modeldriven.util.db.mysql.MysqlTableInfoUtil;
import com.salama.util.ResourceUtil;

public class DataOriginGeneratorContext {
	private final static Logger logger = Logger.getLogger(DataOriginGeneratorContext.class);
	
	public final static String DefaultCharsetName = "utf-8";
	public final static Charset DefaultCharset = Charset.forName(DefaultCharsetName);

	public final static String DIR_DATA_ORIGIN_BASE = "webapp/WEB-INF/data-origin";
	public final static String DIR_JAVA_SRC = "src";
	
	public final static String JAVA_SUB_PACKAGE_DATA_DB = "data.db";
	public final static String JAVA_SUB_PACKAGE_SERVICE = "service";
	public final static String JAVA_SUB_PACKAGE_DAO = "dao";

	public final static String PROP_KEY_DATA_ORIGIN_TEMPLATE_DIR = "data-origin.generate.template.dir";
	public final static String PROP_KEY_DATA_ORIGIN_OUTPUT_WEB_PROJECT_DIR = "data-origin.generate.output.web.project.dir";
	public final static String PROP_KEY_DATA_ORIGIN_OUTPUT_WEB_PROJECT_JAVA_PACKAGE = "data-origin.generate.output.web.project.java.package";
	public final static String PROP_KEY_DATA_ORIGIN_OUTPUT_WEB_CONTEXT_NAME = "data-origin.generate.output.web.contextname";

	public final static String PROP_KEY_DB_DRIVER = "db.driver";
	
	public final static String PROP_KEY_PRODUCTION_DB_URL = "production.db.url";
	public final static String PROP_KEY_PRODUCTION_DB_USER = "production.db.user";
	public final static String PROP_KEY_PRODUCTION_DB_PASSWORD = "production.db.password";
	
	public final static String PROP_KEY_ONEDITING_DB_URL = "onediting.db.url";
	public final static String PROP_KEY_ONEDITING_DB_USER = "onediting.db.user";
	public final static String PROP_KEY_ONEDITING_DB_PASSWORD = "onediting.db.password";

	
	private List<String> _dbTableNameList = null;
	private List<DBTable> _dbTableList = new ArrayList<DBTable>();
	
	private DataOriginDirManager _dataOriginDirManager = null;
	
	private File _templateDir;
	private File _outputWebProjectDir;
	private File _outputWebProjectJavaSrcDir;
	private String _outputWebProjectJavaPackage;
	private String _outputWebContextName;
	
	//private HashSet<String> _builtInTableNameSet = new HashSet<String>();
	
	private Properties _dataOriginProperties = null;
	public DataOriginGeneratorContext(DataOriginGenerateTaskType taskType) throws ClassNotFoundException, SQLException, IOException {
		
		_dataOriginProperties = ResourceUtil.getProperties("/data-origin-generator.properties");
		
		makeBaseDir();
		
		initDBTables();
		
		if(taskType == DataOriginGenerateTaskType.GenerateMeta) {
			initBuiltinDBTables();
		}
	}
	
	protected void makeBaseDir() {
		_templateDir = new File(getValueByPropertyKey(PROP_KEY_DATA_ORIGIN_TEMPLATE_DIR));
		_outputWebProjectDir = new File(getValueByPropertyKey(PROP_KEY_DATA_ORIGIN_OUTPUT_WEB_PROJECT_DIR));
		_outputWebProjectJavaSrcDir = new File(_outputWebProjectDir, DIR_JAVA_SRC);
		_outputWebProjectJavaPackage = getValueByPropertyKey(PROP_KEY_DATA_ORIGIN_OUTPUT_WEB_PROJECT_JAVA_PACKAGE);
		_outputWebContextName = getValueByPropertyKey(PROP_KEY_DATA_ORIGIN_OUTPUT_WEB_CONTEXT_NAME);
		
		File baseDir = new File(_outputWebProjectDir, DIR_DATA_ORIGIN_BASE);
		if(!baseDir.exists()) {
			baseDir.mkdirs();
		}
		_dataOriginDirManager = new DataOriginDirManager(baseDir);
	}
	
	protected void initBuiltinDBTables() throws IOException {
		Connection conn = null;
		
		try {
			//create builtin tables -----------------------------------------------------
			//read sql of create table
			InputStream sqlSourceInput = DataOriginGenerator.class.getResourceAsStream("/data-origin-db.sql");
			ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
			DataOriginGeneratorUtil.copy(sqlSourceInput, bytesOutput);
			String sqlSource = bytesOutput.toString(DataOriginGeneratorContext.DefaultCharsetName);
			//split to array of create table sql
			String[] createTableSqlArray = sqlSource.split("(;[ \t\r]*[\n]+)");

			//DB operation
			conn = createConnectionOfOnEditingDB();
			
			PreparedStatement stmt = null;
			for(int i = 0; i < createTableSqlArray.length; i++) {
				try {
					stmt = conn.prepareStatement(createTableSqlArray[i]);
					stmt.executeUpdate();
				} catch(Exception e) {
					if((e.getClass() == MySQLSyntaxErrorException.class 
							&& ((MySQLSyntaxErrorException)e).getSQLState().equals("42S01")
						)
						||
						(e.getClass() == com.mysql.jdbc.exceptions.MySQLSyntaxErrorException.class
							&& ((com.mysql.jdbc.exceptions.MySQLSyntaxErrorException)e).getSQLState().equals("42S01")
						)
					) {
						//table exists
						logger.info("initBuiltinDBTables() table exists:\n" + createTableSqlArray[i]);
					} else {
						throw e;
					}
				} finally {
					try {
						stmt.close();
					} catch(Throwable e) {
					}
				}
			}
			
			
			//add dbTable into list -----------------------------------------------
			addDBTableByTableName(conn, "DOAdmin");
			addDBTableByTableName(conn, "DOUploadFileMeta");
			addDBTableByTableName(conn, "DODataModificationCommitTask");
			addDBTableByTableName(conn, "DODataModificationCommitTaskBundle");
			
		} catch(Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
		
	}
	
	private void addDBTableByTableName(Connection conn, String dbTableName) throws SQLException {
		_dbTableNameList.add(dbTableName);

		DBTable dbTable = MysqlTableInfoUtil.GetTable(conn, dbTableName);
		_dbTableList.add(dbTable);
	}
	
	protected void initDBTables() throws ClassNotFoundException, SQLException {
		Connection conn = null;
		
		try {
			conn = createConnectionOfProductionDB();
			
			_dbTableNameList = MysqlTableInfoUtil.getAllTables(conn);
			
			//output db tables xml
			String dbTableName = null;
			DBTable dbTable = null;
			for(int i = 0; i < _dbTableNameList.size(); i++) {
				dbTableName = _dbTableNameList.get(i);
				dbTable = MysqlTableInfoUtil.GetTable(conn, dbTableName);
				
				_dbTableList.add(dbTable);
			}
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	public Connection createConnectionOfProductionDB() throws ClassNotFoundException, SQLException {
		return createConnection(
				getValueByPropertyKey(PROP_KEY_DB_DRIVER), 
				getValueByPropertyKey(PROP_KEY_PRODUCTION_DB_URL), 
				getValueByPropertyKey(PROP_KEY_PRODUCTION_DB_USER), 
				getValueByPropertyKey(PROP_KEY_PRODUCTION_DB_PASSWORD)
				);
	}

	public Connection createConnectionOfOnEditingDB() throws ClassNotFoundException, SQLException {
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
	

	

	/***************** Getter Setter *******************/
	private String getValueByPropertyKey(String key) {
		return _dataOriginProperties.getProperty(key);
	}
	
	public List<String> getDbTableNameList() {
		return _dbTableNameList;
	}
	public void setDbTableNameList(List<String> dbTableNameList) {
		_dbTableNameList = dbTableNameList;
	}
	public List<DBTable> getDbTableList() {
		return _dbTableList;
	}
	public void setDbTableList(List<DBTable> dbTableList) {
		_dbTableList = dbTableList;
	}
	public DataOriginDirManager getDataOriginDirManager() {
		return _dataOriginDirManager;
	}
	public void setDataOriginDirManager(DataOriginDirManager dataOriginDirManager) {
		_dataOriginDirManager = dataOriginDirManager;
	}
	public File getTemplateDir() {
		return _templateDir;
	}
	public void setTemplateDir(File templateDir) {
		_templateDir = templateDir;
	}
	public File getOutputWebProjectDir() {
		return _outputWebProjectDir;
	}
	public void setOutputWebProjectDir(File outputWebProjectDir) {
		_outputWebProjectDir = outputWebProjectDir;
	}
	public String getOutputWebProjectJavaPackage() {
		return _outputWebProjectJavaPackage;
	}
	public void setOutputWebProjectJavaPackage(String outputWebProjectJavaPackage) {
		_outputWebProjectJavaPackage = outputWebProjectJavaPackage;
	}

	public File getOutputWebProjectJavaSrcDir() {
		return _outputWebProjectJavaSrcDir;
	}

	public void setOutputWebProjectJavaSrcDir(File outputWebProjectJavaSrcDir) {
		_outputWebProjectJavaSrcDir = outputWebProjectJavaSrcDir;
	}
	
	public String getOutputWebContextName() {
		return _outputWebContextName;
	}

	public void setOutputWebContextName(String outputWebContextName) {
		_outputWebContextName = outputWebContextName;
	}

}
