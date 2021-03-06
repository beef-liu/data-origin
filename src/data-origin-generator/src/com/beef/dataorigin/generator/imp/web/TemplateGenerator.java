package com.beef.dataorigin.generator.imp.web;

import httl.Engine;
import httl.Template;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.beef.dataorigin.context.DataOriginContext;
import com.beef.dataorigin.generator.DataOriginGeneratorContext;
import com.beef.dataorigin.generator.DataOriginGenerator.WebGenerateOverwriteFlag;
import com.beef.dataorigin.generator.util.DataOriginGeneratorUtil;
import com.beef.dataorigin.setting.meta.MetaDataImportSetting;
import com.beef.dataorigin.setting.meta.data.MetaDataField;
import com.beef.dataorigin.util.ExcelUtil;
import com.salama.modeldriven.util.db.DBTable;

public class TemplateGenerator {
	private final static Logger logger = Logger.getLogger(TemplateGenerator.class);
	
	public final static String TEMPLATE_FILE_NAME_PATTERN = "${";
	private final static String JAVA_PACKAGE_PREFIX = "package ";

	public static enum TemplateCommentType {HtmlComment, JavaComment};

	public final static String TEMPLATE_PARAM_NAME_TABLE_LIST = "tables";
	public final static String TEMPLATE_PARAM_NAME_TABLE_NAME_LIST = "tableNames";
	public final static String TEMPLATE_PARAM_NAME_EMPTY = "empty";
	public final static String TEMPLATE_PARAM_NAME_NULL = "null";
	public final static String TEMPLATE_PARAM_NAME_TABLE_NAME = "tableName";
	public final static String TEMPLATE_PARAM_NAME_DATA_CLASS_NAME = "dataClassName";
	public final static String TEMPLATE_PARAM_NAME_DBTABLE = "dbTable";
	public final static String TEMPLATE_PARAM_NAME_BASE_PACKAGE = "basePackage";
	public final static String TEMPLATE_PARAM_NAME_WEB_CONTEXT_NAME = "webContextName";
	public final static String TEMPLATE_PARAM_NAME_META_DATA_UI_SETTING = "dataUISetting";
	public final static String TEMPLATE_PARAM_NAME_MDBTABLE = "mDBTable";
	public final static String TEMPLATE_PARAM_NAME_MMETA_DATA_UI_SETTING = "mDataUISetting";
	
	
	public final static String TEMPLATE_FILE_NAME_PATTERN_TABLE_NAME = "${" + TEMPLATE_PARAM_NAME_TABLE_NAME + "}";
	public final static String TEMPLATE_FILE_NAME_PATTERN_DATA_CLASS_NAME = "${" + TEMPLATE_PARAM_NAME_DATA_CLASS_NAME + "}";

	public static void generateFile(
			DataOriginContext dataOriginContext,
			DataOriginGeneratorContext generatorContext,
			File file,
			WebGenerateOverwriteFlag overwriteFlg) throws IOException, ParseException {
		boolean isJavaFile = file.getName().toLowerCase().contains(DataOriginGeneratorUtil.FILE_EXT_JAVA);
		boolean needGenerateFile = file.getName().contains(TEMPLATE_FILE_NAME_PATTERN);
		
		if(!needGenerateFile) {
			generateTemplateFile(dataOriginContext, generatorContext, file, overwriteFlg, isJavaFile, needGenerateFile, null);
		} else {
			if(needGenerateFile && 
					(file.getName().contains(TEMPLATE_FILE_NAME_PATTERN_TABLE_NAME)
						|| file.getName().contains(TEMPLATE_FILE_NAME_PATTERN_DATA_CLASS_NAME)
						)
					) {
				//need enumerate table list
				DBTable dbTable;
				for(int i = 0; i < generatorContext.getDbTableList().size(); i++) {
					dbTable = generatorContext.getDbTableList().get(i);

					generateTemplateFile(dataOriginContext, generatorContext, file, overwriteFlg, isJavaFile, needGenerateFile, dbTable);
				}
			} else {
				generateTemplateFile(dataOriginContext, generatorContext, file, overwriteFlg, isJavaFile, needGenerateFile, null);
			}
		}
		
	}

