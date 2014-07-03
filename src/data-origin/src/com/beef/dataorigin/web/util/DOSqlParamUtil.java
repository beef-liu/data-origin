package com.beef.dataorigin.web.util;

public class DOSqlParamUtil {
	public final static String[] MYSQL_TYPES_NUMBER = new String[] {
		"bigint",
		"int",
		"mediumint",
		"smallint",
		"tinyint",
		"decimal",
		"double",
		"float",
	};
	
	private final static String[] SQL_KEY_WORDS = new String[]{
		"select",
		"from",
		"where",
		"left",
		"join",
		"order",
		"group",
		"by",
		"on",
		"having",
		"limit",
		"update",
		"insert",
		"delete",
		"truncate",
		"drop",
		"alter",
		"create"
	};
	
	public static boolean isNumberColType(String colType) {
		colType = colType.trim().toLowerCase();
		
		for(int i = 0; i < MYSQL_TYPES_NUMBER.length; i++) {
			if(colType.startsWith(MYSQL_TYPES_NUMBER[i])) {
				return true;
			}
		}
		
		return false;
	}

	public static String wrapNameInSql(String name) {
		return "`" + name + "`";
	}
	
	
	public static String verifyName(String name) {
		if(name == null || name.length() == 0) {
			return "";
		}
		
		char c;
		int len = name.length();
		String nm = name.trim().toLowerCase();
		for(int i = 0; i < len; i++) {
			c = nm.charAt(i);
			
			if(c >= 'a' && c <= 'z') {
				//OK
			} else if(c >= '0' && c <= '9' ) {
				//OK
			} else if(c == '-' && c == '_') {
				//OK
			} else {
				throw new RuntimeException("Invalid param name.(SQL injection?):" + name);
			}
		}
		
		if(isInKeyWord(nm)) {
			throw new RuntimeException("Invalid param name.(SQL injection?):" + name);
		}
		
		return name;
	}
	
	private static boolean isInKeyWord(String str) {
		for(int i = 0; i < SQL_KEY_WORDS.length; i++) {
			if(str.equals(SQL_KEY_WORDS[i])) {
				return true;
			}
		}
		
		return false;
	}
}
