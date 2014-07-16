package com.github.sixro.vodafoneitalymavenplugin.util;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.github.sixro.vodafoneitalymavenplugin.util.Word;
import com.github.sixro.vodafoneitalymavenplugin.util.Word.NoSuchDocPropertyException;

public class WordTest {

	private static final File OUTPUT_DIR = new File("target/vodafonecanvass"); //System.getProperty("java.io.tmpdir");
	
	private Word word;
	private String propertyName;
	private File temporaryFile;

	@Before public void setup() throws IOException {
		if (! OUTPUT_DIR.exists())
			OUTPUT_DIR.mkdirs();
		
		word = new Word(WordTest.class.getResourceAsStream("/example.docx"));
		propertyName = "myproperty";
		temporaryFile = new File(OUTPUT_DIR, "example_changed.docx");
	}
	
	@Test public void setTextualDocProperty_change_expected_property() throws NoSuchDocPropertyException, IOException {
		assertEquals("Hello World", word.getTextualDocProperty(propertyName));
		
		word.setTextualDocProperty(propertyName, "Bye bye");
		word.save(temporaryFile);
		
		assertEquals("Bye bye", new Word(temporaryFile).getTextualDocProperty(propertyName));
	}

	@Test public void replaceText_produces_a_doc_that_will_not_contains_that_text_anymore() throws NoSuchDocPropertyException, IOException {
		String placeholder = "my.property";
		assertTrue(word.containsText(placeholder));
		
		word.replaceText(placeholder, "Bill Gates");
		word.save(temporaryFile);
		
		assertFalse(new Word(temporaryFile).containsText(placeholder));
	}

	@Test public void replaceText_act_inside_tables_too() throws NoSuchDocPropertyException, IOException {
		String placeholder = "table.property";
		assertTrue(word.containsText(placeholder));
		
		word.replaceText(placeholder, "Ali Baba");
		word.save(temporaryFile);
		
		assertFalse(new Word(temporaryFile).containsText(placeholder));
	}

}
