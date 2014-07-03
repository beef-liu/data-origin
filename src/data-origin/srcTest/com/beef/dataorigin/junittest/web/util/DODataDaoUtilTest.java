package com.beef.dataorigin.junittest.web.util;

import static org.junit.Assert.*;

import org.junit.Test;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;

import com.beef.dataorigin.setting.DataOriginSetting;
import com.beef.dataorigin.web.util.DODataDaoUtil;

public class DODataDaoUtilTest {

	@Test
	public void test() {
		try {
			DataOriginSetting setting = new DataOriginSetting();
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(DataOriginSetting.class.getSimpleName() + ".xml", setting, DataOriginSetting.class, XmlDeserializer.DefaultCharset);
		} catch(Throwable e) {
			e.printStackTrace();
		}
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
