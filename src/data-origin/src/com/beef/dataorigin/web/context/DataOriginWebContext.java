package com.beef.dataorigin.web.context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import MetoXML.Base.XmlParseException;

import com.beef.dataorigin.context.DataOriginContext;
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

	private final static String DIR_DATA_ORIGIN_BASE = "WEB-INF/data-origin";
	
	private static DataOriginContext _dataOriginContext = null;
	
	
	public static DataOriginContext getDataOriginContext() {
		return _dataOriginContext;
	}

	@Override
	public void reload(ServletContext servletContext, String configLocation) {
		try {
			String baseDirPath = servletContext.getRealPath(DIR_DATA_ORIGIN_BASE);
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
