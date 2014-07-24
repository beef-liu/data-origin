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
import com.beef.dataorigin.web.upload.IDOFilePersistence;
import com.salama.service.clouddata.CloudDataAppContext;
import com.salama.service.clouddata.core.AppContext;
import com.salama.service.clouddata.core.AppServiceContext;
import com.salama.service.core.context.CommonContext;

public class DataOriginWebContext implements CommonContext {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5255769767510033085L;

	private final Logger logger = Logger.getLogger(DataOriginWebContext.class);

	private final static String DEFAULT_DIR_DATA_ORIGIN_BASE = "WEB-INF/data-origin";
	
	private static DataOriginWebContextConfig _dataOriginWebContextConfig = null;
	
	private static DataOriginContext _dataOriginContext = null;


	private static IDOFilePersistence _uploadFilePersistence = null;

	public static DataOriginContext getDataOriginContext() {
		return _dataOriginContext;
	}
	
	public static IDOFilePersistence getUploadFilePersistence() {
		return _uploadFilePersistence;
	}

	@Override
	public void reload(ServletContext servletContext, String configLocation) {
		try {
			//WebContext config
			String configFilePath = servletContext.getRealPath(configLocation);
			XmlDeserializer xmlDes = new XmlDeserializer();
			_dataOriginWebContextConfig = (DataOriginWebContextConfig) xmlDes.Deserialize(
					configFilePath, DataOriginWebContextConfig.class, XmlDeserializer.DefaultCharset);
			
			//init file persistenc
			_uploadFilePersistence = (IDOFilePersistence) Class.forName(
					_dataOriginWebContextConfig.getUploadFilePersistenceClass()).newInstance(); 

			//base dir of DataOriginContext 
			String baseDirPath = _dataOriginWebContextConfig.getDataOriginBaseDir();
			if(baseDirPath == null || baseDirPath.length() == 0) {
				baseDirPath = servletContext.getRealPath(DEFAULT_DIR_DATA_ORIGIN_BASE);
			} else if(baseDirPath.startsWith("WEB-INF") || baseDirPath.startsWith("/WEB-INF")) {
				baseDirPath = servletContext.getRealPath(baseDirPath);
			} else {
				//_dataOriginWebContextConfig.getDataOriginBaseDir() is an absolute path
			}
			
			File baseDir = new File(baseDirPath);
			initDataOriginContext(baseDir);
			
		} catch(Throwable e) {
			logger.error(null, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void destroy() {
		_dataOriginContext = null;
	}
	
	private static void initDataOriginContext(File baseDir) throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		AppContext appContext = AppServiceContext.getAppContext();
		_dataOriginContext = new DataOriginContext(baseDir, (CloudDataAppContext) appContext);
	}
}
