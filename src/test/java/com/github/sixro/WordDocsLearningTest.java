package com.github.sixro;

import java.io.*;
import java.util.*;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;

public class WordDocsLearningTest {

	@Test public void test() throws IOException {
		XWPFDocument word = new XWPFDocument(WordDocsLearningTest.class.getResourceAsStream("/RN.docx"));
	    
		insertAtBookmark(word, "listOfSoftwareComponents", new String[]{ "MD-MERLINO-V1.0.2-20140608.xls", "another-excel.xls" });
	    
	    File file = new File("src/test/resources/myword.docx");
	    if (file.exists()) file.delete();
	    FileOutputStream os = new FileOutputStream(file);
	    word.write(os);
	    os.close();
	}

	public final void insertAtBookmark(XWPFDocument document, String bookmarkName, String[] paragraphs) {
        List<XWPFParagraph> paraList = null;
        Iterator<XWPFParagraph> paraIter = null;
        XWPFParagraph para = null;
        List<CTBookmark> bookmarkList = null;
        Iterator<CTBookmark> bookmarkIter = null;
        CTBookmark bookmark = null;
        XWPFRun run = null;
       
        paraList = document.getParagraphs();
        paraIter = paraList.iterator();
           
        while(paraIter.hasNext()) {
            para = paraIter.next();
               
            bookmarkList = para.getCTP().getBookmarkStartList();
            bookmarkIter = bookmarkList.iterator();
               
            while(bookmarkIter.hasNext()) {
                bookmark = bookmarkIter.next();
                if(bookmark.getName().equals(bookmarkName)) {
                    run = para.createRun();
                    
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
