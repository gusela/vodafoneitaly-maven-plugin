package com.github.sixro;

import static org.junit.Assert.*;

import java.io.*;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.joda.time.LocalDate;
import org.junit.Test;

public class PackageMojoTest {

	@Test public void generateAllSQ_create_2_files_SQ_containing_information_grabbed_by_scripts_also_when_scripts_are_3() throws IOException {
		File sqOutputDirectory = new File("target/sq/");
		if (sqOutputDirectory.exists()) {
			FileUtils.deleteDirectory(sqOutputDirectory);
			sqOutputDirectory.mkdirs();
		}
		
		File kitDirectory = new File("src/test/resources/sql/");
		File sqTemplate = new File("src/test/resources/SQ.xls");
		if (! sqOutputDirectory.exists())
			sqOutputDirectory.mkdirs();
		
		PackageMojo packageMojo = new PackageMojo();
		packageMojo.generateAllSQ(kitDirectory, sqOutputDirectory, sqTemplate, "1.0.1", LocalDate.parse("2014-07-21"));
		
		assertEquals(2, FileUtils.listFiles(sqOutputDirectory, new AndFileFilter(new PrefixFileFilter("SQ-"), new SuffixFileFilter(".xls")), null).size());
		assertTrue(containsFile(sqOutputDirectory, "SQ-Merlino_QDT-V1.0.1-20140721.xls"));
		assertTrue(containsFile(sqOutputDirectory, "SQ-DAMS_DLR-V1.0.1-20140721.xls"));
	}

	@SuppressWarnings("unchecked")
	private boolean containsFile(File directory, String filename) {
		Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, null);
		for (File file : files) {
			if (file.getName().equalsIgnoreCase(filename))
				return true;
		}
		return false;
	}

}
