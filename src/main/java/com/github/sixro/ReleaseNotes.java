package com.github.sixro;

import java.io.*;
import java.util.Properties;

import com.github.sixro.util.Word;

public class ReleaseNotes {

	private Word word;

	public ReleaseNotes(Word word) {
		this.word = word;
	}

	public void save(File file) {
		try {
			word.save(file);
		} catch (IOException e) {
			throw new RuntimeException("unable to save release notes", e);
		}
	}

	public void replaceAll(Properties properties) {
		for (String propertyName : properties.stringPropertyNames())
			word.replaceText("${" + propertyName + "}", properties.getProperty(propertyName));
	}

}
