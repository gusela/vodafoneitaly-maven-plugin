package com.github.sixro;

import java.io.*;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.github.sixro.util.Word;

public class ReleaseNotesTest {

	@Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{ 
		setImposteriser(ClassImposteriser.INSTANCE);
	}};
	private Word word = context.mock(Word.class);
	private ReleaseNotes releaseNotes = new ReleaseNotes(word);
	
	@Test public void save_delegate_to_word_save() throws IOException {
		final File file = new File("ignore");

		context.checking(new Expectations() {{ 
			oneOf(word).save(file);
		}});
		
		releaseNotes.save(file);
	}

	@Test public void replaceAll_replace_each_property_using_a_placeholder() {
		final Properties properties = new Properties();
		properties.setProperty("my.property", "Hello World");
		
		context.checking(new Expectations() {{ 
			oneOf(word).replaceText("${my.property}", "Hello World");
		}});
		
		releaseNotes.replaceAll(properties);
	}
	
}
