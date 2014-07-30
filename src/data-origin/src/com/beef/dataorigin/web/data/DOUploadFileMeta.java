package com.beef.dataorigin.web.data;

import com.beef.dataorigin.web.upload.persistence.DOUploadFilePersistenceMeta;

public class DOUploadFileMeta extends DOUploadFilePersistenceMeta {
	private String _file_ext = null;
	private String _content_hash_code = null;
	private String _file_tag = null;
	private long _update_time = 0;
	
	private String _thumbnail_download_url = null;
	
	public String getThumbnail_download_url() {
		return _thumbnail_download_url;
	}
	public void setThumbnail_download_url(String thumbnail_download_url) {
		_thumbnail_download_url = thumbnail_download_url;
	}
	public String getFile_ext() {
		return _file_ext;
	}
	public void setFile_ext(String file_ext) {
		_file_ext = file_ext;
	}
	public String getContent_hash_code() {
		return _content_hash_code;
	}
	public void setContent_hash_code(String content_hash_code) {
		_content_hash_code = content_hash_code;
	}
	public long getUpdate_time() {
		return _update_time;
	}
	public void setUpdate_time(long update_time) {
		_update_time = update_time;
	}
	public String getFile_tag() {
		return _file_tag;
	}
	public void setFile_tag(String file_tag) {
		_file_tag = file_tag;
	}
	
	
}
