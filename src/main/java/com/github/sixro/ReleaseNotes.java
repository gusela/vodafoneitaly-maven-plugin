package com.github.sixro;

import java.io.*;

import com.github.sixro.util.Word;
import com.github.sixro.util.Word.NoSuchDocPropertyException;

public class ReleaseNotes {

	private Word word;

	public ReleaseNotes(Word word) {
		this.word = word;
	}

	public void setSystem(String systemName) {
		try {
			word.setTextualDocProperty("_dprj", systemName);
		} catch (NoSuchDocPropertyException e) {
			throw new RuntimeException("unable to update 'system' in release notes word document", e);
		}
	}

	public void save(File file) {
		try {
			word.save(file);
		} catch (IOException e) {
			throw new RuntimeException("unable to save release notes", e);
		}
	}

}
