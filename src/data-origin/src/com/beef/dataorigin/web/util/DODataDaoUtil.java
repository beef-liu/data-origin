package com.beef.dataorigin.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class DODataDaoUtil {
	public final static String SEARCH_CONDITION_SCOPE_DELIM = "~";

	public final static String REGEX_DATE_YMD = "[0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,2}|[0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,2}";
	public final static String REGEX_DATE_YMDHMS = "[0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}|[0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}";
	
	public final static String FORMAT_DATE_YMD_MINUS = "yyyy-MM-dd";
	public final static String FORMAT_DATE_YMD_SLASH = "yyyy/MM/dd";
	public final static String FORMAT_DATE_HMS = "HH:mm:ss";
	public final static String FORMAT_DATE_YMD_HMS_MINUS = FORMAT_DATE_YMD_MINUS + " " + FORMAT_DATE_HMS;
	public final static String FORMAT_DATE_YMD_HMS_SLASH = FORMAT_DATE_YMD_SLASH + " " + FORMAT_DATE_HMS;
	
	private final static SimpleDateFormat dateFormatYmdMinus = new SimpleDateFormat(FORMAT_DATE_YMD_MINUS);
	private final static SimpleDateFormat dateFormatYmdSlash = new SimpleDateFormat(FORMAT_DATE_YMD_SLASH);
	private final static SimpleDateFormat dateFormatYmdHmsMinus = new SimpleDateFormat(FORMAT_DATE_YMD_HMS_MINUS);
	private final static SimpleDateFormat dateFormatYmdHmsSlash = new SimpleDateFormat(FORMAT_DATE_YMD_HMS_SLASH);

	private static Pattern regexDateYmd;
	private static Pattern regexDateYmdHms;
	
	static {
		try {
			PatternCompiler compiler = new Perl5Compiler();

			regexDateYmd = compiler.compile(REGEX_DATE_YMD);
			regexDateYmdHms = compiler.compile(REGEX_DATE_YMDHMS);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
		
	public static boolean isFormatOfPattern(Pattern regexPattern, String val) {
		PatternMatcher matcher = new Perl5Matcher();
		return matcher.matches(val, regexPattern);
	}
	
	public static boolean isFormatOfDateYmd(String val) {
		PatternMatcher matcher = new Perl5Matcher();
		return matcher.matches(val, regexDateYmd);
	}
	
	public static boolean isFormatOfDateYmdHms(String val) {
		PatternMatcher matcher = new Perl5Matcher();
		return matcher.matches(val, regexDateYmdHms);
	}
	
	public static String parseDateOrNumberToNumber(String val) throws ParseException {
		if(isFormatOfDateYmdHms(val)) {
			return Long.toString(parseDateYmdHmsToUTC(val));
		} else if(isFormatOfDateYmd(val)) {
			return Long.toString(parseDateYmdToUTC(val));
		} else {
			val = val.replace(",", "");
			return val;
		}
	}
	
	public static long parseDateYmdToUTC(String dtStr) throws ParseException {
		Date dt;
		if(dtStr.indexOf('-') > 0) {
			dt = dateFormatYmdMinus.parse(dtStr);
		} else {
			dt = dateFormatYmdSlash.parse(dtStr);
		}
		
		return dt.getTime();
	}

	public static long parseDateYmdHmsToUTC(String dtStr) throws ParseException {
		Date dt;
		if(dtStr.indexOf('-') > 0) {
			dt = dateFormatYmdHmsMinus.parse(dtStr);
		} else {
			dt = dateFormatYmdHmsSlash.parse(dtStr);
		}
		
		return dt.getTime();
	}

	public static String formatUTCToDate(String dateFormat, long utc) {
		if(dateFormat.startsWith(FORMAT_DATE_YMD_MINUS)) {
			if(dateFormat.length() == FORMAT_DATE_YMD_MINUS.length()) {
				return formatUTCToYmdMinus(utc);
			} else {
				return formatUTCToYmdHmsMinus(utc);
			}
		} else {
			if(dateFormat.length() == FORMAT_DATE_YMD_SLASH.length()) {
				return formatUTCToYmdSlash(utc);
			} else {
				return formatUTCToYmdHmsSlash(utc);
			}
		}
	}
	
	public static String formatUTCToYmdMinus(long utc) {
		Date dt = new Date(utc);
		
		return dateFormatYmdMinus.format(dt);
	}
	
	public static String formatUTCToYmdSlash(long utc) {
		Date dt = new Date(utc);
		
		return dateFormatYmdSlash.format(dt);
	}

	public static String formatUTCToYmdHmsMinus(long utc) {
		Date dt = new Date(utc);
		
		return dateFormatYmdHmsMinus.format(dt);
	}
	
	public static String formatUTCToYmdHmsSlash(long utc) {
		Date dt = new Date(utc);
		
		return dateFormatYmdHmsSlash.format(dt);
	}

	public static String[] splitByDelim(String val, String delim) {
		StringTokenizer stk = new StringTokenizer(val, delim);
		
		String[] vals = new String[stk.countTokens()];
		
		int i = 0;
		while(stk.hasMoreTokens()) {
			vals[i++] = stk.nextToken();
		}
		
		return vals;
	}
	
	public static String[] splitByScopeDelim(String val) {
		int indexOfDelim = val.indexOf(SEARCH_CONDITION_SCOPE_DELIM.charAt(0));
		
		if(indexOfDelim < 0) {
			String[] vals = new String[1];
			vals[0] = val;
			
			return vals;
		} else {
			String[] vals = new String[2];
			vals[0] = null;
			vals[1] = null;
			
			if(val.length() == 1) {
				return vals;
			} else {
				if(indexOfDelim == 0) {
					vals[1] = val.substring(indexOfDelim + 1);
					return vals;
				} else {
					vals[0] = val.substring(0, indexOfDelim);
					
					if(indexOfDelim < (val.length() - 1)) {
						vals[1] = val.substring(indexOfDelim + 1); 
					}
					
					return vals;
				}
			}
		}
	}
	
	public static long copy(InputStream input, OutputStream output) throws IOException {
		long totalRead = 0;
		int readCnt = 0;
		
		byte[] tempBuff = new byte[10240];
		
		while(true) {
			readCnt = input.read(tempBuff, 0, tempBuff.length);
			
			if(readCnt < 0) {
				break;
			}
			
			if(readCnt > 0) {
				output.write(tempBuff, 0, readCnt);
				output.flush();
				
				totalRead += readCnt;
			}
		}
		
		return totalRead;
	} 
	
}
