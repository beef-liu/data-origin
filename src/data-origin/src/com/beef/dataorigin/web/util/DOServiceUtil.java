package com.beef.dataorigin.web.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.util.HexUtil;
import com.salama.util.ResourceUtil;
import com.salama.util.db.DBUtil;

public class DOServiceUtil {
	private static final Logger logger = Logger.getLogger(DOServiceUtil.class);

	/**
	 * Max 127
	 */
	private static int ServerNum = 1;
	
	private static DataIdCreator _dataIdCreator = new DataIdCreator();
	
	public static int getServerNum() {
		return ServerNum;
	}

	static {
		Properties serverSettingProperties = null;
		try {
			serverSettingProperties = ResourceUtil.getProperties("/DefaultServiceUtil.properties");
			ServerNum = Integer.parseInt(serverSettingProperties.getProperty("server.num"));
		} catch(Throwable e) {
			logger.error(null, e);
		}
		
	}
	
	public static Connection getProductionDBConnection() {
		try {
			return DBUtil.getConnection("java:/comp/env/" + DataOriginWebContext.getDataOriginContext().getDataOriginSetting().getProductionDBResourceName());
		} catch (Exception e) {
			logger.error("Getting connection failed", e);
			return null;
		}
	}
	
	public static Connection getOnEditingDBConnection() {
		try {
			return DBUtil.getConnection("java:/comp/env/" + DataOriginWebContext.getDataOriginContext().getDataOriginSetting().getOnEditingDBResourceName());
		} catch (Exception e) {
			logger.error("Getting connection failed", e);
			return null;
		}
	}
	
	
	public static String newDataId() {
		return _dataIdCreator.newDataId();
	}
	
	private static class DataIdCreator {
		private final static int ID_SEQ_MIN = 0;
		private final static int ID_SEQ_MAX = 0x01000000;

		private int _DataIdSeq = ID_SEQ_MIN;
		private Object _LockForDataId = new Object();
		
		public String newDataId() {
			synchronized (_LockForDataId) {
				if(_DataIdSeq == ID_SEQ_MAX) {
					_DataIdSeq = ID_SEQ_MIN + 1;
				} else {
					_DataIdSeq ++;
				}
				
				int dataIdSeqAndServerNum = ServerNum | _DataIdSeq;
				
				return HexUtil.toHexString(System.currentTimeMillis()) + HexUtil.toHexString(dataIdSeqAndServerNum);
			}
		}
	}
}
