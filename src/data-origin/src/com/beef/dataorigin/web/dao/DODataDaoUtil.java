package com.beef.dataorigin.web.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class DODataDaoUtil {
	public final static String SEARCH_CONDITION_SCOPE_DELIM = "~";
	
	public final static String REGEX_DATE_YMD = "[0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,2}|[0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,2}";
	public final static String REGEX_DATE_YMDHMS = "[0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}|[0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,2}  [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}";
	
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
	
	public static boolean isFormatOfDateYmd(String val) {
		PatternMatcher matcher = new Perl5Matcher();
		return matcher.matches(val, regexDateYmd);
	}
	
	public static boolean isFormatOfDateYmdHms(String val) {
		PatternMatcher matcher = new Perl5Matcher();
		return matcher.matches(val, regexDateYmdHms);
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
}
