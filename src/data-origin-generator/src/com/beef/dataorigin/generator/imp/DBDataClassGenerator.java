package com.beef.dataorigin.generator.imp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.salama.modeldriven.generator.data.DataGeneratorOfJava;
import com.salama.modeldriven.generator.data.DataGeneratorOfObjectiveC;

public class DBDataClassGenerator {

	public static void generateAll(Connection conn, File outputDir, String namespace) throws ResourceNotFoundException, ParseErrorException, Exception {
		if(!outputDir.exists()) {
			outputDir.mkdir();
		}

		{
			File outputDirOfJava = new File(outputDir, "java");
			if(!outputDirOfJava.exists()) {
				outputDirOfJava.mkdirs();
			}
			DataGeneratorOfJava.createAllTableData(conn, outputDirOfJava, namespace);
		}
		
		{
			/*
			File outputDirOfObjC = new File(outputDir, "objC");
			if(!outputDirOfObjC.exists()) {
				outputDirOfObjC.mkdirs();
			}
			DataGeneratorOfObjectiveC.createAllTableData(conn, outputDirOfObjC);
			*/
		}
		
	}
	
}
