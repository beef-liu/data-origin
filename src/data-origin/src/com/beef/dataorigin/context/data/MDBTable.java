package com.beef.dataorigin.context.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.salama.modeldriven.util.db.DBColumn;

public class MDBTable {
    private String _tableName = "";

    private String _comment = "";

    private List<String> _uniqueIndexNameList = new ArrayList<String>();

    private Map<String, DBColumn> _columnMap = new HashMap<String, DBColumn>();
    
    private String[] _primaryKeys = null;

	public String getTableName() {
		return _tableName;
	}

	public void setTableName(String tableName) {
		_tableName = tableName;
	}

	public String getComment() {
		return _comment;
	}

	public void setComment(String comment) {
		_comment = comment;
	}

    /**
     * Column Name list
     */
	public List<String> getUniqueIndexNameList() {
		return _uniqueIndexNameList;
	}

    /**
     * key: colName  value:DBColumn
     */
	public Map<String, DBColumn> getColumnMap() {
		return _columnMap;
	}
    

	public String[] getPrimaryKeys() {
		return _primaryKeys;
	}

	public void setPrimaryKeys(String[] primaryKeys) {
		_primaryKeys = primaryKeys;
	}

}
