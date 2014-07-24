package com.beef.dataorigin.web.upload;

public class DOFilePersistenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7222626175283735505L;

	public DOFilePersistenceException() {
		super();
	}
	
	public DOFilePersistenceException(String msg) {
		super(msg);
	}
	
	public DOFilePersistenceException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public DOFilePersistenceException(Throwable e) {
		super(e);
	}
	
}
