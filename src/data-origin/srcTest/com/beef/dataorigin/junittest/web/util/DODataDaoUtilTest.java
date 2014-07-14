package com.beef.dataorigin.junittest.web.util;

import static org.junit.Assert.*;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.junit.Test;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.setting.DataOriginSetting;
import com.beef.dataorigin.web.util.DODataDaoUtil;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;

public class DODataDaoUtilTest {

	@Test
	public void test() {
		try {
			DataOriginSetting setting = new DataOriginSetting();
			setting.setServiceMsgList(DOServiceMsgUtil.createDefaultMsgList());
			
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(DataOriginSetting.class.getSimpleName() + ".xml", setting, DataOriginSetting.class, XmlDeserializer.DefaultCharset);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRegex1() {
		try {
			String regex = ".{0,3}";
			PatternCompiler compiler = new Perl5Compiler();

			testRegex(compiler, regex, "");
			testRegex(compiler, regex, "a");
			testRegex(compiler, regex, "abc");
			testRegex(compiler, regex, "abcd");
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRegex2() {
		try {
			String regex = "[a-z]{0,3}";
			PatternCompiler compiler = new Perl5Compiler();

			testRegex(compiler, regex, "");
			testRegex(compiler, regex, "a");
			testRegex(compiler, regex, "abc");
			testRegex(compiler, regex, "abcd");
			testRegex(compiler, regex, "测");
			testRegex(compiler, regex, "测试");
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRegex3() {
		try {
			//String regex = "[a-zA-Z0-9 \\\\_\\-\\!\\~\\`\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\+\\=\\|\\[\\]'\"\\:\\;\\,\\.\\/\\<\\>\\?]{0,30}";
			String regex = "[a-zA-Z0-9 \\\\_\\-\\!\\~`\\@\\#\\$%\\^&\\*\\(\\)\\{\\}\\+\\=\\|\\[\\]'\":;\\,\\.\\/\\<\\>\\?]{0,50}";
			//String regex = "[a-zA-Z0-9 '\"]{0,30}";
			PatternCompiler compiler = new Perl5Compiler();

			testRegex(compiler, regex, "'\":");
			testRegex(compiler, regex, "aA0 \\_-!~`@#");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+=");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+=[]{}|'\"");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+=[]{}|:;");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+=[]{}|:;\"");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+=[]{}|;'\"a:");
			testRegex(compiler, regex, "aA0 \\_-!~`@#$%^&*()-+=[]{}|:;\"',./<>?");
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static boolean testRegex(PatternCompiler compiler, String regex, String val) throws MalformedPatternException {
		Pattern pattern = compiler.compile(regex);
		boolean isMatched = DODataDaoUtil.isFormatOfPattern(pattern, val);
		
		System.out.println(regex + " " + isMatched + " of match:" + val);
		
		return isMatched;
	}
	
	
	
	@Test
	public void testSplitByDelim1() {
		String val = "a,b,c";

		testSplitByDelim(val, ",");
	}
	
	@Test
	public void testSplitByScopeDelim1() {
		String val = "a~b";
		testSplitByScopeDelim(val);
	}

	@Test
	public void testSplitByScopeDelim2() {
		String val = "~b";
		testSplitByScopeDelim(val);
	}

	@Test
	public void testSplitByScopeDelim3() {
		String val = "a~";
		testSplitByScopeDelim(val);
	}
	
	@Test
	public void testSplitByScopeDelim4() {
		String val = "~";
		testSplitByScopeDelim(val);
	}

	@Test
	public void testSplitByScopeDelim5() {
		String val = "ab";
		testSplitByScopeDelim(val);
	}

	private void testSplitByDelim(String val, String delim) {
		String[] vals = DODataDaoUtil.splitByDelim(val, delim);

		System.out.print("val:" + val + "\tvals:");
		for(int i = 0; i < vals.length; i++) {
			if(i > 0) {
				System.out.print(",");
			}
			System.out.print(vals[i]);
		}
		
		System.out.println();
	}
	
	private void testSplitByScopeDelim(String val) {
		String[] vals = DODataDaoUtil.splitByScopeDelim(val);

		System.out.print("val:" + val + "\tvals:");
		for(int i = 0; i < vals.length; i++) {
			if(i > 0) {
				System.out.print(",");
			}
			System.out.print(vals[i]);
		}
		
		System.out.println();
	}
	
}
