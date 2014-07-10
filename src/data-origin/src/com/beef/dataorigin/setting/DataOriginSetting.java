package com.beef.dataorigin.setting;

import java.util.List;

import com.beef.dataorigin.setting.msg.DOServiceMsg;

public class DataOriginSetting {
	private String _productionDBResourceName = "jdbc/dataorigin_db";
	private String _onEditingDBResourceName = "jdbc/dataorigin_db_editing";
	
	//private String _dbDataPackage = "com.beef.dataorigin.test.ws.data.db";
	
	private List<DOServiceMsg> _serviceMsgList = null;

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

	public List<DOServiceMsg> getServiceMsgList() {
		return _serviceMsgList;
	}

	public void setServiceMsgList(List<DOServiceMsg> serviceMsgList) {
		_serviceMsgList = serviceMsgList;
	}
	
}
