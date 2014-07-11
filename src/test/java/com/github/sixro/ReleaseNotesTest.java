package com.github.sixro;

import java.io.*;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.github.sixro.Word.NoSuchDocPropertyException;

public class ReleaseNotesTest {

	@Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{ 
		setImposteriser(ClassImposteriser.INSTANCE);
	}};
	private Word word = context.mock(Word.class);
	private ReleaseNotes releaseNotes = new ReleaseNotes(word);
	
	@Test public void setSystem_updateDocProperty_underscoredprj() throws NoSuchDocPropertyException {
		context.checking(new Expectations() {{ 
			oneOf(word).setTextualDocProperty("_dprj", "Boh");
		}});
		
		releaseNotes.setSystem("Boh");
	}

	@Test public void save_delegate_to_word_save() throws IOException {
		final File file = new File("ignore");

		context.checking(new Expectations() {{ 
			oneOf(word).save(file);
		}});
		
		releaseNotes.save(file);
	}

}
