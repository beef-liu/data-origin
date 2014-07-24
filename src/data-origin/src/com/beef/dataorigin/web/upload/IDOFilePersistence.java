package com.beef.dataorigin.web.upload;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

/**
 * All the methods of file operations will be considered sessionless and thread safe. So there is only 1 instance for all thread to use. 
 * @author XingGu Liu
 *
 */
public interface IDOFilePersistence {
	
	/**
	 * This will be invoked when WebContext is loaded.
	 * @throws DOFilePersistenceException
	 */
	public void init(ServletContext servletContext);
	
	/**
	 * This will be invoked when WebContext is destroyed.
	 * @throws DOFilePersistenceException
	 */
	public void destroy() throws DOFilePersistenceException;
	
	/**
	 * 
	 * @param fileInput
	 * @param contentLength
	 * @param contentType
	 * @param fileName
	 * @return
	 * @throws DOFilePersistenceException
	 */
	public DOFilePersistenceMeta putFile(
			InputStream fileInput, String fileId,
			long contentLength, String contentType			
			) throws DOFilePersistenceException;
	
	public DOFilePersistenceMeta getFileMeta(String fileId) throws DOFilePersistenceException;
	
	public InputStream getFile(String fileId) throws DOFilePersistenceException;
	
	public void deleteFile(String fileId) throws DOFilePersistenceException;
	
}
