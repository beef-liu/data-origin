package com.beef.dataorigin.web.data;

public class DOAdmin {
	private String _admin_id = "";

	public String getAdmin_id() {
		return _admin_id;
	}

	public void setAdmin_id(String value) {
		_admin_id = value;
	}

	private String _password = ""; 
	
	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}
	
	private String _priviledge_roles = "";

	public String getPriviledge_roles() {
		return _priviledge_roles;
	}

	public void setPriviledge_roles(String value) {
		_priviledge_roles = value;
	}

	private String _name = "";

	public String getName() {
		return _name;
	}

	public void setName(String value) {
		_name = value;
	}

	private long _update_time = 0;

	public long getUpdate_time() {
		return _update_time;
	}

	public void setUpdate_time(long value) {
		_update_time = value;
	}
	
}
