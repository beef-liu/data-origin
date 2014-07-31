package com.beef.dataorigin.web.upload;

import java.awt.Point;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import MetoXML.XmlSerializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DOUploadFileMeta;
import com.beef.dataorigin.web.upload.persistence.DOUploadFilePersistenceException;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;
import com.salama.service.core.net.http.HttpResponseWrapper;
import com.salama.service.core.net.http.MultipartFile;
import com.salama.service.core.net.http.MultipartRequestWrapper;
import com.salama.util.easyimage.ImageUtil;

public class DOUploadFileService {
	private final static Logger logger = Logger.getLogger(DOUploadFileService.class);
	
	protected final static String TABLE_NAME_DO_UPLOAD_FILE_META = "DOUploadFileMeta";
	
	protected static int _thumbnailHeight = 128;
	
	protected String uploadFile(RequestWrapper request) {
		try {
			String fileId = request.getParameter("fileId");
			if(fileId == null || fileId.length() == 0) {
				fileId = DOServiceUtil.newDataId();
			}
			
			String fileTag = "";
			
			MultipartRequestWrapper multipartRequest = (MultipartRequestWrapper)request;
			MultipartFile multipartFile = getMultipartFileFromRequest(multipartRequest);
			
			DOUploadFileMeta fileMeta = uploadFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), 
					fileId, multipartFile.getSize(), multipartFile.getContentType(), fileTag);

