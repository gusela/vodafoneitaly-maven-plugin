package com.github.sixro.vodafoneitalymavenplugin.util;

import java.io.*;
import java.util.List;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

/**
 * Represents a MS Word document.
 * 
 * <p>
 * This object is able to replace <i>docProperties</i> of MS Word but pay attention because this will ask to the user a confirmation to
 * updated fields on document opening.
 * </p>
 * 
 * @author <a href="mailto:me@sixro.net" >sixro</a> 
 */
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
		replaceHeader(text, replacement);
		replaceFooter(text, replacement);
		replaceBody(text, replacement);
	}

	public boolean containsText(String text) {
		List<XWPFHeader> headers = word.getHeaderList();
		for (XWPFHeader header : headers) {
			if (tablesContainText(header.getTables(), text))
				return true;
			if (containsText(header.getParagraphs(), text))
				return true;
		}
		
		List<XWPFFooter> footers = word.getFooterList();
		for (XWPFFooter footer: footers) {
			if (tablesContainText(footer.getTables(), text))
				return true;
			if (containsText(footer.getParagraphs(), text))
				return true;
		}
		
		if (tablesContainText(word.getTables(), text))
			return true;
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

	private void replaceBody(String text, String replacement) {
		replaceAllInTables(word.getTables(), text, replacement);
		replaceAll(word.getParagraphs(), text, replacement);
	}
	
	private void replaceFooter(String text, String replacement) {
		List<XWPFFooter> footers = word.getFooterList();
		for (XWPFFooter footer: footers) {
			replaceAllInTables(footer.getTables(), text, replacement);
			replaceAll(footer.getParagraphs(), text, replacement);
		}
	}
	
	private void replaceHeader(String text, String replacement) {
		List<XWPFHeader> headers = word.getHeaderList();
		for (XWPFHeader header : headers) {
			replaceAllInTables(header.getTables(), text, replacement);
			replaceAll(header.getParagraphs(), text, replacement);
		}
	}
	
	private void replaceAllInTables(List<XWPFTable> tables, String text, String replacement) {
		for (XWPFTable table : tables) {
			List<XWPFTableRow> rows = table.getRows();
			for (XWPFTableRow row : rows) {
				List<XWPFTableCell> cells = row.getTableCells();
				for (XWPFTableCell cell : cells)
					replaceAll(cell.getParagraphs(),  text,  replacement);
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
	
	private boolean tablesContainText(List<XWPFTable> tables, String text) {
		for (XWPFTable table : tables) {
			List<XWPFTableRow> rows = table.getRows();
			for (XWPFTableRow row : rows) {
				List<XWPFTableCell> cells = row.getTableCells();
				for (XWPFTableCell cell : cells) {
					if (containsText(cell.getParagraphs(), text))
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
