package com.beef.dataorigin.web.upload;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import MetoXML.Base.XmlParseException;

import com.beef.dataorigin.web.util.DODataDaoUtil;

public class DODefaultFilePersistence implements IDOFilePersistence {
	public final static String DEFAULT_FILE_PERSISTENCE_DIR_VIRTUAL_PATH = "/uploaded_file";
	public final static String DEFAULT_FILE_TEMP_DIR_VIRTUAL_PATH = "/WEB-INF/temp";
	public final static String DEFAULT_FILE_META_EXT = ".dofilemeta";
	
	private String _webContextPath;
	
	private File _persistenceDir;
	private File _tempDir;
	

	@Override
	public void init(ServletContext servletContext) {
		_webContextPath = servletContext.getContextPath();
		
		String persistenceDirPath = servletContext.getRealPath(DEFAULT_FILE_PERSISTENCE_DIR_VIRTUAL_PATH);
		_persistenceDir = new File(persistenceDirPath);
		if(!_persistenceDir.exists()) {
			_persistenceDir.mkdirs();
		}
		
		String tempDirPath = servletContext.getRealPath(DEFAULT_FILE_TEMP_DIR_VIRTUAL_PATH);
		_tempDir = new File(tempDirPath);
		if(!_tempDir.exists()) {
			_tempDir.mkdirs();
		}
	}
	
	@Override
	public void destroy() throws DOFilePersistenceException {
	}

	@Override
	public DOFilePersistenceMeta putFile(InputStream fileInput, String fileId,
			long contentLength, String contentType)
			throws DOFilePersistenceException {
		
		try {
			//create meta
			DOFilePersistenceMeta fileMeta = new DOFilePersistenceMeta();
			fileMeta.setFileId(fileId);
			fileMeta.setContentLength(contentLength);
			fileMeta.setContentType(contentType);
			fileMeta.setDownloadUrl(_webContextPath + DEFAULT_FILE_PERSISTENCE_DIR_VIRTUAL_PATH + "/" + fileId);
			
			//save meta
			writeMetaFile(fileMeta);
			
			//save file
			writeFile(fileInput, fileId);
			
			return fileMeta;
		} catch (Exception e) {
			throw new DOFilePersistenceException(e);
		}
	}
	
	@Override
	public DOFilePersistenceMeta getFileMeta(String fileId)
			throws DOFilePersistenceException {
		try {
			return readMetaFile(fileId);
		} catch (Exception e) {
			throw new DOFilePersistenceException(e);
		}
	}

	@Override
	public InputStream getFile(String fileId) throws DOFilePersistenceException {
		try {
			File file = new File(_persistenceDir, fileId);
			return new FileInputStream(file);
		} catch (Exception e) {
			throw new DOFilePersistenceException(e);
		}
	}

	@Override
	public void deleteFile(String fileId) throws DOFilePersistenceException {
		File file = new File(_persistenceDir, fileId);
		file.delete();
		
		deleteMetaFile(fileId);
	}

	private void writeFile(InputStream fileInput, String fileId) throws IOException {
		File file = new File(_persistenceDir, fileId);
		
		FileOutputStream fos = null;
		
		try {
			fos = new FileOutputStream(file);
			
			DODataDaoUtil.copy(fileInput, fos);
			
		} finally {
			fos.close();
		}
	}
	
	private void deleteMetaFile(String fileId) {
		File metaFile = new File(_persistenceDir, fileId + DEFAULT_FILE_META_EXT);
		metaFile.delete();
	}

	private DOFilePersistenceMeta readMetaFile(String fileId) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException, XmlParseException, InstantiationException, NoSuchMethodException {
		File metaFile = new File(_persistenceDir, fileId + DEFAULT_FILE_META_EXT);
		
		XmlDeserializer xmlDes = new XmlDeserializer();
		DOFilePersistenceMeta fileMeta = (DOFilePersistenceMeta) xmlDes.Deserialize(metaFile.getAbsolutePath(), DOFilePersistenceMeta.class, XmlDeserializer.DefaultCharset);
		
		return fileMeta;
	}
	
	private void writeMetaFile(DOFilePersistenceMeta fileMeta) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		File metaFile = new File(_persistenceDir, fileMeta.getFileId() + DEFAULT_FILE_META_EXT);
		
		XmlSerializer xmlSer = new XmlSerializer();
		xmlSer.Serialize(metaFile.getAbsolutePath(), fileMeta, DOFilePersistenceMeta.class, XmlDeserializer.DefaultCharset);
	}
	
}
