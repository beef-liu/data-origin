package com.beef.dataorigin.web.upload;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import MetoXML.XmlSerializer;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DOUploadFileMeta;
import com.beef.dataorigin.web.service.DODataDetailService;
import com.beef.dataorigin.web.upload.persistence.DOUploadFilePersistenceException;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.modeldriven.util.db.mysql.MysqlTableInfoUtil;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;
import com.salama.service.core.net.http.HttpResponseWrapper;
import com.salama.service.core.net.http.MultipartFile;
import com.salama.service.core.net.http.MultipartRequestWrapper;
import com.salama.util.db.DBMetaUtil;
import com.salama.util.db.JDBCUtil;
import com.salama.util.easyimage.ImageUtil;

public class DOUploadFileService {
	private final static Logger logger = Logger.getLogger(DOUploadFileService.class);
	
	protected final static String TABLE_NAME_DO_UPLOAD_FILE_META = "DOUploadFileMeta";
	
	protected static int _thumbnailHeight = 128;
	
	protected String uploadFile(RequestWrapper request,
			String fileId, String fileTag) {
		try {
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
			createThumbnail(fileInput, fileId, fileMeta.getFile_ext());
			
			//insert or update file meta ------------------
			conn = DOServiceUtil.getOnEditingDBConnection();
			int updCnt = DODataDao.updateDataByPK(conn, TABLE_NAME_DO_UPLOAD_FILE_META, fileMeta);
			if(updCnt == 0) {
				DODataDao.insertData(conn, TABLE_NAME_DO_UPLOAD_FILE_META, fileMeta);
			}
			
			return fileMeta;
		} finally {
			try {
				conn.close();
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

	protected void createThumbnail(InputStream fileInput, String fileId, String fileExt) throws IOException {
		File thumbnailFile = getThumbnailFile(fileId, fileExt);
		boolean imgAllowEmptyRegion = false;
		
		String format;
		if(fileExt.equalsIgnoreCase(".jpg") || fileExt.equalsIgnoreCase(".jpeg")) {
			format = ImageUtil.IMAGE_FORMAT_JPEG;
		} else if(fileExt.equalsIgnoreCase(".gif")) {
			format = ImageUtil.IMAGE_FORMAT_GIF;
		} else {
			format = ImageUtil.IMAGE_FORMAT_PNG;
		}
		
		ImageUtil.createImage(fileInput, 0, _thumbnailHeight, thumbnailFile, imgAllowEmptyRegion, format);
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
	
	protected static boolean isImageFileExt(String fileExt) {
		if(fileExt.equalsIgnoreCase(".png") 
				|| fileExt.equalsIgnoreCase(".jpg")
				|| fileExt.equalsIgnoreCase(".jpeg")
				|| fileExt.equalsIgnoreCase(".gif")
				) {
			return true;
		} else {
			return false;
		}
	}
	
}
