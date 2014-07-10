package com.beef.dataorigin.setting.msg;

public class DOServiceMsg {
	public final static String MSG_LEVEL_INFO = "info";
	public final static String MSG_LEVEL_WARN = "warn";
	public final static String MSG_LEVEL_ERROR = "error";
	
	private String _msgCode;
	private String _msg;
	private String _msgLevel;
	
	public DOServiceMsg() {
	}
	
	public DOServiceMsg(String msgLevel, String msgCode, String msg) {
		_msgLevel = msgLevel;
		_msgCode = msgCode;
		_msg = msg;
	}
	
	/**
	 * e.g.info,warn,error
	 * @param msgLevel
	 */
	public String getMsgLevel() {
		return _msgLevel;
	}

	/**
	 * info
	 * warn
	 * error
	 */
	public void setMsgLevel(String msgLevel) {
		_msgLevel = msgLevel;
	}

	public String getMsgCode() {
		return _msgCode;
	}

	public void setMsgCode(String msgCode) {
		_msgCode = msgCode;
	}

	public String getMsg() {
		return _msg;
	}

	public void setMsg(String msg) {
		_msg = msg;
	}
	
}
