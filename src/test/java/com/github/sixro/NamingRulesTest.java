package com.github.sixro;

import static org.junit.Assert.*;

import org.junit.*;

public class NamingRulesTest {

	@Test public void standardFileName_returns_expected_name() {
		assertEquals("RN-Merlino-V1.0.0-20140720.doc", NamingRules.standardFileName("RN.doc", "Merlino", "1.0.0", "20140720"));
		assertEquals("RN-Merlino-V1.1.0-20140720.doc", NamingRules.standardFileName("RN.doc", "Merlino", "1.1.0", "20140720"));
	}

}
