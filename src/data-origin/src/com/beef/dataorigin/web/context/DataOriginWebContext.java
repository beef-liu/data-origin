package com.beef.dataorigin.web.context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import MetoXML.Base.XmlParseException;

import com.beef.dataorigin.context.DataOriginContext;
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

	protected void reload(ServletContext servletContext, String configLocation, AppContext appContext) {
		try {
			//WebContext config ---------------------------------------
			String configFilePath = servletContext.getRealPath(configLocation);
			XmlDeserializer xmlDes = new XmlDeserializer();
			_dataOriginWebContextConfig = (DataOriginWebContextConfig) xmlDes.Deserialize(
					configFilePath, DataOriginWebContextConfig.class, XmlDeserializer.DefaultCharset);
			
			//init file persistenc --------------------------------------
			_uploadFilePersistence = (IDOUploadFilePersistence) Class.forName(
					_dataOriginWebContextConfig.getUploadFilePersistenceClass()).newInstance();
			_uploadFilePersistence.init(servletContext);
			
			_webContextPath = servletContext.getContextPath();
			
			String thumbnailPersistenceDirPath = servletContext.getRealPath(DEFAULT_THUMBNAIL_DIR_VIRTUAL_PATH);
			_thumbnailPersistenceDir = new File(thumbnailPersistenceDirPath);
			if(!_thumbnailPersistenceDir.exists()) {
				_thumbnailPersistenceDir.mkdirs();
			}
			

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
		
		_dataOriginContext = null;
	}
	
	private static void initDataOriginContext(File baseDir, AppContext appContext) throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		//AppContext appContext = AppServiceContext.getAppContext();
		_dataOriginContext = new DataOriginContext(baseDir, (CloudDataAppContext) appContext);
	}
}
