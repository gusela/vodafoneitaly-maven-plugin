package com.github.sixro.util;

import java.io.*;
import java.util.List;

import org.apache.poi.xwpf.usermodel.*;
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
		if (!word.isEnforcedUpdateFields())
			word.enforceUpdateFields();

		CTProperty property = findProperty(propertyName);
		property.setLpwstr(text);
	}

	public void replaceText(String text, String replacement) {
		List<XWPFHeader> headerList = word.getHeaderList();
		for (XWPFHeader header : headerList)
			replaceAll(header.getParagraphs(), text, replacement);

		List<XWPFParagraph> paragraphs = word.getParagraphs();
		replaceAll(paragraphs, text, replacement);
	}

	public boolean containsText(String text) {
		List<XWPFHeader> headerList = word.getHeaderList();
		for (XWPFHeader header : headerList) {
			if (containsText(header.getParagraphs(), text))
				return true;
		}
		
		return containsText(word.getParagraphs(), text);
	}

	public void save(File file) throws IOException {
		FileOutputStream outpusStream = new FileOutputStream(file);
		word.write(outpusStream);
		outpusStream.close();
	}

	private void replaceAll(List<XWPFParagraph> paragraphs, String text, String replacement) {
		for (XWPFParagraph paragraph : paragraphs) {
			for (XWPFRun r : paragraph.getRuns()) {
				String paragraphText = r.getText(r.getTextPosition());
				if (paragraphText != null && paragraphText.contains(text)) {
					paragraphText = paragraphText.replaceAll(text, replacement);
					r.setText(paragraphText, 0);
				}
			}
		}
	}
	
	private boolean containsText(List<XWPFParagraph> paragraphs, String text) {
		for (XWPFParagraph paragraph : paragraphs) {
			for (XWPFRun r : paragraph.getRuns()) {
				String paragraphText = r.getText(r.getTextPosition());
				if (paragraphText != null && paragraphText.contains(text)) {
					return true;
				}
			}
		}
		return false;
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

}
