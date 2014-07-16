package com.github.sixro.vodafoneitalymavenplugin.util;

import static org.junit.Assert.*;

import java.io.*;

import org.joda.time.LocalDateTime;
import org.junit.*;

import com.github.sixro.vodafoneitalymavenplugin.util.Excel;

public class ExcelTest {

	private static final File SOURCE_FILE = new File("src/test/resources/example.xls");
	private Excel excel;

	@Before public void setup() throws IOException {
		excel = new Excel(SOURCE_FILE);
	}
	
	@Test public void setCellByName_change_text() {
		String name = "myName";
		assertEquals("A", excel.getCellTextByName(name));
		
		excel.setCellByName(name, "B");
		assertEquals("B", excel.getCellTextByName(name));
	}
	
	@Test public void setCellByName_change_date() {
		String name = "myDate";
		assertEquals(LocalDateTime.parse("1975-09-21"), excel.getCellDateByName(name));
		
		LocalDateTime newDateTime = LocalDateTime.parse("2014-07-15");
		excel.setCellByName(name, newDateTime);
		assertEquals(newDateTime, excel.getCellDateByName(name));
	}

	@Test public void save_store_a_new_file_preserving_the_content_of_starting_file() throws IOException {
		File destfile = new File("target/excel/example_changed.xls");
		destfile.delete();
		destfile.getParentFile().mkdirs();
		
		String name = "myName";
		excel.setCellByName(name, "B");
		excel.save(destfile);
		
		assertEquals("A", new Excel(SOURCE_FILE).getCellTextByName(name));
		assertEquals("B", new Excel(destfile).getCellTextByName(name));
	}

	@Test public void getCellTextByRef_returns_expected_text() {
		assertEquals("A", excel.getCellTextByRef("C2"));
	}
	
	@Test public void setCellByRef_change_text() {
		String ref = "E3";
		assertEquals("", excel.getCellTextByRef(ref));
		
		excel.setCellByRef(ref, "X");
		assertEquals("X", excel.getCellTextByRef(ref));
	}
	
}
