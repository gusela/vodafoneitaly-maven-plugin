package com.github.sixro.vodafoneitalymavenplugin;

import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.junit.*;

import com.github.sixro.vodafoneitalymavenplugin.NamingRules;

public class NamingRulesTest {

	@Test public void standardFileName_returns_expected_name() {
		assertEquals("RN-Merlino-V1.0.0-20140720.doc", NamingRules.standardFileName("RN.doc", "Merlino", "1.0.0", "20140720"));
		assertEquals("HO-Merlino_IAT-V1.2.0-20140716.docx", NamingRules.standardFileName("HO_IAT.docx", "Merlino", "1.2.0", LocalDate.parse("2014-07-16")));
	}

	@Test public void md5filename_returns_expected_name() {
		assertEquals("CS-Merlino_KitEccezione2-20140716-v.1.2.0.md5", NamingRules.md5filename("Merlino", "1.2.0", LocalDate.parse("2014-07-16"), "Eccezione2"));
	}
	
}
