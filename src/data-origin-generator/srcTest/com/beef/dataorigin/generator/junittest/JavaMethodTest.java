package com.beef.dataorigin.generator.junittest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class JavaMethodTest {

	@Test
	public void test1() {
		Method[] methods = TemplateDataImportExportService.class.getMethods();
		
		for(Method methodTmp : methods) {
			if(methodTmp.getName().equals("downloadTempExcel")) {
				System.out.println("isPublic:" + ((methodTmp.getModifiers() & Modifier.PUBLIC) != 0));
			}
		}
	}
}
