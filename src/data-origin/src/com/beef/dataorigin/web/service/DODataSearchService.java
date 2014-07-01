package com.beef.dataorigin.web.service;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;

import MetoXML.XmlDeserializer;
import MetoXML.XmlReader;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.data.DODataServiceError;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.salama.service.clouddata.CloudDataAppContext;
import com.salama.service.clouddata.CloudDataServiceContext;
import com.salama.service.clouddata.core.AppContext;
import com.salama.service.clouddata.core.AppServiceContext;
import com.salama.service.clouddata.core.annotation.ReturnValueConverter;

public class DODataSearchService <DataType> {
	private final static Logger logger = Logger.getLogger(DODataSearchService.class);
	
	public final static int MAX_PAGE_SIZE = 500;
	
	@ReturnValueConverter(valueFromRequestParam = "responseType", 
			jsonpReturnVariableNameFromRequestParam="jsonpReturn",
			skipObjectConvert = false)
	public String searchData(int beginIndex, int pageSize,
			String tableName,
			String searchConditionXml) {
		AppContext appContext = AppServiceContext.getAppContext();
		
		try {
			
			DOSearchCondition searchCondition = (DOSearchCondition) XmlDeserializer.stringToObject(
					searchConditionXml, DOSearchCondition.class, DataOriginWebContext.getDataOriginContext());
			
			return null;
		} catch(Throwable e) {
			logger.error(null, e);
			
			try {
				DODataServiceError error = new DODataServiceError(e);
				return appContext.objectToXml(error, DODataServiceError.class);
			} catch (Throwable e1) {
				throw new RuntimeException(e1);
			}
		}
	} 
}
