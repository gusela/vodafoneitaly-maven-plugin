package com.github.sixro.vodafoneitalymavenplugin;

import java.io.*;
import java.util.*;

import org.apache.poi.*;
import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.junit.Test;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

public class WordDocsLearningTest {

	@Test public void test() throws IOException {
		XWPFDocument word = new XWPFDocument(WordDocsLearningTest.class.getResourceAsStream("/RN.docx"));
//		System.out.println("isEnforcedUpdateFields " + word.isEnforcedUpdateFields());
		word.enforceUpdateFields();
	    
//		insertAtBookmark(word, "listOfSoftwareComponents", new String[]{ "MD-MERLINO-V1.0.2-20140608.xls", "another-excel.xls" });
		
		// NOTE: this way to print fields does not run!!!
		//printFields(word);
		
		POIXMLProperties documentProperties = word.getProperties();
		CustomProperties customProperties = documentProperties.getCustomProperties();
		CTProperties properties = customProperties.getUnderlyingProperties();
		List<CTProperty> propertyList = properties.getPropertyList();
		for (CTProperty prop : propertyList) {
			if (prop.getName().equalsIgnoreCase("_dversion")) {
				prop.setLpwstr("test");
			}
		}
		documentProperties.commit();
	    
		//File file = new File("src/test/resources/myword.docx");
	    File file = new File("target/myword.docx");
	    if (file.exists()) file.delete();
	    FileOutputStream os = new FileOutputStream(file);
	    word.write(os);
	    os.close();
	}

	@SuppressWarnings("unused")
	private void printFields(XWPFDocument word) {
		for (XWPFParagraph p : word.getParagraphs()) {
			List<CTSimpleField> fields = p.getCTP().getFldSimpleList();
			for (CTSimpleField f : fields) {
				System.out.println(f);
			}
		}
	}

	public final void insertAtBookmark(XWPFDocument document, String bookmarkName, String[] paragraphs) {
        List<XWPFParagraph> paraList = null;
        Iterator<XWPFParagraph> paraIter = null;
        XWPFParagraph para = null;
        List<CTBookmark> bookmarkList = null;
        Iterator<CTBookmark> bookmarkIter = null;
        CTBookmark bookmark = null;
       
        paraList = document.getParagraphs();
        paraIter = paraList.iterator();
           
        while(paraIter.hasNext()) {
            para = paraIter.next();
               
            bookmarkList = para.getCTP().getBookmarkStartList();
            bookmarkIter = bookmarkList.iterator();
               
            while(bookmarkIter.hasNext()) {
                bookmark = bookmarkIter.next();
                if(bookmark.getName().equals(bookmarkName)) {
                    createParagraphs(para, paragraphs);
                    return;
                }
            }
        }
    } 
	
	private void createParagraphs(XWPFParagraph p, String[] paragraphs) {
	    if (p != null) {
	        XWPFDocument doc = p.getDocument();
	        XmlCursor cursor = p.getCTP().newCursor();
	        for (int i = 0; i < paragraphs.length; i++) {
	            XWPFParagraph newP = doc.createParagraph();
	            newP.getCTP().setPPr(p.getCTP().getPPr());
	            XWPFRun newR = newP.createRun();
	            newR.getCTR().setRPr(p.getRuns().get(0).getCTR().getRPr());
	            newR.setText(paragraphs[i]);
	            XmlCursor c2 = newP.getCTP().newCursor();
	            c2.moveXml(cursor);
	            c2.dispose();
	        }
	        cursor.removeXml(); // Removes replacement text paragraph
	        cursor.dispose();
	    }
	}

}
