package com.beef.dataorigin.web.upload.persistence;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

/**
 * All the methods of file operations will be considered sessionless and thread safe. So there is only 1 instance for all thread to use. 
 * @author XingGu Liu
 *
 */
public interface IDOUploadFilePersistence {
	
	/**
	 * This will be invoked when WebContext is loaded.
	 * @throws DOUploadFilePersistenceException
	 */
	public void init(ServletContext servletContext);
	
	/**
	 * This will be invoked when WebContext is destroyed.
	 * @throws DOUploadFilePersistenceException
	 */
	public void destroy() throws DOUploadFilePersistenceException;
	
	/**
	 * 
	 * @param fileInput
	 * @param contentLength
	 * @param contentType
	 * @param fileName
	 * @return download url of the file
	 * @throws DOUploadFilePersistenceException
	 */
	public String putFile(
			InputStream fileInput,
			String fileId, long contentLength, String contentType		
			) throws DOUploadFilePersistenceException;
	
	//public DOUploadFilePersistenceMeta getFileMeta(String fileId) throws DOFilePersistenceException;
	
	public InputStream getFile(String fileId) throws DOUploadFilePersistenceException;
	
	public void deleteFile(String fileId) throws DOUploadFilePersistenceException;
	
}
