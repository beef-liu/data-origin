package com.beef.dataorigin.context;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import MetoXML.Base.XmlParseException;
import MetoXML.Util.ClassFinder;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataImportSetting;
import com.beef.dataorigin.context.data.MMetaDataUISetting;
import com.beef.dataorigin.setting.DataOriginSetting;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.MetaDataUISetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.setting.msg.DOServiceMsg;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.reflect.PreScanClassFinder;

public class DataOriginContext implements ClassFinder {
	private final static Logger logger = Logger.getLogger(DataOriginContext.class);
	
	private PreScanClassFinder _dataOriginDataClassFinder;
	private ClassFinder _webDataClassFinder; 
	
	///////////// Dir Manager ////////////
	private DataOriginDirManager _dataOriginDirManager;
	
	///////////// Setting Files ///////////////
	/**
	 * data-origin/DataOriginSetting.xml
	 */
	private DataOriginSetting _dataOriginSetting;
	
	private List<String> _tableNameList;
	
	public List<String> getTableNameList() {
		return _tableNameList;
	}

	/**
	 * data-origin/dbtables/*.xml
	 * key: table name (lowercase) value:DBTable
	 */
	private Map<String, DBTable> _DBTableMap;
	
	/**
	 * data-origin/dbtables/*.xml
	 * key: table name (lowercase) value:MDBTable
	 */
	private Map<String, MDBTable> _mDBTableMap;

	/**
	 * data-origin/meta/data-import-setting/*.xml
	 * key: table name (lowercase) value:MetaDataImportSetting
	 */
	private Map<String, MetaDataImportSetting> _MetaDataImportSettingMap;
	
	private Map<String, MMetaDataImportSetting> _mMetaDataImportSettingMap;
	
	/**
	 * data-origin/meta/data-ui-setting/*.xml
	 * key:table name (lowercase) value:MMetaDataUISetting
	 */
	private Map<String, MetaDataUISetting> _MetaDataUISettingMap;

	/**
	 * data-origin/meta/data-ui-setting/*.xml
	 * key:table name (lowercase) value:MMetaDataUISetting
	 */
	private Map<String, MMetaDataUISetting> _mMetaDataUISettingMap;
	
	/**
	 * key:msgCode value: xml of DOServiceMsg
	 */
	private Map<String, String> _serviceMsgXmlMap;
	private Map<String, DOServiceMsg> _serviceMsgMap;
	
	public DataOriginSetting getDataOriginSetting() {
		return _dataOriginSetting;
	}
	
	public DBTable getDBTable(String tableName) {
		return _DBTableMap.get(tableName.toLowerCase());
	}
	
	public MDBTable getMDBTable(String tableName) {
		return _mDBTableMap.get(tableName.toLowerCase());
	}
	
	public MetaDataImportSetting getMetaDataImportSetting(String tableName) {
		return _MetaDataImportSettingMap.get(tableName.toLowerCase());
	}
	
	public MMetaDataImportSetting getMMetaDataImportSetting(String tableName) {
		return _mMetaDataImportSettingMap.get(tableName.toLowerCase());
	}
	
	public MetaDataUISetting getMetaDataUISetting(String tableName) {
		return _MetaDataUISettingMap.get(tableName.toLowerCase());
	}
	
	public MMetaDataUISetting getMMetaDataUISetting(String tableName) {
		return _mMetaDataUISettingMap.get(tableName.toLowerCase());
	}
	
	public String getServiceMsgXml(String msgCode) {
		return _serviceMsgXmlMap.get(msgCode);
	}
	
	public DOServiceMsg getServiceMsg(String msgCode) {
		return _serviceMsgMap.get(msgCode);
	}

	public DataOriginContext(File baseDir, ClassFinder dataClassFinder) throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		_dataOriginDirManager = new DataOriginDirManager(baseDir);

		//init ClassFinder
		initClassFinder(dataClassFinder);
		
		//DataOriginSetting.xml
		loadDataOriginSetting();
		
