package com.beef.dataorigin.web.data;

import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.salama.modeldriven.util.db.DBColumn;

public class DODataImportColMetaInfo {
	private DBColumn dbCol = null;
	private MetaDataField metaDataField = null;
	
	public DBColumn getDbCol() {
		return dbCol;
	}
	public void setDbCol(DBColumn dbCol) {
		this.dbCol = dbCol;
	}
	public MetaDataField getMetaDataField() {
		return metaDataField;
	}
	public void setMetaDataField(MetaDataField metaDataField) {
		this.metaDataField = metaDataField;
	}

}
