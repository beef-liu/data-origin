package com.beef.dataorigin.generator.imp.web;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import MetoXML.XmlSerializer;
import MetoXML.Base.XmlParseException;

import com.beef.dataorigin.context.DataOriginContext;
import com.beef.dataorigin.generator.DataOriginGenerator;
import com.beef.dataorigin.generator.DataOriginGeneratorContext;
import com.beef.dataorigin.generator.imp.settings.MetaDataFieldGenerator;
import com.beef.dataorigin.generator.util.DataOriginGeneratorUtil;
import com.beef.dataorigin.setting.meta.MetaDataUISetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.setting.meta.data.MetaSearchCondition;
import com.beef.dataorigin.setting.meta.data.MetaSearchConditionItem;
import com.salama.modeldriven.util.db.DBColumn;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.util.io.DirectoryRecursiveVisitor;

public class WebGenerator {

	public static void generateAll(final DataOriginGeneratorContext generatorContext, final boolean isOverwrite) 
			throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException, XmlParseException, InstantiationException, NoSuchMethodException {
		final DataOriginContext dataOriginContext = new DataOriginContext(generatorContext.getDataOriginDirManager().getBaseDir(), null);
		
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.isHidden();
			}
		};
		
		DirectoryRecursiveVisitor dirVisitor = new DirectoryRecursiveVisitor(fileFilter) {
			
			@Override
			protected void dealLeaveLeafDirectory(File currentPath) {
			}
			
			@Override
			protected void dealLeaveDirectory(File currentPath) {
			}
			
			@Override
			protected void dealFile(File currentFile) {
				try {
					TemplateGenerator.generateFile(dataOriginContext, generatorContext, currentFile, isOverwrite);
				} catch(Throwable e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			protected void dealEnterDirectory(File currentPath) {
			}
		};
		
		dirVisitor.recursiveVisit(generatorContext.getTemplateDir());
	}
	
	
	/*
	private static File destFileOfTemplateFileOfJava(
			DataOriginGeneratorContext generatorContext,
			File curFile) {
		String tRelativePath = DataOriginGeneratorUtil.getRelativePath(generatorContext.getOutputWebProjectJavaSrcDir(), curFile);
		
		return DataOriginGeneratorUtil.getFileForJava(
				generatorContext.getOutputWebProjectJavaSrcDir(), 
				generatorContext.getOutputWebProjectJavaPackage() + "." + tRelativePath.replace(File.separatorChar, '.'), 
				curFile.getName());
	}

	private static File destFileOfTemplateFileOfNotJava(
			File templateDir, File destDir, 
			File curFile) {
		String tRelativePath = DataOriginGeneratorUtil.getRelativePath(templateDir, curFile);
		return new File(destDir, tRelativePath);
	}
	*/
	
}
