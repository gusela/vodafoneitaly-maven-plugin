package com.github.sixro.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.joda.time.LocalDateTime;

public class Excel {

	private HSSFWorkbook workBook;

	public Excel(File file) throws IOException {
		workBook = new HSSFWorkbook(new FileInputStream(file));
    }

	public String getCellTextByName(String name) {
		HSSFCell cell = findCellByName(name);
		String text = null;
		switch (cell.getCellType()) {
	        case Cell.CELL_TYPE_BOOLEAN:
	        	text = "" + cell.getBooleanCellValue();
	            break;
	        case Cell.CELL_TYPE_NUMERIC:
	            if (HSSFDateUtil.isCellDateFormatted(cell)) {
	                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateCellValue = cell.getDateCellValue();
                    text = parser.format(dateCellValue);
	            } else {
	                text = "" + cell.getNumericCellValue();
	            }
	            break;
	        case Cell.CELL_TYPE_STRING:
	            text = cell.getStringCellValue();
	            break;
	        case Cell.CELL_TYPE_BLANK:
	        	text = "";
	            break;
	        case Cell.CELL_TYPE_ERROR:
	        	// FIXME
	            break;
	        case Cell.CELL_TYPE_FORMULA:
	        	// FIXME
	            break;
	    }
		return text;
	}

	public String getCellTextByRef(String ref) {
		HSSFCell cell = findCellByRef(ref);
		if (cell == null)
			return "";
		
		String text = null;
		switch (cell.getCellType()) {
	        case Cell.CELL_TYPE_BOOLEAN:
	        	text = "" + cell.getBooleanCellValue();
	            break;
	        case Cell.CELL_TYPE_NUMERIC:
	            if (HSSFDateUtil.isCellDateFormatted(cell)) {
	                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateCellValue = cell.getDateCellValue();
                    text = parser.format(dateCellValue);
	            } else {
	                text = "" + cell.getNumericCellValue();
	            }
	            break;
	        case Cell.CELL_TYPE_STRING:
	            text = cell.getStringCellValue();
	            break;
	        case Cell.CELL_TYPE_BLANK:
	        	text = "";
	            break;
	        case Cell.CELL_TYPE_ERROR:
	        	// FIXME
	            break;
	        case Cell.CELL_TYPE_FORMULA:
	        	// FIXME
	            break;
	    }
		return text;
	}

	public LocalDateTime getCellDateByName(String name) {
		HSSFCell cell = findCellByName(name);
		LocalDateTime value = null;
		switch (cell.getCellType()) {
	        case Cell.CELL_TYPE_BOOLEAN:
                throw new IllegalArgumentException("name '" + name + "' refers to a boolean cell and not to a date cell");
	        case Cell.CELL_TYPE_NUMERIC:
	            if (HSSFDateUtil.isCellDateFormatted(cell)) {
	            	value = LocalDateTime.fromDateFields(cell.getDateCellValue());
	            } else {
	                throw new IllegalArgumentException("name '" + name + "' refers to a numeric cell and not to a date cell");
	            }
	            break;
	        case Cell.CELL_TYPE_STRING:
                throw new IllegalArgumentException("name '" + name + "' refers to a textual cell and not to a date cell");
	        case Cell.CELL_TYPE_BLANK:
	            break;
	        case Cell.CELL_TYPE_ERROR:
	        	// FIXME
	            break;
	        case Cell.CELL_TYPE_FORMULA:
	        	// FIXME
	            break;
	    }
		return value;
	}

	public void setCellByName(String name, String text) {
		HSSFCell cell = findCellByName(name);
		switch (cell.getCellType()) {
	        case Cell.CELL_TYPE_BOOLEAN:
	        	// FIXME
	        	//text = "" + cell.getBooleanCellValue();
	            break;
	        case Cell.CELL_TYPE_NUMERIC:
	        	// FIXME
//	            if (HSSFDateUtil.isCellDateFormatted(cell)) {
//	                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date dateCellValue = cell.getDateCellValue();
//                    text = parser.format(dateCellValue);
//	            } else {
//	                text = "" + cell.getNumericCellValue();
//	            }
	            break;
	        case Cell.CELL_TYPE_STRING:
	            cell.setCellValue(text);
	            break;
	        case Cell.CELL_TYPE_BLANK:
	        	// FIXME
	            break;
	        case Cell.CELL_TYPE_ERROR:
	        	// FIXME
	            break;
	        case Cell.CELL_TYPE_FORMULA:
	        	// FIXME
	            break;
	    }
	}

	public void setCellByName(String name, LocalDateTime datetime) {
		HSSFCell cell = findCellByName(name);
		switch (cell.getCellType()) {
	        case Cell.CELL_TYPE_BOOLEAN:
                throw new IllegalArgumentException("name '" + name + "' refers to a boolean cell and not to a date cell");
	        case Cell.CELL_TYPE_NUMERIC:
	        case Cell.CELL_TYPE_BLANK:
	            if (HSSFDateUtil.isCellDateFormatted(cell)) {
	            	cell.setCellValue(datetime.toDate());
	            } else {
	                throw new IllegalArgumentException("name '" + name + "' refers to a boolean cell and not to a date cell");
	            }
	            break;
	        case Cell.CELL_TYPE_STRING:
                throw new IllegalArgumentException("name '" + name + "' refers to a boolean cell and not to a date cell");
	        case Cell.CELL_TYPE_ERROR:
	        	// FIXME
	            break;
	        case Cell.CELL_TYPE_FORMULA:
	        	// FIXME
	            break;
	    }
	}

	public void setCellByRef(String ref, String text) {
		HSSFCell cell = findCellByRef(ref);
		cell.setCellValue(text);
	}

	public void save(File file) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(file);
		workBook.write(outputStream);
		outputStream.close();
	}

	private HSSFCell findCellByName(String name) {
//		int numberOfSheets = workBook.getNumberOfSheets();
//		for (int i = 0; i < numberOfSheets; i++) {
//			System.out.println("  sheet[" + i + "]: " + workBook.getSheetName(i));
//		}

		int nameIndex = workBook.getNameIndex(name);
		HSSFName hssfName = workBook.getNameAt(nameIndex);
		AreaReference areaReference = new AreaReference(hssfName.getRefersToFormula());
		CellReference cellReference = areaReference.getAllReferencedCells()[0];
		HSSFSheet sheet = workBook.getSheetAt(0);
		HSSFRow row = sheet.getRow(cellReference.getRow());
		return row.getCell((int) cellReference.getCol(), Row.CREATE_NULL_AS_BLANK);
	}

	private HSSFCell findCellByRef(String ref) {
		HSSFSheet sheet = workBook.getSheetAt(0);
		CellReference reference = new CellReference(ref);
		int refRow = reference.getRow();
		HSSFRow row = sheet.getRow(refRow);
		if (row == null)
			row = sheet.createRow(refRow);
		
		return row.getCell((int) reference.getCol(), Row.CREATE_NULL_AS_BLANK);
	}

}
