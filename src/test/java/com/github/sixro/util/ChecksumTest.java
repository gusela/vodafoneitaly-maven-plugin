package com.github.sixro.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class ChecksumTest {

	@Test public void returns_expected_checksum() {
		assertEquals("1220704766", Checksum.valueOf(new File("src/test/resources/softwares/pippo.bin")));
		assertEquals("12738659", Checksum.valueOf(new File("src/test/resources/softwares/pluto.zip")));
	}

}
