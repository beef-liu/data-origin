package com.beef.dataorigin.context;

import java.io.File;

import org.apache.log4j.Logger;

public class DataOriginDirManager {
	private final static Logger logger = Logger.getLogger(DataOriginDirManager.class);
	
	//public final static String SUB_DIR_DB_DATA_GENERATE = "dbdata-generate";
	public final static String SUB_DIR_DB_TABLES = "dbtables";

	public final static String SUB_DIR_META = "meta";
	public final static String SUB_DIR_META_DATA_IMPORT_SETTING = "data-import-setting";
	public final static String SUB_DIR_META_DATA_UI_SETTING = "data-ui-setting";

	private File _baseDir;
	//private File _dbDataClassDir;
	private File _dbTablesDir;
	private File _metaDir;
	private File _metaDataImportSettingDir;
	private File _metaDataUISettingDir;

	public File getBaseDir() {
		return _baseDir;
	}

	/*
	public File getDbDataClassDir() {
		return _dbDataClassDir;
	}
	*/

	public File getDbTablesDir() {
		return _dbTablesDir;
	}

	public File getMetaDir() {
		return _metaDir;
	}

	public File getMetaDataImportSettingDir() {
		return _metaDataImportSettingDir;
	}

	public File getMetaDataUISettingDir() {
		return _metaDataUISettingDir;
	}

	public DataOriginDirManager(File baseDir) {
		_baseDir = baseDir;
		checkDirExists(_baseDir);
		
		if(!_baseDir.exists()) {
			throw new RuntimeException("DataOriginGenerator makeBaseDir() failed");
		}

		/*
		_dbDataClassDir = new File(_baseDir, SUB_DIR_DB_DATA_GENERATE);
		checkDirExists(_dbDataClassDir);
		*/
		
		_dbTablesDir = new File(_baseDir, SUB_DIR_DB_TABLES);
		checkDirExists(_dbTablesDir);
		
		_metaDir = new File(_baseDir, SUB_DIR_META);
		checkDirExists(_metaDir);
		
		_metaDataImportSettingDir = new File(_metaDir, SUB_DIR_META_DATA_IMPORT_SETTING);
		checkDirExists(_metaDataImportSettingDir);
		
		_metaDataUISettingDir = new File(_metaDir, SUB_DIR_META_DATA_UI_SETTING);
		checkDirExists(_metaDataUISettingDir);
	}
	
	private static void checkDirExists(File dir) {
		if(!dir.exists()) {
			dir.mkdirs();
			logger.info("Dir does not exist:" + dir.getAbsolutePath());
		}
	}

}
