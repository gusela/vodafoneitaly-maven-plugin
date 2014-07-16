package com.github.sixro.vodafoneitalymavenplugin.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.github.sixro.vodafoneitalymavenplugin.util.Checksum.MD5;

public class MD5Test extends MD5 {

	@Test public void returns_expected_MD5() {
		assertEquals("c01227d9f30928dae60bbcd7009f3488", MD5.valueOf(new File("src/test/resources/myCustomKit/RN.docx")));
	}

}
