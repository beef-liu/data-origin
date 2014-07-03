package com.beef.dataorigin.generator.imp.settings;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.beef.dataorigin.generator.DataOriginGenerator;
import com.beef.dataorigin.generator.DataOriginGeneratorContext;
import com.beef.dataorigin.generator.util.DataOriginGeneratorUtil;
import com.salama.modeldriven.generator.data.DataGeneratorOfJava;
import com.salama.modeldriven.generator.data.DataGeneratorOfObjectiveC;

public class DBDataClassGenerator {

	
	public static void generateAll(DataOriginGeneratorContext generatorContext) throws ResourceNotFoundException, ParseErrorException, Exception {
		
		String packageOfDataDB = generatorContext.getOutputWebProjectJavaPackage() + "." + DataOriginGeneratorContext.JAVA_SUB_PACKAGE_DATA_DB;
		File outputDir = DataOriginGeneratorUtil.getPackageDirForJava(
				generatorContext.getOutputWebProjectJavaSrcDir(), 
				packageOfDataDB);
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}

		Connection conn = null;
		try {
			conn = generatorContext.createConnectionOfProductionDB();

			{
				DataGeneratorOfJava.createAllTableData(conn, outputDir, packageOfDataDB);
			}
		} finally {
			try {
				conn.close();
			} catch(Throwable e) {
			}
			File velocityLogFile = new File(outputDir, "velocity_generator.log");
			if(velocityLogFile.exists()) {
				velocityLogFile.delete();
			}
		}
		
	}
	
}
