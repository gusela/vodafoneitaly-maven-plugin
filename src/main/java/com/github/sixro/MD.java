package com.github.sixro;

import java.io.*;
import java.util.*;

import org.joda.time.*;

import com.github.sixro.util.*;

public class MD {

	private static final String COLUMN_SGST = System.getProperty("vodafonecanvass.md.column.sgst", "C");
	private static final String COLUMN_SYSTEM = System.getProperty("vodafonecanvass.md.column.system", "D");
	private static final String COLUMN_MODULE = System.getProperty("vodafonecanvass.md.column.module", "E");
	private static final String COLUMN_TYPE = System.getProperty("vodafonecanvass.md.column.type", "F");
	private static final String COLUMN_CHECKSUM = System.getProperty("vodafonecanvass.md.column.checksum", "H");

	private static final int SOFTWARES_START_ROW = Integer.getInteger("vodafonecanvass.md.softwares.startRow", 9);

	private final File mdTemplate;
	private final String sgst;
	private final String system;
	private final String version;
	private final LocalDate date;
	
	private final List<File> softwares;

	public MD(File mdTemplate, String sgst, String system, String version, LocalDate date) {
		this.mdTemplate = mdTemplate;
		this.sgst = sgst;
		this.system = system;
		this.version = version;
		this.date = date;
		
		this.softwares = new LinkedList<File>();
	}

	public void addSoftware(File file) {
		this.softwares.add(file);
	}

	public File saveTo(File outputDirectory) throws IOException {
		File file = new File(outputDirectory, NamingRules.standardFileName(mdTemplate.getName(), system, version, date));
		
		Excel excel = new Excel(mdTemplate);
		excel.setCellByName("date", date.toLocalDateTime(new LocalTime(0, 0, 0, 0)));
		
		int count = 0;
		for (File software : softwares) {
			int row = count +SOFTWARES_START_ROW;
			
			excel.setCellByRef(COLUMN_SGST + row, sgst);
			excel.setCellByRef(COLUMN_SYSTEM + row, system);
			String softwareFilename = software.getName();
			excel.setCellByRef(COLUMN_MODULE + row, softwareFilename);
			String softwareExtension = softwareFilename.substring(softwareFilename.lastIndexOf('.') +1);
			excel.setCellByRef(COLUMN_TYPE + row, softwareExtension);
			excel.setCellByRef(COLUMN_CHECKSUM + row, Checksum.valueOf(software));
			
			count++;
		}
		excel.save(file);
		return file;
	}

}
