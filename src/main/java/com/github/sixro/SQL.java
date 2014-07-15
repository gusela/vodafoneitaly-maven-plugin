package com.github.sixro;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class SQL {

	private List<String> lines;

	@SuppressWarnings("unchecked")
	public SQL(File file) throws IOException {
		lines = FileUtils.readLines(file);
	}

	public void save(File outputFile) throws IOException {
		FileUtils.writeLines(outputFile, lines, "\r\n");
	}

}
