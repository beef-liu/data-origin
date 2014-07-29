package com.beef.dataorigin.web.upload.persistence;

public class DOUploadFilePersistenceMeta {
	private String _file_id = null;
	private String _content_type = null;
	private long _content_length = 0;
	private String _download_url = null;

	public String getFile_id() {
		return _file_id;
	}
	public void setFile_id(String file_id) {
		_file_id = file_id;
	}
	public String getContent_type() {
		return _content_type;
	}
	public void setContent_type(String content_type) {
		_content_type = content_type;
	}
	public long getContent_length() {
		return _content_length;
	}
	public void setContent_length(long content_length) {
		_content_length = content_length;
	}
	public String getDownload_url() {
		return _download_url;
	}
	public void setDownload_url(String download_url) {
		_download_url = download_url;
	}
	
}
