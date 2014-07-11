package com.github.sixro;

import java.io.*;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

public class Word {

	private XWPFDocument word;

	public Word(InputStream inputStream) throws IOException {
		word = new XWPFDocument(inputStream);
	}

	public Word(File file) throws IOException {
		this(new FileInputStream(file));
	}

	public String getTextualDocProperty(String propertyName) throws NoSuchDocPropertyException {
		CTProperty property = findProperty(propertyName);
		return property.getLpwstr();
	}

	public void setTextualDocProperty(String propertyName, String text) throws NoSuchDocPropertyException {
		if (! word.isEnforcedUpdateFields())
			word.enforceUpdateFields();
		
		CTProperty property = findProperty(propertyName);
		property.setLpwstr(text);
	}

	private CTProperty findProperty(String propertyName) throws NoSuchDocPropertyException {
		CTProperty property = null;
		for (CTProperty prop : word.getProperties().getCustomProperties().getUnderlyingProperties().getPropertyList()) {
			if (prop.getName().equalsIgnoreCase(propertyName)) {
				property = prop;
				break;
			}
		}
		
		if (property == null)
			throw new Word.NoSuchDocPropertyException("unable to find a doc property named '" + propertyName + "'");
		return property;
	}

	@SuppressWarnings("serial")
	public class NoSuchDocPropertyException extends Exception {

		public NoSuchDocPropertyException(String message, Throwable cause) {
			super(message, cause);
		}

		public NoSuchDocPropertyException(String message) {
			super(message);
		}

	}

	public void save(File file) throws IOException {
		FileOutputStream outpusStream = new FileOutputStream(file);
		word.write(outpusStream);
		outpusStream.close();
	}

}
