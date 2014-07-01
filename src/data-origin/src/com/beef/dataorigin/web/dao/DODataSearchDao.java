package com.beef.dataorigin.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataUISetting;
import com.beef.dataorigin.setting.meta.MetaDataUISetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.data.DOSearchConditionItem;
import com.beef.dataorigin.web.util.DOSqlParamUtil;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.service.clouddata.util.SqlParamValidator;

public class DODataSearchDao {
	
	/**
	 * 
	 * @param beginIndex
	 * @param pageSize
	 * @param searchCondition
	 * @return xml of data list
	 */
	public String searchDataXml(
			Connection conn,
			int beginIndex, int pageSize, 
			String tableName, DOSearchCondition searchCondition) {
		PreparedStatement stmt = null;
		
		try {
			//stmt = conn.prepareStatement(arg0)
		} finally {
			
		}
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("select * from ").append(DOSqlParamUtil.verifyName(tableName));
	
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
		MMetaDataUISetting mMetaDataUISetting = DataOriginWebContext.getDataOriginContext().getMetaDataUISettingMap().get(tableName);

		//where conditions -------------------------------------------------------------
		StringBuilder sqlWhereConditions = new StringBuilder();
		DOSearchConditionItem searchConditionItem;
		for(int i = 0; i < searchCondition.getSearchConditionItemList().size(); i++) {
			searchConditionItem = searchCondition.getSearchConditionItemList().get(i);
			
		}
		
		return null;
	}
	
	protected static boolean addSearchConditionItem(
			MMetaDataUISetting mMetaDataUISetting,
			MDBTable mDBTable,
			List<String> colValueList,
			StringBuilder sqlWhereConditions, 
			DOSearchConditionItem searchConditionItem) {
		MetaDataField dataField;
		DBColumn dbColumn;
		String colType;
		
		if(searchConditionItem.getFieldValue() == null || searchConditionItem.getFieldValue().length() == 0) {
			return false;
		}
		
		if(sqlWhereConditions.length() != 0) {
			sqlWhereConditions.append(" and ");
		}
		
		sqlWhereConditions.append(" ( ");
		
		dataField = mMetaDataUISetting.getFieldMap().get(
				DOSqlParamUtil.verifyName(searchConditionItem.getFieldName())); 
		dbColumn = mDBTable.getColumnMap().get(searchConditionItem.getFieldName());
		colType = dbColumn.getColumnType().trim().toLowerCase();
		
		sqlWhereConditions.append(dataField.getFieldName());
		if(dataField.getFieldDispFormat() != null && dataField.getFieldDispFormat().length() > 0) {
			
		}
		if(DOSqlParamUtil.isNumberColType(colType)) {
		} else {
		}
		
		sqlWhereConditions.append(" ) ");
		
		return true;
	}
}
