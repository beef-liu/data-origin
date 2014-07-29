package com.beef.dataorigin.generator.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.beef.dataorigin.generator.DataOriginGeneratorContext;

public class DataOriginGeneratorUtil {
	
	public final static String FILE_EXT_JAVA = ".java";

	public static String getRelativePath(File baseDir, File file) {
		String filePath = file.getAbsolutePath();
		String basePath = baseDir.getAbsolutePath();
		
		String relativePath = filePath.substring(basePath.length());
		if(relativePath.charAt(0) == File.separatorChar) {
			relativePath = relativePath.substring(1);
		}
		
		return relativePath;
	}
	
	public static File getFileForJava(File srcDir, String javaPackage, String javaFileName) {
		return new File(getPackageDirForJava(srcDir, javaPackage), javaFileName);
	}
	
	public static File getPackageDirForJava(File srcDir, String javaPackage) {
		return new File(srcDir, javaPackage.replace('.', File.separatorChar));
	}
	
	public static int readFile(File file, OutputStream output) throws IOException {
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(file);
			
			return copy(fis, output);
		} finally {
			fis.close();
		}
		
	}
	
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] tempBuff = new byte[1024];
		int readCnt;
		int totalRead = 0;
		while(true) {
			readCnt = input.read(tempBuff, 0, tempBuff.length);
			
			if(readCnt < 0) {
				break;
			}
			
			totalRead += readCnt;

			if(readCnt > 0) {
				output.write(tempBuff, 0, readCnt);
				output.flush();
			}
		}
		
		return totalRead;
	}

	public static void copyFile(File srcFile, File destFile) throws IOException {
		FileOutputStream fos = null;
		FileInputStream fis = null;
		
		try {
			byte[] tempBuff = new byte[10240];
			
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			
			int readCnt;
			
			while(true) {
				readCnt = fis.read(tempBuff, 0, tempBuff.length);
				
				if(readCnt < 0) {
					break;
				}
				
				if(readCnt > 0) {
					fos.write(tempBuff, 0, readCnt);
					fos.flush();
				}
			}
			
		} finally {
			try {
				fos.close();
			} catch(Throwable e) {
			}
			try {
				fis.close();
			} catch(Throwable e) {
			}
		}
	}
	
	
}
