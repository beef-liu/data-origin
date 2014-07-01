package com.beef.dataorigin.web.data;

import java.io.PrintWriter;
import java.io.StringWriter;


public class DODataServiceError {
	
	private String _errorMsg;
	
	private String _errorType;
	
	private String _errorStackTrace;

	
	public String getErrorMsg() {
		return _errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		_errorMsg = errorMsg;
	}

	public String getErrorType() {
		return _errorType;
	}

	public void setErrorType(String errorType) {
		_errorType = errorType;
	}

	public String getErrorStackTrace() {
		return _errorStackTrace;
	}

	public void setErrorStackTrace(String errorStackTrace) {
		_errorStackTrace = errorStackTrace;
	}

	public DODataServiceError(String errorMsg) {
		this(errorMsg, null);
	}

	public DODataServiceError(Throwable error) { 
		this(error.getMessage(), error);
	}
	
	public DODataServiceError(String errorMsg, Throwable error) {
		_errorMsg = errorMsg;
		
		if(error != null) {
			_errorType = error.getClass().getName();
			
			PrintWriter pw = null;
			try {
				StringWriter sw = new StringWriter();
				pw = new PrintWriter(sw);
				error.printStackTrace(pw);
				_errorStackTrace = sw.toString();
			} finally {
				pw.close();
			}
		}
	}
	
}
