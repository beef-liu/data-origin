package com.beef.dataorigin.generator.junittest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import httl.Engine;
import httl.Template;

import org.junit.Test;

import MetoXML.XmlSerializer;

import com.beef.dataorigin.generator.DataOriginGenerator;
import com.beef.dataorigin.generator.DataOriginGeneratorContext;
import com.salama.modeldriven.util.db.DBTable;
import com.salama.modeldriven.util.db.mysql.MysqlTableInfoUtil;

public class HttlTest {
	DataOriginGeneratorContext _generatorContext;

	public HttlTest() {
		try {
			_generatorContext = new DataOriginGeneratorContext();		
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testHttl1() {
		try {
			String s = URLEncoder.encode(" +", "utf-8");
			System.out.println("s:" + s);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testHttl("httl_test1.html");
	}

	@Test
	public void testHttl2() {
		testHttl("httl_test1.java");
	}

	public void testHttl(String fileName) {
		FileOutputStream fos = null;
		
		try {
			File templateDir = new File("testFiles/");
			String templateDirPath = templateDir.getAbsolutePath();
			String templFilePath = "#" + fileName;
			File outputFile = new File(templateDir, "_" + fileName);
			DBTable dbTable = getDBTable("Brand");
			
			Map<String, Object> httlParams = new HashMap<String, Object>();
			httlParams.put("table", dbTable);
			httlParams.put("empty", "");
			httlParams.put("testparam", "testparam ok");
			
			Properties httlConfig = new Properties();
			httlConfig.setProperty("template.directory", templateDirPath);
			httlConfig.setProperty("template.suffix", ".httl,.html,.js,.java");
			httlConfig.setProperty("loaders", "httl.spi.loaders.FileLoader");
			httlConfig.setProperty("import.packages", "com.salama.modeldriven.util.db,com.beef.dataorigin.setting");
			httlConfig.setProperty("import.getters", "get,is");
			
			//httlConfig.setProperty("comment.left", "get,is");
			
			Engine engine = Engine.getEngine(httlConfig);
			Template template = engine.getTemplate(templFilePath, "utf-8");
			
			fos = new FileOutputStream(outputFile);
			template.render(httlParams, fos);
			
			
			template = engine.parseTemplate("test ${testparam} !!!");
			//String
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			template.render(httlParams, bos);
			String result = bos.toString();
			System.out.println("Test String Template:" + result);
		} catch(Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private DBTable getDBTable(String tableName) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		
		try {
			conn = _generatorContext.createConnectionOfProductionDB();
			
			//List<String> _dbTableNameList = MysqlTableInfoUtil.getAllTables(conn);
			//dbTableName = _dbTableNameList.get(i);
			return MysqlTableInfoUtil.GetTable(conn, tableName);
			
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
		}
	}
}