	private static void generateTemplateFile(
			DataOriginContext dataOriginContext,
			DataOriginGeneratorContext generatorContext,
			File file, 
			WebGenerateOverwriteFlag overwriteFlg,
			boolean isJavaFile, boolean needGenerateFile,
			DBTable dbTable
			) throws IOException, ParseException {
		logger.debug("generateTemplateFile() file:" + file.getAbsolutePath());

		File destFile;
		Template template;
		Map<String, Object> templateParamMap = createTemplateParamMap(dataOriginContext, generatorContext, dbTable);

		if(isJavaFile) {
			destFile = getFileForJavaTemplateFile(dataOriginContext, generatorContext, dbTable, file, needGenerateFile, templateParamMap);
		} else {
			destFile = getFileForNotJavaTemplateFile(dataOriginContext, generatorContext, dbTable, file, needGenerateFile, templateParamMap);
		}
		
		if(destFile.exists()) {
			if(overwriteFlg == WebGenerateOverwriteFlag.NoOverwrite) {
				return;
			} else if(overwriteFlg == WebGenerateOverwriteFlag.OverwriteAll) {
			} else if (overwriteFlg == WebGenerateOverwriteFlag.OverwriteGeneratedFileOnly) {
				if(!needGenerateFile) {
					return;
				}
			}
		}
		
		if(!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdirs();
		}

		if(needGenerateFile) {
			String fileNameLowercase = file.getName();
			if(fileNameLowercase.endsWith(".xls") || fileNameLowercase.endsWith(".xlsx")) {
				//template excel
				createTemplateXls(dataOriginContext, dbTable, file, destFile);
			} else {
				template = createTemplate(generatorContext, dbTable, file);
				
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(destFile);
					template.render(templateParamMap, fos);
					
					logger.debug("Generated template file:" + file.getAbsolutePath());
				} finally {
					fos.close();
				}
			}
		} else {
			//just copy
			DataOriginGeneratorUtil.copyFile(file, destFile);
			logger.debug("copy file:" + file.getAbsolutePath() + " -> " + destFile.getAbsolutePath());
		}
	}
	
	private static void createTemplateXls(
			DataOriginContext dataOriginContext,
			DBTable dbTable,
			File file,
			File destFile) throws IOException {
		String fileNameLowercase = file.getName();
		boolean isXLSX = fileNameLowercase.endsWith(".xlsx");
		
		InputStream inputExcel = null;
		OutputStream outputExcel = null;
		try {
			inputExcel = new FileInputStream(file);
			Workbook workbook = ExcelUtil.createWorkbook(inputExcel, isXLSX);
			Sheet sheet = workbook.getSheetAt(0);
			
			//title --------------------------------------
			int beginCol = 0;
			int col;
			Row row = sheet.getRow(0);
			if(row == null) {
				row = sheet.createRow(0);
			}
			Cell cell;
			MetaDataField dataField;
			MetaDataImportSetting dataImportSetting = dataOriginContext.getMetaDataImportSetting(
					dbTable.getTableName());
			for(col = beginCol; col < dataImportSetting.getFieldList().size(); col++) {
				dataField = dataImportSetting.getFieldList().get(col);
				
				cell = row.getCell(col);
				if(cell == null) {
					cell = row.createCell(col);
				}
				cell.setCellValue(dataField.getFieldDispName());
			}
			
			//output
			outputExcel = new FileOutputStream(destFile);
			workbook.write(outputExcel);
		} finally {
			try {
				inputExcel.close();
			} catch(Throwable e) {
			}
			try {
				outputExcel.close();
			} catch(Throwable e) {
			}
		}
		
	}
	
	private static File getFileForJavaTemplateFile(
			DataOriginContext dataOriginContext,
			DataOriginGeneratorContext generatorContext,
			DBTable dbTable, 
			File templateFile,
			boolean needGenerateFileName,
			Map<String, Object> templateParamMap
			) throws IOException, ParseException {
		String javaPackage = getJavaPackageOfFile(templateFile);
		if(javaPackage.contains(TEMPLATE_FILE_NAME_PATTERN)) {
			javaPackage = generateTemplateString(dataOriginContext, generatorContext, dbTable, javaPackage, templateParamMap);
		}
		
		String destFileName; 
		if(needGenerateFileName) {
			//file name need be generated
			destFileName = generateTemplateString(dataOriginContext, generatorContext, dbTable, templateFile.getName(), templateParamMap); 
		} else {
			destFileName = templateFile.getName();
		}
		
		return DataOriginGeneratorUtil.getFileForJava(generatorContext.getOutputWebProjectJavaSrcDir(), javaPackage, destFileName);
	}

	private static File getFileForNotJavaTemplateFile(
			DataOriginContext dataOriginContext,
			DataOriginGeneratorContext generatorContext,
			DBTable dbTable, 
			File templateFile,
			boolean needGenerateFileName,
			Map<String, Object> templateParamMap
			) throws IOException, ParseException {
		String destFileName; 
		if(needGenerateFileName) {
			//file name need be generated
			destFileName = generateTemplateString(dataOriginContext, generatorContext, dbTable, templateFile.getName(), templateParamMap); 
		} else {
			destFileName = templateFile.getName();
		}

		File destFile = new File(templateFile.getParentFile(), destFileName);
		String relativePath = DataOriginGeneratorUtil.getRelativePath(generatorContext.getTemplateDir(), destFile);
		
		return new File(generatorContext.getOutputWebProjectDir(), relativePath); 
	}
	
	private static String getJavaPackageOfFile(File javaFile) throws IOException {
		BufferedReader reader = null;

		try {
			String javaPackage = null;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(javaFile), DataOriginGeneratorContext.DefaultCharset));

			int index;
			String line;
			int i = 0;
			while(i < 10) {
				line = reader.readLine();
				
				if(line == null) {
					break;
				}
				
				index = line.indexOf(JAVA_PACKAGE_PREFIX);
				if(index >= 0) {
					javaPackage = line.substring(JAVA_PACKAGE_PREFIX.length()).trim();
					if(javaPackage.endsWith(";")) {
						javaPackage = javaPackage.substring(0, javaPackage.length() - 1);
					}
					break;
				}
				
				i++;
			}
			
			return javaPackage;
		} finally {
			reader.close();
		}
	}
	
	private static String generateTemplateString(
			DataOriginContext dataOriginContext,
			DataOriginGeneratorContext generatorContext,
			DBTable dbTable, 
			String templateFileName,
			Map<String, Object> templateParamMap
			) throws IOException, ParseException {
		Template template = createTemplate(generatorContext, dbTable, templateFileName, TemplateCommentType.JavaComment);
		//Map<String, Object> paramMap = createTemplateParamMap(dataOriginContext, generatorContext, dbTable);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		template.render(templateParamMap, bos);
		
		return bos.toString(DataOriginGeneratorContext.DefaultCharsetName);
	}
	
	private static Template createTemplate(
			DataOriginGeneratorContext generatorContext,
			DBTable dbTable,
			File templateFile
			) throws IOException, ParseException {
		TemplateCommentType commentType;
		String templateFileNameLowercase = templateFile.getName().toLowerCase();
		if(templateFileNameLowercase.endsWith(".js") 
				|| templateFileNameLowercase.endsWith(".java")
		) {
			commentType = TemplateCommentType.JavaComment;
		} else {
			commentType = TemplateCommentType.HtmlComment;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOriginGeneratorUtil.readFile(templateFile, bos);
		String templateContent = bos.toString(DataOriginGeneratorContext.DefaultCharsetName);

		return createTemplate(generatorContext, dbTable, templateContent, commentType);
	}

	private static Template createTemplate(
			DataOriginGeneratorContext generatorContext,
			DBTable dbTable,
			String templateContent,
			TemplateCommentType commentType
			) throws IOException, ParseException {
		Properties httlConfig = new Properties();
		//httlConfig.setProperty("template.directory", generatorContext.getTemplateDir().getAbsolutePath());
		//httlConfig.setProperty("loaders", "httl.spi.loaders.FileLoader");
		
//		httlConfig.setProperty("template.suffix", ".httl,.html,.js,.java");
//		httlConfig.setProperty("import.packages", "com.salama.modeldriven.util.db,com.beef.dataorigin.setting");
//		httlConfig.setProperty("import.getters", "get,is");
		
		if(commentType == TemplateCommentType.JavaComment) {
			httlConfig.setProperty("comment.left", "/*");
			httlConfig.setProperty("comment.right", "*/");
		} else {
			httlConfig.setProperty("comment.left", "<!--");
			httlConfig.setProperty("comment.right", "-->");
		}
		
		Engine engine = Engine.getEngine(httlConfig);
		return engine.parseTemplate(templateContent);
	}
	
	private static Map<String, Object> createTemplateParamMap(
			DataOriginContext dataOriginContext,
			DataOriginGeneratorContext generatorContext,
			DBTable dbTable) {
		Map<String, Object> httlParams = new HashMap<String, Object>();
		httlParams.put(TEMPLATE_PARAM_NAME_EMPTY, "");
		httlParams.put(TEMPLATE_PARAM_NAME_NULL, "");
		httlParams.put(TEMPLATE_PARAM_NAME_TABLE_NAME_LIST, dataOriginContext.getTableNameList());
		httlParams.put(TEMPLATE_PARAM_NAME_TABLE_LIST, dataOriginContext.getDBTableList());
		
		httlParams.put(TEMPLATE_PARAM_NAME_BASE_PACKAGE, generatorContext.getOutputWebProjectJavaPackage());
		httlParams.put(TEMPLATE_PARAM_NAME_WEB_CONTEXT_NAME, generatorContext.getOutputWebContextName());

		if(dbTable != null) {
			logger.debug("createTemplateParamMap() tableName:" + dbTable.getTableName());
			
			httlParams.put(TEMPLATE_PARAM_NAME_TABLE_NAME, dbTable.getTableName().toLowerCase());
			httlParams.put(TEMPLATE_PARAM_NAME_DATA_CLASS_NAME, dataOriginContext.getMetaDataUISetting(dbTable.getTableName()).getDataClassName());
			
			httlParams.put(TEMPLATE_PARAM_NAME_DBTABLE, dbTable);
			httlParams.put(TEMPLATE_PARAM_NAME_META_DATA_UI_SETTING, dataOriginContext.getMetaDataUISetting(dbTable.getTableName()));
			httlParams.put(TEMPLATE_PARAM_NAME_MMETA_DATA_UI_SETTING, dataOriginContext.getMMetaDataUISetting(dbTable.getTableName()));
			httlParams.put(TEMPLATE_PARAM_NAME_MDBTABLE, dataOriginContext.getMDBTable(dbTable.getTableName()));
		}
		
		return httlParams;
	}
	
}
