package com.github.sixro.vodafoneitalymavenplugin;

import static org.junit.Assert.*;

import java.io.*;

import org.joda.time.*;
import org.junit.*;

import com.github.sixro.vodafoneitalymavenplugin.util.Excel;

public class SQTest {

	private static final File TEMPLATE = new File("src/test/resources/SQ.xls");
	private static final String SYSTEM = "Merlino";
	private static final String DATABASE = "QDT";
	private static final String VERSION = "1.0.2";
	private static final LocalDate DATE = LocalDate.parse("2014-07-21");
	
	private SQL BOH00029;
	private SQL BOH00030;

	private SQ sq;

	@Before public void setup() throws IOException {
		BOH00029 = new SQL(new File("src/test/resources/sql/BOH00029.sql"));
		BOH00030 = new SQL(new File("src/test/resources/sql/BOH00030.sql"));

		sq = new SQ(TEMPLATE, SYSTEM, DATABASE, VERSION, DATE);
		sq.addSQL(BOH00029);
		sq.addSQL(BOH00030);
	}
	
	@Test public void store_a_file_with_expected_Vodafone_name() throws IOException {
		File sqFile = sq.saveTo(new File("target/sq"));
		assertNotNull(sqFile);
		assertEquals("SQ-Merlino_QDT-V1.0.2-20140721.xls", sqFile.getName());
	}

	@Test public void stored_file_contains_canvass_date() throws IOException {
		File sqFile = sq.saveTo(new File("target/sq"));
		
		assertEquals(LocalDateTime.parse("2014-07-21"), new Excel(sqFile).getCellDateByName("date"));
	}

	@Test public void stored_file_contains_1_row_for_each_SQL() throws IOException {
		File sqFile = sq.saveTo(new File("target/sq"));
		
		Excel excel = new Excel(sqFile);
		assertEquals("BOH00029", excel.getCellTextByRef("E10"));

		assertEquals("SG06881", excel.getCellTextByRef("C11"));
		assertEquals(DATABASE, excel.getCellTextByRef("D11"));
		assertEquals("BOH00030", excel.getCellTextByRef("E11"));
		assertEquals("1.7", excel.getCellTextByRef("F11"));
		assertEquals("SQL", excel.getCellTextByRef("H11"));
		assertEquals("NTGCUSER", excel.getCellTextByRef("I11"));
		assertEquals("no", excel.getCellTextByRef("J11"));
		assertEquals("Marco Francardi", excel.getCellTextByRef("K11"));
		assertEquals("3 ore", excel.getCellTextByRef("L11"));
		assertEquals("Compilazione package PKG_USAGE_STATISTICS", excel.getCellTextByRef("M11"));
	}

}