			return XmlSerializer.objectToString(fileMeta, DOUploadFileMeta.class);
		} catch(Throwable e) {
			logger.error(null, e);
			
			return DOServiceMsgUtil.makeMsgXml(e);
		}
	}
	
	/**
	 * This method is for downloading private file. Use file_download_url if download public file.
	 * @param fileId
	 * @throws DOUploadFilePersistenceException
	 */
	protected void downloadFile(RequestWrapper request, ResponseWrapper response,
			String fileId) {
		InputStream fileInput = null;
		OutputStream output = null;

		try {
			//get file
			DOUploadFileMeta fileMeta = getFileMeta(fileId);
			fileInput = DataOriginWebContext.getUploadFilePersistence().getFile(fileId);
			
			//make file name for download
			String downloadFileName = makeDownloadFileName(fileMeta);
			
			//headers
			response.setContentLength((int)fileMeta.getContent_length());
			response.setContentType(fileMeta.getContent_type());
			response.addHeader(HttpResponseWrapper.HEADER_NAME_CONTENT_DISPOSITION, "attachment;" + "filename=" + downloadFileName);
			
			//output
			output = response.getOutputStream();
			DODataDaoUtil.copy(fileInput, output);
		} catch(Throwable e) {
			logger.error(null, e);
		} finally {
			try {
				fileInput.close();
			} catch(Throwable e) {
			}
			try {
				output.close();
			} catch(Throwable e) {
			}
		}
	}

	protected DOUploadFileMeta uploadFile(
			InputStream fileInput, String originalFileName,
			String fileId, long contentLength, String contentType, 
			String fileTag
			) throws DOUploadFilePersistenceException, ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
		Connection conn = null;
		boolean autoCommit = false; 
		try {
			DOUploadFileMeta fileMeta = new DOUploadFileMeta();
			fileMeta.setFile_id(fileId);
			fileMeta.setContent_length(contentLength);
			fileMeta.setContent_type(contentType);
			fileMeta.setFile_ext(getFileExt(originalFileName));
			fileMeta.setFile_tag(fileTag);
			fileMeta.setUpdate_time(System.currentTimeMillis());
			
			//save to persistence storage ------------------
			String downloadUrl = DataOriginWebContext.getUploadFilePersistence().putFile(
					fileInput, fileId, contentLength, contentType);
			fileMeta.setDownload_url(downloadUrl);
			
			//create thumbnail ------------------
			String imageFormat = getImageFormat(fileMeta.getFile_ext());
			if(imageFormat != null) {
				InputStream savedFileInput = null;
				try {
					
					savedFileInput = DataOriginWebContext.getUploadFilePersistence().getFile(fileId);
					Point imageSize = ImageUtil.getImageSize(savedFileInput);
					savedFileInput.close();
					
					
					savedFileInput = DataOriginWebContext.getUploadFilePersistence().getFile(fileId);
					createThumbnail(savedFileInput, fileId, fileMeta.getFile_ext(), imageFormat, imageSize.x, imageSize.y);
					
					fileMeta.setThumbnail_download_url(getThumbnailVirtualPath(fileMeta));
				} finally {
					try {
						savedFileInput.close();
					} catch(Throwable e) {
					}
				}
			}
			
			//insert or update file meta ------------------
			conn = DOServiceUtil.getOnEditingDBConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(true);
			
			int updCnt = updateDataForUpload(conn, fileMeta);
			if(updCnt == 0) {
				DODataDao.insertData(conn, TABLE_NAME_DO_UPLOAD_FILE_META, fileMeta);
			} else {
				fileMeta = (DOUploadFileMeta) DODataDao.searchDataByPK(conn, TABLE_NAME_DO_UPLOAD_FILE_META, fileMeta);
			}
			
			return fileMeta;
		} finally {
			try {
				conn.setAutoCommit(autoCommit);;
			} catch(Throwable e) {
			}
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
	
	private final static String SQL_UPDATE_BY_PK = "update DOUploadFileMeta set " 
			+ " content_type = ?, content_length = ?, " 
			+ " file_ext = ?, download_url = ?, thumbnail_download_url = ?, content_hash_code = ?, " 
			+ " update_time = ? where file_id = ?";
	protected static int updateDataForUpload(Connection conn, DOUploadFileMeta data) throws SQLException {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(SQL_UPDATE_BY_PK);
			
			int index = 1;
			stmt.setString(index++, data.getContent_type());
			stmt.setLong(index++, data.getContent_length());
			stmt.setString(index++, data.getFile_ext());
			stmt.setString(index++, data.getDownload_url());
			stmt.setString(index++, data.getThumbnail_download_url());
			stmt.setString(index++, data.getContent_hash_code());
			stmt.setLong(index++, data.getUpdate_time());
			stmt.setString(index++, data.getFile_id());
			
			return stmt.executeUpdate();
		} finally {
			try {
				stmt.close();
			} catch(Throwable e) {
			}
		}
	}

	protected MultipartFile getMultipartFileFromRequest(MultipartRequestWrapper request) {
		MultipartFile multipartFile = null;
		
		Iterator<MultipartFile> iteMultipartFile = request.getFiles().iterator();
		if(iteMultipartFile.hasNext()) {
			multipartFile = iteMultipartFile.next();
		}
		
		return multipartFile;
	}
	
	
	public static String getThumbnailVirtualPath(DOUploadFileMeta fileMeta) {
		return DataOriginWebContext.getWebContextPath() + DataOriginWebContext.DEFAULT_THUMBNAIL_DIR_VIRTUAL_PATH + "/" 
				+ fileMeta.getFile_id() + fileMeta.getFile_ext(); 
	}
	
	protected String makeDownloadFileName(DOUploadFileMeta fileMeta) {
		String fileExt = fileMeta.getFile_ext();
		if(fileExt == null) {
			fileExt = "";
		} else {
			if(fileExt.charAt(0) != '.') {
				fileExt = "." + fileExt;
			}
		}
		return fileMeta.getFile_id() + fileExt;
	}

	protected DOUploadFileMeta getFileMeta(String fileId) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Connection conn = null;
		try {
			conn = DOServiceUtil.getOnEditingDBConnection();

			DOUploadFileMeta fileMetaPK = new DOUploadFileMeta();
			fileMetaPK.setFile_id(fileId);
			DOUploadFileMeta fileMeta = (DOUploadFileMeta) DODataDao.searchDataByPK(
					conn, TABLE_NAME_DO_UPLOAD_FILE_META, fileMetaPK);
			
			return fileMeta;
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
			
	}

	protected void createThumbnail(InputStream fileInput, 
			String fileId, String fileExt, String imageFormat,
			int originalImageWidth, int originalImageHeight 
			) throws IOException {
		File thumbnailFile = getThumbnailFile(fileId, fileExt);
		boolean imgAllowEmptyRegion = false;
		
		ImageUtil.createImage(fileInput, originalImageWidth, originalImageHeight, 
				0, _thumbnailHeight, thumbnailFile, imgAllowEmptyRegion, imageFormat);
	}

	protected String getImageFormat(String fileExt) {
		String format = null;
		if(".png".equalsIgnoreCase(fileExt)) {
			format = ImageUtil.IMAGE_FORMAT_PNG;
		} else if(".jpg".equalsIgnoreCase(fileExt) || ".jpeg".equalsIgnoreCase(fileExt)) {
			format = ImageUtil.IMAGE_FORMAT_JPEG;
		} else if(".gif".equalsIgnoreCase(fileExt)) {
			format = ImageUtil.IMAGE_FORMAT_GIF;
		} else if(".bmp".equalsIgnoreCase(fileExt)) {
			format = ImageUtil.IMAGE_FORMAT_PNG;
		} else {
		}
		
		return format;
	}
	
	protected File getThumbnailFile(String fileId, String fileExt) {
		return new File(DataOriginWebContext.getThumbnailPersistenceDir(), fileId + fileExt);
	}
	
	protected static String getFileExt(String originalFileName) {
		int index = originalFileName.lastIndexOf('.');
		if(index <= 0) {
			return "";
		}
		
		return originalFileName.substring(index).toLowerCase();
	}
}
