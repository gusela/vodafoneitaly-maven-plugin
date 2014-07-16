package com.github.sixro;

import static org.junit.Assert.*;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.junit.*;

public class SQLTest {

	private static final String DOS_LINE_ENDING = "\r\n";
	
	private static final File DEST = new File("target/sql/NEW_BOH00027.sql");
	private static final File SOURCE = new File("src/test/resources/sql/BOH00027.sql");

	private SQL sql;

	@Before public void setup() throws IOException {
		sql = new SQL(SOURCE);
	}
	
	@Test public void save_file_in_dos() throws IOException {
		assertFalse(fileContainsCharacters(SOURCE, DOS_LINE_ENDING));
		
		sql.save(DEST);

		assertTrue(fileContainsCharacters(DEST, DOS_LINE_ENDING));
	}

	@Test public void updateFirstRowAsRequiredByDBA_change_first_line_using_sql_metadata() throws IOException {
		sql.updateFirstRowAsRequiredByDBA();
		assertEquals("REM $Id$ BOH00027,v 1.7 04/07/2014 00:00:00 QDT Exp $", sql.firstLine());
	}

	@Test public void version_returns_1_dot_7() throws IOException {
		assertEquals("1.7", sql.version());
	}

	@Test public void updateDate_returns_expected_date_after_version() throws IOException {
		assertEquals(LocalDate.parse("2014-07-04"), sql.updateDate());
	}

	@Test public void updateDate_returns_expected_date_also_when_there_is_no_text_after_that() throws IOException {
		SQL sql = new SQL(new File("src/test/resources/sql/BOH00028.sql"));
		assertEquals(LocalDate.parse("2014-01-14"), sql.updateDate());
	}

	@Test public void metadata_returns_expected_values() throws IOException {
		assertEquals("QDT", sql.metadata("DATABASE"));
		assertEquals("NTGCUSER", sql.metadata("SCHEMA"));
		assertEquals("1.7 - 04/07/2014 Ora non vengono piu' recuperati i dati relativi alla applicazione CheckNbaExistance perche' inutili", sql.metadata("VERSION"));
		assertEquals("", sql.metadata("BUG/OTHER"));
	}

	@Test(expected=SQL.NoSuchMetadataException.class) public void metadata_throws_an_error_when_metadata_is_not_found() throws IOException {
		sql.metadata("ABRACADABRA");
	}

	private boolean fileContainsCharacters(File file, String text) throws FileNotFoundException, IOException {
		return IOUtils.toString(new FileReader(file)).contains(text);
	}

}
