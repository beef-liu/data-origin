package com.beef.dataorigin.generator.junittest;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.junit.Test;

import com.beef.dataorigin.util.ExcelUtil;

public class ExcelTest {
	
	@Test
	public void testSetColor() {
		String inFileName = "testFiles/test.xlsx";
		String outFileName = "testFiles/test_outupt.xlsx";
		InputStream inputExcel = null;
		OutputStream outputExcel = null;
		
		try { 
			inputExcel = new FileInputStream(inFileName);
			Workbook workbook = ExcelUtil.createWorkbook(inputExcel, true);
			inputExcel.close();

			
			CellStyle cellStyle = workbook.createCellStyle();
			//cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.index);
			//cellStyle.setFillPattern(CellStyle.SQUARES);
			cellStyle.setFillForegroundColor(IndexedColors.BLACK.index);
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			//cellStyle.setFillBackgroundColor(new XSSFColor(Color.PINK));
			//cellStyle.setFillForegroundColor(new XSSFColor(Color.PINK));
			
			Sheet sheet = workbook.getSheetAt(0);
			Row row;
			Cell cell;
			
			for(int i = 0; i < AllIndexedColorObjs.length; i++) {
				row = sheet.getRow(i);
				if(row == null) {
					row = sheet.createRow(i);
				}
				cell = row.getCell(0);
				if(cell == null) {
					cell = row.createCell(0);
				}
				
				cell.setCellValue(AllIndexedColorObjs[i].colorName);
				//cellStyle = cell.getCellStyle();
				
				cellStyle = workbook.createCellStyle();
				cellStyle.setFillForegroundColor(AllIndexedColorObjs[i].color);
				cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
				cell.setCellStyle(cellStyle);
			}

			outputExcel = new FileOutputStream(outFileName);
			workbook.write(outputExcel);
			outputExcel.close();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private final static IndexedColorObj[] AllIndexedColorObjs = new IndexedColorObj[] {
		new IndexedColorObj(IndexedColors.BLACK.index, "BLACK"),
		new IndexedColorObj(IndexedColors.WHITE.index, "WHITE"),
		new IndexedColorObj(IndexedColors.RED.index, "RED"),
		new IndexedColorObj(IndexedColors.BRIGHT_GREEN.index, "BRIGHT_GREEN"),
		new IndexedColorObj(IndexedColors.BLUE.index, "BLUE"),
		new IndexedColorObj(IndexedColors.YELLOW.index, "YELLOW"),
		new IndexedColorObj(IndexedColors.PINK.index, "PINK"),
		new IndexedColorObj(IndexedColors.TURQUOISE.index, "TURQUOISE"),
		new IndexedColorObj(IndexedColors.DARK_RED.index, "DARK_RED"),
		new IndexedColorObj(IndexedColors.GREEN.index, "GREEN"),
		new IndexedColorObj(IndexedColors.DARK_BLUE.index, "DARK_BLUE"),
		new IndexedColorObj(IndexedColors.DARK_YELLOW.index, "DARK_YELLOW"),
		new IndexedColorObj(IndexedColors.VIOLET.index, "VIOLET"),
		new IndexedColorObj(IndexedColors.TEAL.index, "TEAL"),
		new IndexedColorObj(IndexedColors.GREY_25_PERCENT.index, "GREY_25_PERCENT"),
		new IndexedColorObj(IndexedColors.GREY_50_PERCENT.index, "GREY_50_PERCENT"),
		new IndexedColorObj(IndexedColors.CORNFLOWER_BLUE.index, "CORNFLOWER_BLUE"),
		new IndexedColorObj(IndexedColors.MAROON.index, "MAROON"),
		new IndexedColorObj(IndexedColors.LEMON_CHIFFON.index, "LEMON_CHIFFON"),
		new IndexedColorObj(IndexedColors.ORCHID.index, "ORCHID"),
		new IndexedColorObj(IndexedColors.CORAL.index, "CORAL"),
		new IndexedColorObj(IndexedColors.ROYAL_BLUE.index, "ROYAL_BLUE"),
		new IndexedColorObj(IndexedColors.LIGHT_CORNFLOWER_BLUE.index, "LIGHT_CORNFLOWER_BLUE"),
		new IndexedColorObj(IndexedColors.SKY_BLUE.index, "SKY_BLUE"),
		new IndexedColorObj(IndexedColors.LIGHT_TURQUOISE.index, "LIGHT_TURQUOISE"),
		new IndexedColorObj(IndexedColors.LIGHT_GREEN.index, "LIGHT_GREEN"),
		new IndexedColorObj(IndexedColors.LIGHT_YELLOW.index, "LIGHT_YELLOW"),
		new IndexedColorObj(IndexedColors.PALE_BLUE.index, "PALE_BLUE"),
		new IndexedColorObj(IndexedColors.ROSE.index, "ROSE"),
		new IndexedColorObj(IndexedColors.LAVENDER.index, "LAVENDER"),
		new IndexedColorObj(IndexedColors.TAN.index, "TAN"),
		new IndexedColorObj(IndexedColors.LIGHT_BLUE.index, "LIGHT_BLUE"),
		new IndexedColorObj(IndexedColors.AQUA.index, "AQUA"),
		new IndexedColorObj(IndexedColors.LIME.index, "LIME"),
		new IndexedColorObj(IndexedColors.GOLD.index, "GOLD"),
		new IndexedColorObj(IndexedColors.LIGHT_ORANGE.index, "LIGHT_ORANGE"),
		new IndexedColorObj(IndexedColors.ORANGE.index, "ORANGE"),
		new IndexedColorObj(IndexedColors.BLUE_GREY.index, "BLUE_GREY"),
		new IndexedColorObj(IndexedColors.GREY_40_PERCENT.index, "GREY_40_PERCENT"),
		new IndexedColorObj(IndexedColors.DARK_TEAL.index, "DARK_TEAL"),
		new IndexedColorObj(IndexedColors.SEA_GREEN.index, "SEA_GREEN"),
		new IndexedColorObj(IndexedColors.DARK_GREEN.index, "DARK_GREEN"),
		new IndexedColorObj(IndexedColors.OLIVE_GREEN.index, "OLIVE_GREEN"),
		new IndexedColorObj(IndexedColors.BROWN.index, "BROWN"),
		new IndexedColorObj(IndexedColors.PLUM.index, "PLUM"),
		new IndexedColorObj(IndexedColors.INDIGO.index, "INDIGO"),
		new IndexedColorObj(IndexedColors.GREY_80_PERCENT.index, "GREY_80_PERCENT"),
		new IndexedColorObj(IndexedColors.AUTOMATIC.index, "AUTOMATIC"),
	};

	private static class IndexedColorObj {
		public short color;
		public String colorName;
		
		public IndexedColorObj() {
			
		}
		
		public IndexedColorObj(short c, String nm) {
			this.color = c;
			this.colorName = nm;
		}
	}

}
