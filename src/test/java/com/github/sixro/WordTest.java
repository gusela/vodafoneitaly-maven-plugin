package com.github.sixro;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.github.sixro.Word.NoSuchDocPropertyException;

public class WordTest {

	private Word word;
	private String propertyName;
	private File temporaryFile;

	@Before public void setup() throws IOException {
		word = new Word(WordTest.class.getResourceAsStream("/example.docx"));
		propertyName = "myproperty";
		temporaryFile = new File(System.getProperty("java.io.tmpdir"), "example_changed.docx");
	}
	
	@Test public void setTextualDocProperty_change_expected_property() throws NoSuchDocPropertyException, IOException {
		assertEquals("Hello World", word.getTextualDocProperty(propertyName));
		
		word.setTextualDocProperty(propertyName, "Bye bye");
		word.save(temporaryFile);
		
		assertEquals("Bye bye", new Word(temporaryFile).getTextualDocProperty(propertyName));
	}

}
