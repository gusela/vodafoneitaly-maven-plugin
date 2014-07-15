package com.github.sixro;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.joda.time.LocalDate;
import org.junit.*;

public class PackageMojoTest {

	private static final File KIT_SOURCE_DIRECTORY = new File("src/test/resources/myCustomKit");
	private static final File KIT_OUTPUT_DIRECTORY = new File("target/kit/");
	private static final File SQ_OUTPUT_DIR = new File("target/sq/");
	private static final File MD_OUTPUT_DIR = new File("target/md/");
	private static final File MD_TEMPLATE = new File("src/test/resources/MD.xls");
	private static final File SQ_TEMPLATE = new File("src/test/resources/SQ.xls");
	private static final File SQLS_DIR = new File("src/test/resources/sql/");
	private static final File SOFTWARES_DIR = new File("src/test/resources/softwares");
	private PackageMojo packageMojo;

	@Before public void setup() throws IOException {
		cleanDirectory(SQ_OUTPUT_DIR);
		cleanDirectory(MD_OUTPUT_DIR);
		cleanDirectory(KIT_OUTPUT_DIRECTORY);
		
		packageMojo = new PackageMojo();
	}

	@Test public void generateAllSQ_create_2_files_SQ_containing_information_grabbed_by_scripts_also_when_scripts_are_3() throws IOException {
		packageMojo.generateAllSQ(SQLS_DIR, SQ_OUTPUT_DIR, SQ_TEMPLATE, "1.0.1", LocalDate.parse("2014-07-21"));
		
		assertEquals(2, numberOfFiles(SQ_OUTPUT_DIR, "SQ-", ".xls"));
		assertTrue(containsFile(SQ_OUTPUT_DIR, "SQ-Merlino_QDT-V1.0.1-20140721.xls"));
		assertTrue(containsFile(SQ_OUTPUT_DIR, "SQ-DAMS_DLR-V1.0.1-20140721.xls"));
	}

	@Test public void generateMD_create_1_file_with_expected_name() throws IOException {
		packageMojo.generateMD(SOFTWARES_DIR, MD_OUTPUT_DIR, MD_TEMPLATE, "ST11111", "Mer", "1.0.1", LocalDate.parse("2014-07-21"));
		
		assertEquals(1, numberOfFiles(MD_OUTPUT_DIR, "MD-", ".xls"));
		assertTrue(containsFile(MD_OUTPUT_DIR, "MD-Mer-V1.0.1-20140721.xls"));
	}

	@Test public void copyAllKitFiles_create_the_same_number_of_files_in_output_directory() throws IOException {
		packageMojo.copyAllKitFiles(KIT_SOURCE_DIRECTORY, KIT_OUTPUT_DIRECTORY, "Mer", "1.0.2", LocalDate.parse("2014-07-21"), new Properties());
		
		assertEquals(3, numberOfFiles(KIT_OUTPUT_DIRECTORY));
	}

	@Test public void copyAllKitFiles_copy_files_without_numbers_in_name_with_a_name_following_naming_rules() throws IOException {
		packageMojo.copyAllKitFiles(KIT_SOURCE_DIRECTORY, KIT_OUTPUT_DIRECTORY, "Mer", "1.0.2", LocalDate.parse("2014-07-21"), new Properties());
		
		assertTrue(containsFile(KIT_OUTPUT_DIRECTORY, "RN-Mer-V1.0.2-20140721.docx"));
	}

	@Test public void hasToBeRenamed_returns_false_when_input_contains_a_number() {
		assertFalse(packageMojo.hasToBeRenamed(new File("RN-1.docx")));
	}

	@Test public void hasToBeRenamed_returns_true_when_input_does_not_contain_a_number() {
		assertTrue(packageMojo.hasToBeRenamed(new File("RN-.docx")));
	}

	private int numberOfFiles(File dir, String prefix, String suffix) {
		return FileUtils.listFiles(dir, new AndFileFilter(new PrefixFileFilter(prefix), new SuffixFileFilter(suffix)), TrueFileFilter.INSTANCE).size();
	}

	private int numberOfFiles(File dir) {
		return FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size();
	}

	@SuppressWarnings("unchecked")
	private boolean containsFile(File directory, String filename) {
		Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			if (file.getName().equalsIgnoreCase(filename))
				return true;
		}
		return false;
	}

	private void cleanDirectory(File dir) throws IOException {
		if (dir.exists())
			FileUtils.deleteDirectory(dir);
		dir.mkdirs();
	}
	
}

