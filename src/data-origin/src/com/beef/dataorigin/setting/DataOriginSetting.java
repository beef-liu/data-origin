package com.beef.dataorigin.setting;

public class DataOriginSetting {
	private String _productionDBResourceName = "jdbc/dataorigin_db";
	private String _onEditingDBResourceName = "jdbc/dataorigin_db_editing";
	
	private String _dbDataPackage = "com.beef.dataorigin.test.ws.data.db";

	public String getProductionDBResourceName() {
		return _productionDBResourceName;
	}

	public void setProductionDBResourceName(String productionDBResourceName) {
		_productionDBResourceName = productionDBResourceName;
	}

	public String getOnEditingDBResourceName() {
		return _onEditingDBResourceName;
	}

	public void setOnEditingDBResourceName(String onEditingDBResourceName) {
		_onEditingDBResourceName = onEditingDBResourceName;
	}

	public String getDbDataPackage() {
		return _dbDataPackage;
	}

	public void setDbDataPackage(String dbDataPackage) {
		_dbDataPackage = dbDataPackage;
	}
	
	
}
