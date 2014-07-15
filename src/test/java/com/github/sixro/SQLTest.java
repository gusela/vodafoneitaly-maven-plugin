package com.github.sixro;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SQLTest {

	private static final String DOS_LINE_ENDING = "\r\n";
	
	private static final File DEST = new File("target/sql/NEW_BOH00027.sql");
	private static final File SOURCE = new File("src/test/resources/sql/BOH00027.sql");

	@Test public void save_file_in_dos() throws IOException {
		assertFalse(fileContainsCharacters(SOURCE, DOS_LINE_ENDING));
		
		SQL sql = new SQL(SOURCE);
		sql.save(DEST);

		assertTrue(fileContainsCharacters(DEST, DOS_LINE_ENDING));
	}

	private boolean fileContainsCharacters(File file, String text) throws FileNotFoundException, IOException {
		return IOUtils.toString(new FileReader(file)).contains(text);
	}

}
