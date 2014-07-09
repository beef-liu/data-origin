package com.beef.dataorigin.web.dao;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import CollectionCommon.ITreeNode;
import MetoXML.AbstractReflectInfoCachedSerializer;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataUISetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.data.DOSearchCondition;
import com.beef.dataorigin.web.data.DOSearchConditionItem;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOSqlParamUtil;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.service.clouddata.util.dao.QueryDataDao;
import com.salama.util.db.JDBCUtil;

public class DODataDao extends AbstractReflectInfoCachedSerializer {
	private final static Logger logger = Logger.getLogger(DODataDao.class);
	
	/**
	 * 
	 * @param beginIndex
	 * @param pageSize
	 * @param searchCondition
	 * @param orderByFields e.g.:{"name", "amount"}
	 * @return xml of data list
	 * @throws ParseException 
	 * @throws SQLException 
	 */
	public static String searchDataXmlBySearchCondition(
			Connection conn,
			int beginIndex, int pageSize, 
			String tableName, DOSearchCondition searchCondition,
			String[] orderByFields) throws ParseException, SQLException {
		StringBuilder sql = new StringBuilder();
		
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
		MMetaDataUISetting mMetaDataUISetting = DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(tableName);

		sql.append("select * from ").append(DOSqlParamUtil.wrapNameInSql(mDBTable.getTableName()));
		
		//where conditions -------------------------------------------------------------
		List<Object> colValueListForStmt = new ArrayList<Object>();
		StringBuilder sqlWhereConditions = new StringBuilder();
		DOSearchConditionItem searchConditionItem;
		for(int i = 0; i < searchCondition.getSearchConditionItemList().size(); i++) {
			searchConditionItem = searchCondition.getSearchConditionItemList().get(i);
			
			addSearchConditionItem(mMetaDataUISetting, mDBTable, colValueListForStmt, sqlWhereConditions, searchConditionItem);
		}
		
		if(sqlWhereConditions.length() > 0) {
			sql.append(" where ");
			sql.append(sqlWhereConditions);
		}
		
		//order by -------------------------------------------------------------
		if(orderByFields != null && orderByFields.length > 0) {
			sql.append(" order by ");
			for(int i = 0; i < orderByFields.length; i++) {
				if(i > 0) {
					sql.append(",");
				}
				sql.append(DOSqlParamUtil.wrapNameInSql(orderByFields[i]));
			}
		}

		//limit -------------------------------------------------------------
		sql.append(" limit ").append(beginIndex).append(",").append(pageSize);
		
		logger.debug("sql:" + sql.toString());
		
		//execute statement -------------------------------------------------------------
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(sql.toString());
			
			int index = 1;
			
			for(int i = 0; i < colValueListForStmt.size(); i++) {
				stmt.setObject(index++, colValueListForStmt.get(i));
			}
			
			return QueryDataDao.findDataXml(stmt, "List", mMetaDataUISetting.getDataClassName());
		} finally {
			stmt.close();
		}
	}

	public static int searchDataCountBySearchCondition(
			Connection conn,
			String tableName, DOSearchCondition searchCondition) throws ParseException, SQLException {
		StringBuilder sql = new StringBuilder();
		
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
		MMetaDataUISetting mMetaDataUISetting = DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(tableName);

		sql.append("select count(1) from ").append(DOSqlParamUtil.wrapNameInSql(mDBTable.getTableName()));
		
		//where conditions -------------------------------------------------------------
		List<Object> colValueListForStmt = new ArrayList<Object>();
		StringBuilder sqlWhereConditions = new StringBuilder();
		DOSearchConditionItem searchConditionItem;
		for(int i = 0; i < searchCondition.getSearchConditionItemList().size(); i++) {
			searchConditionItem = searchCondition.getSearchConditionItemList().get(i);
			
			addSearchConditionItem(mMetaDataUISetting, mDBTable, colValueListForStmt, sqlWhereConditions, searchConditionItem);
		}
		
		if(sqlWhereConditions.length() > 0) {
			sql.append(" where ");
			sql.append(sqlWhereConditions);
		}
		
		logger.debug("sql:" + sql.toString());
		
		//execute statement -------------------------------------------------------------
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql.toString());
			
			int index = 1;
			
			for(int i = 0; i < colValueListForStmt.size(); i++) {
				stmt.setObject(index++, colValueListForStmt.get(i));
			}
			
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			} else {
				return 0;
			}
		} finally {
			stmt.close();
		}
	}
	
	protected static boolean addSearchConditionItem(
			MMetaDataUISetting mMetaDataUISetting,
			MDBTable mDBTable,
			List<Object> colValueListForStmt,
			StringBuilder sqlWhereConditions, 
			DOSearchConditionItem searchConditionItem) throws ParseException {
		MetaDataField dataField;
		DBColumn dbColumn;
		String colType;
		
		if(searchConditionItem.getFieldValue() == null 
				|| searchConditionItem.getFieldValue().length() == 0
				) {
			return false;
		}
		

		dataField = mMetaDataUISetting.getFieldMap().get(
				DOSqlParamUtil.verifyName(searchConditionItem.getFieldName())); 
		dbColumn = mDBTable.getColumnMap().get(searchConditionItem.getFieldName());
		colType = dbColumn.getColumnType().trim().toLowerCase();

		// XXX~XXX
		String fieldVal = searchConditionItem.getFieldValue().trim();
		if(fieldVal.equals(DODataDaoUtil.SEARCH_CONDITION_SCOPE_DELIM) 
				&& DOSqlParamUtil.isNumberColType(colType)) {
			return false;
		}
		String[] fieldVals = DODataDaoUtil.splitByScopeDelim(fieldVal);
		
		//append "and" when not 1st time
		if(sqlWhereConditions.length() != 0) {
			sqlWhereConditions.append(" and ");
		}
		sqlWhereConditions.append(" ( ");
		//append field value
		if(DOSqlParamUtil.isNumberColType(colType)) {
			if(fieldVals.length == 1) {
				sqlWhereConditions.append(DOSqlParamUtil.wrapNameInSql(dataField.getFieldName())).append(" = ? ");
				colValueListForStmt.add(DODataDaoUtil.parseDateOrNumberToNumber(fieldVals[0]));
			} else {
				int valsAppendCnt = 0;
				if(fieldVals[0] != null) {
					sqlWhereConditions.append(DOSqlParamUtil.wrapNameInSql(dataField.getFieldName())).append(" >= ? ");
					colValueListForStmt.add(DODataDaoUtil.parseDateOrNumberToNumber(fieldVals[0]));
					valsAppendCnt ++;
				}
				if(fieldVals[1] != null) {
					if(valsAppendCnt > 0) {
						sqlWhereConditions.append(" and ");
					}
					sqlWhereConditions.append(DOSqlParamUtil.wrapNameInSql(dataField.getFieldName())).append(" <= ? ");
					colValueListForStmt.add(DODataDaoUtil.parseDateOrNumberToNumber(fieldVals[1]));
					valsAppendCnt ++;
				}
			}
		} else {
			sqlWhereConditions.append(DOSqlParamUtil.wrapNameInSql(dataField.getFieldName())).append(" like concat(concat('%', ?), '%') ");
			colValueListForStmt.add(fieldVal);
		}
		
		sqlWhereConditions.append(" ) ");
		
		return true;
	}
	
	public static Object searchDataByPK(
			Connection conn,
			String tableName, Object data
			) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		StringBuilder sql = new StringBuilder();
		
		sql.append("select * from ").append(DOSqlParamUtil.wrapNameInSql(DOSqlParamUtil.verifyName(tableName)));
	
		DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);

		//where conditions -------------------------------------------------------------
		sql.append(" where ");
		List<Object> colValueListForStmt = new ArrayList<Object>();
		DBColumn dbCol;
		PropertyDescriptor propDesc;
		Object propVal;
		for(int i = 0; i < dbTable.getColumns().size(); i++) {
			dbCol = dbTable.getColumns().get(i);
			
			if(!dbCol.isPrimaryKey()) {
				continue;
			}
			
			if(colValueListForStmt.size() > 0) {
				sql.append(" and ");
			}
			
			sql.append(DOSqlParamUtil.wrapNameInSql(dbCol.getName())).append(" = ? ");
			
			propDesc = findPropertyDescriptor(dbCol.getName(), data.getClass());
			propVal = propDesc.getReadMethod().invoke(data, (Object[])null);
			colValueListForStmt.add(propVal);
		}

		logger.debug("sql:" + sql.toString());

		
		//statement
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement(sql.toString());
			
			int index = 1;
			
			for(int i = 0; i < colValueListForStmt.size(); i++) {
				stmt.setObject(index++, colValueListForStmt.get(i));
			}

			rs = stmt.executeQuery();
			
			if(rs.next()) {
				return JDBCUtil.ResultSetToData(rs, data.getClass(), true);
			} else {
				return null;
			}
		} finally {
			stmt.close();
		}
	}

	@Override
	protected void BackwardToNode(ITreeNode arg0, int arg1) {
	}

	@Override
	protected void ForwardToNode(ITreeNode arg0, int arg1, boolean arg2) {
	}
	
}
