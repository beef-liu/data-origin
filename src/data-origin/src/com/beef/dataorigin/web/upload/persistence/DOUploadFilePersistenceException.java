package com.beef.dataorigin.web.upload.persistence;

public class DOUploadFilePersistenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7222626175283735505L;

	public DOUploadFilePersistenceException() {
		super();
	}
	
	public DOUploadFilePersistenceException(String msg) {
		super(msg);
	}
	
	public DOUploadFilePersistenceException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public DOUploadFilePersistenceException(Throwable e) {
		super(e);
	}
	
}