		//load xml files of dbTable
		loadDBTableSettings();
		
		//load xml files of data-import-setting/
		loadDataImportSetting();
		
		//load xml files of data-ui-setting/
		loadDataUISetting();
	}
	
	private void initClassFinder(ClassFinder dataClassFinder) {
		_dataOriginDataClassFinder = new PreScanClassFinder();
		_dataOriginDataClassFinder.loadClassOfPackage(DBColumn.class.getPackage().getName());
		_dataOriginDataClassFinder.loadClassOfPackage(DataOriginSetting.class.getPackage().getName());
		_dataOriginDataClassFinder.loadClassOfPackage(DOSearchCondition.class.getPackage().getName());
		
		_webDataClassFinder = dataClassFinder;
	}
	
	private void loadDataOriginSetting() throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		XmlDeserializer xmlDes = new XmlDeserializer();
		File file = new File(_dataOriginDirManager.getBaseDir(), DataOriginSetting.class.getSimpleName() + ".xml");
		_dataOriginSetting = (DataOriginSetting) xmlDes.Deserialize(file.getAbsolutePath(), DataOriginSetting.class, 
				XmlDeserializer.DefaultCharset, this);
		
		logger.info("Load " + DataOriginSetting.class.getSimpleName() + ".xml");
		
		//load into msg map
		_serviceMsgXmlMap = new HashMap<String, String>();
		_serviceMsgMap = new HashMap<String, DOServiceMsg>();
		List<DOServiceMsg> msgList = _dataOriginSetting.getServiceMsgList();
		if(msgList != null) {
			DOServiceMsg msg;
			String msgStr;
			logger.debug("Load ServiceMsg settings -----------------------");
			for(int i = 0; i < msgList.size(); i++) {
				msg = msgList.get(i);
				
				try {
					msgStr = XmlSerializer.objectToString(msg, DOServiceMsg.class);

					_serviceMsgMap.put(msg.getMsgCode(), msg);
					_serviceMsgXmlMap.put(msg.getMsgCode(), msgStr);

					logger.debug("Load ServiceMsg:" + msgStr);
				} catch (Throwable e1) {
					throw new RuntimeException(e1);
				}
			}
		}
		
	}
	
	private void loadDBTableSettings() throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		_tableNameList = new ArrayList<String>();
		_DBTableMap = new HashMap<String, DBTable>();
		_mDBTableMap = new HashMap<String, MDBTable>();
		File[] files = _dataOriginDirManager.getDbTablesDir().listFiles(_xmlFileFilter);
		
		if(files != null) {
			DBTable dbTable;
			XmlDeserializer xmlDes = new XmlDeserializer();
			
			for(int i = 0; i < files.length; i++) {
				dbTable = (DBTable) xmlDes.Deserialize(
						files[i].getAbsolutePath(), DBTable.class, 
						XmlDeserializer.DefaultCharset, this);
				
				_tableNameList.add(dbTable.getTableName().toLowerCase());
				_DBTableMap.put(dbTable.getTableName().toLowerCase(), dbTable);
				_mDBTableMap.put(dbTable.getTableName().toLowerCase(), convertDBTable(dbTable));
			}
		}
	}
	
	private void loadDataImportSetting() throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		_MetaDataImportSettingMap = new HashMap<String, MetaDataImportSetting>();
		_mMetaDataImportSettingMap = new HashMap<String, MMetaDataImportSetting>();
		File[] files = _dataOriginDirManager.getMetaDataImportSettingDir().listFiles(_xmlFileFilter);
		
		if(files != null) {
			MetaDataImportSetting dataSetting;
			XmlDeserializer xmlDes = new XmlDeserializer();
			
			for(int i = 0; i < files.length; i++) {
				dataSetting = (MetaDataImportSetting) xmlDes.Deserialize(
						files[i].getAbsolutePath(), MetaDataImportSetting.class, 
						XmlDeserializer.DefaultCharset, this);
				_MetaDataImportSettingMap.put(dataSetting.getDataTableName().toLowerCase(), dataSetting);
				_mMetaDataImportSettingMap.put(dataSetting.getDataTableName().toLowerCase(), convertMetaDataImportSetting(dataSetting));
			}
		}
	}

	private void loadDataUISetting() throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		_MetaDataUISettingMap = new HashMap<String, MetaDataUISetting>();
		_mMetaDataUISettingMap = new HashMap<String, MMetaDataUISetting>();
		File[] files = _dataOriginDirManager.getMetaDataUISettingDir().listFiles(_xmlFileFilter);
		
		if(files != null) {
			MetaDataUISetting dataSetting;
			XmlDeserializer xmlDes = new XmlDeserializer();
			
			for(int i = 0; i < files.length; i++) {
				dataSetting = (MetaDataUISetting) xmlDes.Deserialize(
						files[i].getAbsolutePath(), MetaDataUISetting.class, 
						XmlDeserializer.DefaultCharset, this);
				_MetaDataUISettingMap.put(dataSetting.getDataTableName().toLowerCase(), dataSetting);
				_mMetaDataUISettingMap.put(dataSetting.getDataTableName().toLowerCase(), convertMetaDataUISetting(dataSetting));
			}
		}
	}
	
	private FileFilter _xmlFileFilter = new FileFilter() {
		
		@Override
		public boolean accept(File file) {
			if(!file.isDirectory() && file.getName().toLowerCase().endsWith(".xml")) {
				return true;
			} else {
				return false;
			}
		}
	};
	
	private static MDBTable convertDBTable(DBTable dbTable) {
		MDBTable mDBTable = new MDBTable();
		
		mDBTable.setTableName(dbTable.getTableName());
		mDBTable.setComment(dbTable.getComment());
		
		for(int i = 0; i < dbTable.getUniqueIndex().size(); i++) {
			mDBTable.getUniqueIndexNameList().add(dbTable.getUniqueIndex().get(i).getName());
		}
		
		DBColumn dbCol;
		for(int i = 0; i < dbTable.getColumns().size(); i++) {
			dbCol = dbTable.getColumns().get(i);
			mDBTable.getColumnMap().put(dbCol.getName(), dbCol);
		}

		return mDBTable;
	}
	
	private static MMetaDataUISetting convertMetaDataUISetting(MetaDataUISetting dataSetting) {
		MMetaDataUISetting mDataSetting = new MMetaDataUISetting();
		
		mDataSetting.setDataClassName(dataSetting.getDataClassName());
		mDataSetting.setDataTableName(dataSetting.getDataTableName());
		mDataSetting.setSearchCondition(dataSetting.getSearchCondition());
		
		MetaDataField dataField;
		for(int i = 0; i < dataSetting.getFieldList().size(); i++) {
			dataField = dataSetting.getFieldList().get(i);
			mDataSetting.getFieldMap().put(dataField.getFieldName(), dataField);
		}
		
		return mDataSetting;
	}
	
	private static MMetaDataImportSetting convertMetaDataImportSetting(MetaDataImportSetting dataSetting) {
		MMetaDataImportSetting mDataSetting = new MMetaDataImportSetting();
		
		mDataSetting.setDataClassName(dataSetting.getDataClassName());
		mDataSetting.setDataTableName(dataSetting.getDataTableName());
		
		MetaDataField dataField;
		for(int i = 0; i < dataSetting.getFieldList().size(); i++) {
			dataField = dataSetting.getFieldList().get(i);
			mDataSetting.getFieldMap().put(dataField.getFieldName(), dataField);
		}
		
		return mDataSetting;
	}
	

	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
		Class<?> cls = null;
		
		try {
			cls = _dataOriginDataClassFinder.findClass(className);
			if(cls != null) {
				return cls;
			}
		} catch(Throwable e) {
		}
		
		try {
			cls = _webDataClassFinder.findClass(className);
			if(cls != null) {
				return cls;
			}
		} catch(Throwable e) {
		}
		
		logger.error("Class not found:" + className);
		return cls;
	}
	
}
