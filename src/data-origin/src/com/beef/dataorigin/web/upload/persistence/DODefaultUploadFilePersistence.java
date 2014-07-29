package com.beef.dataorigin.web.upload.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import com.beef.dataorigin.web.util.DODataDaoUtil;

public class DODefaultUploadFilePersistence implements IDOUploadFilePersistence {
	//public final static String DEFAULT_FILE_META_EXT = ".dofilemeta";
	
	protected final static String DEFAULT_FILE_PERSISTENCE_DIR_VIRTUAL_PATH = "/uploaded_file";
	
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
		
		/*
		String tempDirPath = servletContext.getRealPath(DEFAULT_FILE_TEMP_DIR_VIRTUAL_PATH);
		_tempDir = new File(tempDirPath);
		if(!_tempDir.exists()) {
			_tempDir.mkdirs();
		}
		*/
	}
	
	@Override
	public void destroy() throws DOUploadFilePersistenceException {
	}

	@Override
	public String putFile(InputStream fileInput, String fileId,
			long contentLength, String contentType)
			throws DOUploadFilePersistenceException {
		try {
			//save file
			writeFile(fileInput, fileId);

			String fileDownloadUrl = _webContextPath + DEFAULT_FILE_PERSISTENCE_DIR_VIRTUAL_PATH + "/" + fileId;
			
			return fileDownloadUrl;
		} catch (Exception e) {
			throw new DOUploadFilePersistenceException(e);
		}
	}
	
	/*
	@Override
	public DOUploadFileMeta putFile(InputStream fileInput, DOUploadFileMeta fileMeta)
			throws DOFilePersistenceException {
		
		try {
			//create meta
//			DOUploadFileMeta fileMeta = new DOUploadFileMeta();
//			fileMeta.setFileId(fileId);
//			fileMeta.setContentLength(contentLength);
//			fileMeta.setContentType(contentType);
//			fileMeta.setDownloadUrl(_webContextPath + DEFAULT_FILE_PERSISTENCE_DIR_VIRTUAL_PATH + "/" + fileId);
			
			//save meta
			writeMetaFile(fileMeta);
			
			//save file
			writeFile(fileInput, fileId);
			
			return fileMeta;
		} catch (Exception e) {
			throw new DOFilePersistenceException(e);
		}
	}
	*/
	
	/*
	@Override
	public DOUploadFileMeta getFileMeta(String fileId)
			throws DOFilePersistenceException {
		try {
			return readMetaFile(fileId);
		} catch (Exception e) {
			throw new DOFilePersistenceException(e);
		}
	}
	*/

	@Override
	public InputStream getFile(String fileId) throws DOUploadFilePersistenceException {
		try {
			File file = new File(_persistenceDir, fileId);
			return new FileInputStream(file);
		} catch (Exception e) {
			throw new DOUploadFilePersistenceException(e);
		}
	}

	@Override
	public void deleteFile(String fileId) throws DOUploadFilePersistenceException {
		File file = new File(_persistenceDir, fileId);
		file.delete();
		
		//deleteMetaFile(fileId);
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
	
	/*
	private void deleteMetaFile(String fileId) {
		File metaFile = new File(_persistenceDir, fileId + DEFAULT_FILE_META_EXT);
		metaFile.delete();
	}

	private DOUploadFileMeta readMetaFile(String fileId) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException, XmlParseException, InstantiationException, NoSuchMethodException {
		File metaFile = new File(_persistenceDir, fileId + DEFAULT_FILE_META_EXT);
		
		XmlDeserializer xmlDes = new XmlDeserializer();
		DOUploadFileMeta fileMeta = (DOUploadFileMeta) xmlDes.Deserialize(metaFile.getAbsolutePath(), DOUploadFileMeta.class, XmlDeserializer.DefaultCharset);
		
		return fileMeta;
	}
	
	private void writeMetaFile(DOUploadFileMeta fileMeta) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		File metaFile = new File(_persistenceDir, fileMeta.getFileId() + DEFAULT_FILE_META_EXT);
		
		XmlSerializer xmlSer = new XmlSerializer();
		xmlSer.Serialize(metaFile.getAbsolutePath(), fileMeta, DOUploadFileMeta.class, XmlDeserializer.DefaultCharset);
	}
	*/
	
}
