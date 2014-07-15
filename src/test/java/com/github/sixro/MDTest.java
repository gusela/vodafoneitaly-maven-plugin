package com.github.sixro;

import static org.junit.Assert.*;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.joda.time.*;
import org.junit.*;

import com.github.sixro.util.Excel;

public class MDTest {

	private static final String SOFTWARES_DIR = "src/test/resources/softwares";
	private static final File OUTPUT_DIRECTORY = new File("target/md");
	
	private MD md;

	@Before public void setup() throws IOException {
		if (OUTPUT_DIRECTORY.exists())
			FileUtils.deleteDirectory(OUTPUT_DIRECTORY);
		OUTPUT_DIRECTORY.mkdirs();
		
		md = new MD(new File("src/test/resources/MD.xls"), "ST11111", "Merlino", "1.0.1", LocalDate.parse("2014-05-12"));
		md.addSoftware(new File(SOFTWARES_DIR, "pippo.bin"));
		md.addSoftware(new File(SOFTWARES_DIR, "pluto.zip"));
	}
	
	@Test public void set_canvass_date() throws IOException {
		File file = md.saveTo(OUTPUT_DIRECTORY);
		assertEquals(LocalDateTime.parse("2014-05-12"), new Excel(file).getCellDateByName("date"));
	}
	
	@Test public void fill_document_with_information_of_each_software_found() throws IOException {
		File file = md.saveTo(OUTPUT_DIRECTORY);
		
		Excel excel = new Excel(file);
		assertEquals("pippo.bin", excel.getCellTextByRef("E9"));
		
		assertEquals("ST11111", excel.getCellTextByRef("C10"));
		assertEquals("Merlino", excel.getCellTextByRef("D10"));
		assertEquals("pluto.zip", excel.getCellTextByRef("E10"));
		assertEquals("zip", excel.getCellTextByRef("F10"));
		assertEquals("12738659", excel.getCellTextByRef("H10"));
	}

}
