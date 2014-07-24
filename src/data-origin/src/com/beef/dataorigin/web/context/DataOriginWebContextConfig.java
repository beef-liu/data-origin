package com.beef.dataorigin.web.context;

public class DataOriginWebContextConfig {
	
	private String _dataOriginBaseDir = "WEB-INF/data-origin";
	
	private String _uploadFilePersistenceClass = "com.beef.dataorigin.web.upload.DODefaultFilePersistence";
	
	public String getDataOriginBaseDir() {
		return _dataOriginBaseDir;
	}

	public void setDataOriginBaseDir(String dataOriginBaseDir) {
		_dataOriginBaseDir = dataOriginBaseDir;
	}

	/**
	 * Persistence class for upload file, it must implement com.beef.dataorigin.web.upload.IFilePersistence
	 * @return
	 */
	public String getUploadFilePersistenceClass() {
		return _uploadFilePersistenceClass;
	}

	public void setUploadFilePersistenceClass(String uploadFilePersistenceClass) {
		_uploadFilePersistenceClass = uploadFilePersistenceClass;
	}

}
