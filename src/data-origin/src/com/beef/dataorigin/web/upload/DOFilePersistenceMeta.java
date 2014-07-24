package com.beef.dataorigin.web.upload;

public class DOFilePersistenceMeta {
	private String _downloadUrl = null;
	
	private String _fileId = null;
	
	private long _contentLength = 0;
	private String _contentType = null;
	private String _contentHashCode = null;
	
	public String getDownloadUrl() {
		return _downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		_downloadUrl = downloadUrl;
	}
	public String getFileId() {
		return _fileId;
	}
	public void setFileId(String fileId) {
		_fileId = fileId;
	}
	public long getContentLength() {
		return _contentLength;
	}
	public void setContentLength(long contentLength) {
		_contentLength = contentLength;
	}
	public String getContentType() {
		return _contentType;
	}
	public void setContentType(String contentType) {
		_contentType = contentType;
	}
	public String getContentHashCode() {
		return _contentHashCode;
	}
	public void setContentHashCode(String contentHashCode) {
		_contentHashCode = contentHashCode;
	}
	
}
