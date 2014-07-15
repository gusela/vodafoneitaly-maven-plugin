package com.github.sixro;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;

public class MD {

	private final File mdTemplate;
	private final String system;
	private final String version;
	private final LocalDate date;

	public MD(File mdTemplate, String system, String version, LocalDate date) {
		this.mdTemplate = mdTemplate;
		this.system = system;
		this.version = version;
		this.date = date;
	}

	public File saveTo(File outputDirectory) throws IOException {
		File file = new File(outputDirectory, NamingRules.standardFileName(mdTemplate.getName(), system, version, date));
		FileUtils.copyFile(mdTemplate, file);
		return file;
	}

}
