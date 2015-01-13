package com.beef.dataorigin.web.context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import MetoXML.Base.XmlParseException;

import com.beef.dataorigin.context.DataOriginContext;
import com.beef.dataorigin.web.datacommittask.DODataModificationCommitTaskCallback;
import com.beef.dataorigin.web.datacommittask.DODataModificationCommitTaskScheduler;
import com.beef.dataorigin.web.upload.persistence.IDOUploadFilePersistence;
import com.salama.service.clouddata.CloudDataAppContext;
import com.salama.service.clouddata.core.AppContext;
import com.salama.service.clouddata.core.AppServiceContext;
import com.salama.service.core.context.CommonContext;
import com.salama.service.core.context.ServiceContext;

public abstract class DataOriginWebContext implements CommonContext {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5255769767510033085L;

	private final Logger logger = Logger.getLogger(DataOriginWebContext.class);

	protected final static String DEFAULT_DIR_DATA_ORIGIN_BASE = "WEB-INF/data-origin";
	public final static String DEFAULT_THUMBNAIL_DIR_VIRTUAL_PATH = "/uploaded_thumbnail";
	
	
	protected static DataOriginWebContextConfig _dataOriginWebContextConfig = null;
	
	protected static DataOriginContext _dataOriginContext = null;


	protected static IDOUploadFilePersistence _uploadFilePersistence = null;
	private static String _webContextPath;
	private static File _thumbnailPersistenceDir;

	private static DODataModificationCommitTaskScheduler _dataCommitTaskScheduler = null;
	
	
	public static String getWebContextPath() {
		return _webContextPath;
	}
	public static File getThumbnailPersistenceDir() {
		return _thumbnailPersistenceDir;
	}

	public static DataOriginContext getDataOriginContext() {
		return _dataOriginContext;
	}
	
	public static IDOUploadFilePersistence getUploadFilePersistence() {
		return _uploadFilePersistence;
	}
	public static DODataModificationCommitTaskScheduler getDataCommitTaskScheduler() {
		return _dataCommitTaskScheduler;
	}
	
	public static long getDefaultDataModificationCommitScheduleTime() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		
		long curTime = System.currentTimeMillis();
		long minAheadTime = 300*1000;
		if((cal.getTimeInMillis() - curTime) <= minAheadTime) {
			return ((long)(curTime / 1000)) * 1000 + minAheadTime;
		} else {
			return cal.getTimeInMillis();
		}
	}
	
	protected void setDODataModificationCommitTaskCallback(DODataModificationCommitTaskCallback taskCallback) {
		_dataCommitTaskScheduler.setTaskCallback(taskCallback);
	}

	protected void reload(ServletContext servletContext, String configLocation, AppContext appContext) {
		try {
			//WebContext config ---------------------------------------
			String configFilePath = servletContext.getRealPath(configLocation);
			XmlDeserializer xmlDes = new XmlDeserializer();
			_dataOriginWebContextConfig = (DataOriginWebContextConfig) xmlDes.Deserialize(
					configFilePath, DataOriginWebContextConfig.class, XmlDeserializer.DefaultCharset);
			logger.info("reload() configFilePath:" + configFilePath);
			
			//init file persistenc --------------------------------------
			_uploadFilePersistence = (IDOUploadFilePersistence) Class.forName(
					_dataOriginWebContextConfig.getUploadFilePersistenceClass()).newInstance();
			_uploadFilePersistence.init(servletContext);
			logger.info("reload() UploadFilePersistenceClass:" + _dataOriginWebContextConfig.getUploadFilePersistenceClass());
			
			//web context path
			_webContextPath = servletContext.getContextPath();

			//thumbnail persistence
			String thumbnailPersistenceDirPath = servletContext.getRealPath(DEFAULT_THUMBNAIL_DIR_VIRTUAL_PATH);
			_thumbnailPersistenceDir = new File(thumbnailPersistenceDirPath);
			if(!_thumbnailPersistenceDir.exists()) {
				_thumbnailPersistenceDir.mkdirs();
			}
			logger.info("reload() thumbnailPersistenceDirPath:" + thumbnailPersistenceDirPath);
			

			//base dir of DataOriginContext --------------------------------------
			String baseDirPath = _dataOriginWebContextConfig.getDataOriginBaseDir();
			if(baseDirPath == null || baseDirPath.length() == 0) {
				baseDirPath = servletContext.getRealPath(DEFAULT_DIR_DATA_ORIGIN_BASE);
			} else if(baseDirPath.startsWith("WEB-INF") || baseDirPath.startsWith("/WEB-INF")) {
				baseDirPath = servletContext.getRealPath(baseDirPath);
			} else {
				//_dataOriginWebContextConfig.getDataOriginBaseDir() is an absolute path
			}
			
			File baseDir = new File(baseDirPath);
			initDataOriginContext(baseDir, appContext);
			logger.info("reload() DataOriginContext inited:" + baseDirPath);
			
			//data commit task scheduler
			_dataCommitTaskScheduler = new DODataModificationCommitTaskScheduler();
			logger.info("reload() DataCommitTaskScheduler inited");
		} catch(Throwable e) {
			logger.error(null, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void destroy() {
		if(_uploadFilePersistence != null) {
			try {
				_uploadFilePersistence.destroy();
				_uploadFilePersistence = null;
			} catch(Throwable e) {
				logger.error(null, e);
			}
		}
		
		if(_dataCommitTaskScheduler != null) {
			try {
				_dataCommitTaskScheduler.stopSchedule();
				_dataCommitTaskScheduler = null;
			} catch(Throwable e) {
				logger.error(null, e);
			}
		}
		
		_dataOriginContext = null;
	}
	
	private static void initDataOriginContext(File baseDir, AppContext appContext) throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		//AppContext appContext = AppServiceContext.getAppContext();
		_dataOriginContext = new DataOriginContext(baseDir, (CloudDataAppContext) appContext);
	}
}
