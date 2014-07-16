package com.github.sixro.vodafoneitalymavenplugin.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.github.sixro.vodafoneitalymavenplugin.util.Checksum.Cksum;

public class CksumTest {

	@Test public void returns_expected_checksum() {
		assertEquals("1220704766", Cksum.valueOf(new File("src/test/resources/softwares/pippo.bin")));
		assertEquals("12738659", Cksum.valueOf(new File("src/test/resources/softwares/pluto.zip")));
	}

}
