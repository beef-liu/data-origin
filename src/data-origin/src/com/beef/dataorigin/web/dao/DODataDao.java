package com.beef.dataorigin.web.dao;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hslf.blip.Metafile;
import org.apache.poi.ss.formula.eval.StringValueEval;

import CollectionCommon.ITreeNode;
import MetoXML.AbstractReflectInfoCachedSerializer;
import MetoXML.XmlSerializer;

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
import com.salama.service.clouddata.util.dao.UpdateDataDao;
import com.salama.util.db.DBUtil;
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
	 * @throws IOException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IntrospectionException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static String searchDataXmlBySearchCondition(
			Connection conn,
			int beginIndex, int pageSize, 
			String tableName, DOSearchCondition searchCondition,
			String[] orderByFields) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException, InstantiationException {
		List dataList = searchDataListBySearchCondition(conn, beginIndex, pageSize, tableName, searchCondition, orderByFields);
		return XmlSerializer.objectToString(dataList, ArrayList.class);
	}
	
	public static List searchDataListBySearchCondition(
			Connection conn,
			int beginIndex, int pageSize, 
			String tableName, DOSearchCondition searchCondition,
			String[] orderByFields) throws ParseException, SQLException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
		StringBuilder sql = new StringBuilder();
		
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
		MMetaDataUISetting mMetaDataUISetting = DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(tableName);

		sql.append("select * from ").append(DOSqlParamUtil.quoteSqlIdentifier(mDBTable.getTableName()));
		
		//where conditions -------------------------------------------------------------
		List<Object> colValueListForStmt = new ArrayList<Object>();
		StringBuilder sqlWhereConditions = new StringBuilder();
		DOSearchConditionItem searchConditionItem;
		for(int i = 0; i < searchCondition.getSearchConditionItemList().size(); i++) {
			searchConditionItem = searchCondition.getSearchConditionItemList().get(i);
			
			addSearchConditionItem(mMetaDataUISetting.getFieldMap(), mDBTable, colValueListForStmt, sqlWhereConditions, searchConditionItem);
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
				sql.append(DOSqlParamUtil.quoteSqlIdentifier(orderByFields[i]));
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
			
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(mMetaDataUISetting.getDataClassName());
			return QueryDataDao.findData(stmt, dataClass);
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

		sql.append("select count(1) from ").append(DOSqlParamUtil.quoteSqlIdentifier(mDBTable.getTableName()));
		
		//where conditions -------------------------------------------------------------
		List<Object> colValueListForStmt = new ArrayList<Object>();
		StringBuilder sqlWhereConditions = new StringBuilder();
		DOSearchConditionItem searchConditionItem;
		for(int i = 0; i < searchCondition.getSearchConditionItemList().size(); i++) {
			searchConditionItem = searchCondition.getSearchConditionItemList().get(i);
			
			addSearchConditionItem(mMetaDataUISetting.getFieldMap(), mDBTable, colValueListForStmt, sqlWhereConditions, searchConditionItem);
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
	
	public static boolean addSearchConditionItem(
			Map<String, MetaDataField> metaDataFieldMap,
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
		

		dataField = metaDataFieldMap.get(
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
				sqlWhereConditions.append(DOSqlParamUtil.quoteSqlIdentifier(dataField.getFieldName())).append(" = ? ");
				colValueListForStmt.add(DODataDaoUtil.parseDateOrNumberToNumber(fieldVals[0]));
			} else {
				int valsAppendCnt = 0;
				if(fieldVals[0] != null) {
					sqlWhereConditions.append(DOSqlParamUtil.quoteSqlIdentifier(dataField.getFieldName())).append(" >= ? ");
					colValueListForStmt.add(DODataDaoUtil.parseDateOrNumberToNumber(fieldVals[0]));
					valsAppendCnt ++;
				}
				if(fieldVals[1] != null) {
					if(valsAppendCnt > 0) {
						sqlWhereConditions.append(" and ");
					}
					sqlWhereConditions.append(DOSqlParamUtil.quoteSqlIdentifier(dataField.getFieldName())).append(" <= ? ");
					colValueListForStmt.add(DODataDaoUtil.parseDateOrNumberToNumber(fieldVals[1]));
					valsAppendCnt ++;
				}
			}
		} else {
			sqlWhereConditions.append(DOSqlParamUtil.quoteSqlIdentifier(dataField.getFieldName())).append(" like concat(concat('%', ?), '%') ");
			colValueListForStmt.add(fieldVal);
		}
		
		sqlWhereConditions.append(" ) ");
		
		return true;
	}
	
	public static Object searchDataByPK(
			Connection conn,
			String tableName, Object data
			) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ").append(DOSqlParamUtil.quoteSqlIdentifier(DOSqlParamUtil.verifyName(dbTable.getTableName())));
	

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
			
			sql.append(DOSqlParamUtil.quoteSqlIdentifier(dbCol.getName())).append(" = ? ");
			
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

	public static int insertData(
			Connection conn,
			String tableName, Object data
			) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
		return UpdateDataDao.insertData(conn, dbTable.getTableName(), data);
	}
	
	public static int updateDataByPK(
			Connection conn,
			String tableName, Object data
			) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
		return UpdateDataDao.updateData(conn, mDBTable.getTableName(), data, mDBTable.getPrimaryKeys());
	}

	public static int deleteDataByPK(
			Connection conn,
			String tableName, Object data
			) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
		return UpdateDataDao.deleteData(conn, mDBTable.getTableName(), data, mDBTable.getPrimaryKeys());
	}

	public static int deleteDataByPKList(
			Connection conn,
			String tableName, List dataList
			) throws ParseException, SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(tableName);
		int updCnt = 0;
		for(int i = 0; i < dataList.size(); i++) {
			updCnt += UpdateDataDao.deleteData(conn, mDBTable.getTableName(), dataList.get(i), mDBTable.getPrimaryKeys());
		}
		
		return updCnt;
	}
	
	public static String makeSqlConditionOfPrimaryKey(
			Connection conn,
			String tableName, Object data
			) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		DBTable dbTable = DataOriginWebContext.getDataOriginContext().getDBTable(tableName);
		
		StringBuilder sql = new StringBuilder();

		//conditions of primary keys ----------------------------
		DBColumn dbCol;
		PropertyDescriptor propDesc;
		Object propVal;
		for(int i = 0; i < dbTable.getColumns().size(); i++) {
			dbCol = dbTable.getColumns().get(i);
			
			if(!dbCol.isPrimaryKey()) {
				continue;
			}
			
			propDesc = findPropertyDescriptor(dbCol.getName(), data.getClass());
			propVal = propDesc.getReadMethod().invoke(data, (Object[])null);
			
			appendSqlConditionItem(sql, dbCol, propVal);
		}
		
		return sql.toString();
	}
	
	public static void appendSqlConditionItem(StringBuilder sqlCondition, DBColumn dbCol, Object colVal) {
		
		if(sqlCondition.length() > 0) {
			sqlCondition.append(" and ");
		}
		
		sqlCondition.append(DOSqlParamUtil.quoteSqlIdentifier(dbCol.getName())).append(" = ");
		
		String dbColType = dbCol.getColumnType().toLowerCase();
		if(colVal == null) {
			if(dbColType.startsWith("int")
				|| dbColType.startsWith("bigint")
				|| dbColType.startsWith("binary")
				|| dbColType.startsWith("double")
				|| dbColType.startsWith("float")
				|| dbColType.startsWith("mediumint")
				|| dbColType.startsWith("smallint")
				|| dbColType.startsWith("tinyint")
			) {
				sqlCondition.append("0");
			} else {
				sqlCondition.append("''");
			}
		} else {
			if(dbColType.startsWith("int")
					|| dbColType.startsWith("bigint")
					|| dbColType.startsWith("binary")
					|| dbColType.startsWith("double")
					|| dbColType.startsWith("float")
					|| dbColType.startsWith("mediumint")
					|| dbColType.startsWith("smallint")
					|| dbColType.startsWith("tinyint")
				) {
				String colValStr = String.valueOf(colVal);
				
				//assure it is a number
				if(colValStr.indexOf('.') >= 0) {
					Double.parseDouble(colValStr);
				} else {
					Long.parseLong(colValStr);
				}
				sqlCondition.append(colValStr);
			} else {
				sqlCondition.append("'").append(DBUtil.replaceQuote(String.valueOf(colVal))).append("'");
			}
		}
	}
	
	@Override
	protected void BackwardToNode(ITreeNode arg0, int arg1) {
	}

	@Override
	protected void ForwardToNode(ITreeNode arg0, int arg1, boolean arg2) {
	}
	
}
