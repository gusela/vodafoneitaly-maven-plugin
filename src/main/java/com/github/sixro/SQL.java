package com.github.sixro;

import java.io.*;
import java.util.List;
import java.util.regex.*;

import org.apache.commons.io.*;
import org.joda.time.LocalDate;
import org.joda.time.format.*;

public class SQL {
	
	private static final String METADATA_VERSION = "VERSION";
	private static final String METADATA_DATABASE = "DATABASE";

	public static final Pattern METADATA_REGEX = Pattern.compile("(.+):(.*)");
	public static final Pattern VERSION_REGEX = Pattern.compile("([0-9\\.]+)\\s*-\\s*([0-9/]+)\\s*(.*)");

	private final File file;
	private final List<String> lines;

	@SuppressWarnings("unchecked")
	public SQL(File file) throws IOException {
		this.file = file;
		this.lines = FileUtils.readLines(file);
	}

	public String getFilename() {
		return file.getName();
	}

	public void save(File outputFile) throws IOException {
		FileUtils.writeLines(outputFile, lines, "\r\n");
	}

	public void updateFirstRowAsRequiredByDBA() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		String firstLine = new StringBuilder("REM $Id$ ")
			.append(FilenameUtils.getBaseName(file.getName()))
			.append(",v ")
			.append(version())
			.append(" ")
			.append(formatter.print(updateDate()))
			.append(" 00:00:00 ")
			.append(database())
			.append(" Exp $")
			.toString();
		lines.set(0, firstLine);
	}

	public String version() {
		try {
			Matcher matcher = VERSION_REGEX.matcher(metadata(METADATA_VERSION));
			return (matcher.matches()) ? matcher.group(1).trim() : null;
		} catch (Throwable t) {
			throw new RuntimeException("unexpected error trying to find version in SQL " + file, t);
		}
	}

	public LocalDate updateDate() {
		try {
			Matcher matcher = VERSION_REGEX.matcher(metadata(METADATA_VERSION));
			String dateAsText = (matcher.matches()) ? matcher.group(2).trim() : null;
			DateTimeFormatter parser = DateTimeFormat.forPattern("dd/MM/yyyy");
			return parser.parseLocalDate(dateAsText);
		} catch (Throwable t) {
			throw new RuntimeException("unexpected error trying to find update date in SQL " + file, t);
		}
	}

	public String database() {
		return metadata(METADATA_DATABASE);
	}

	public String sgst() {
		return metadata("SG/ST");
	}

	public String schema() {
		return metadata("SCHEMA");
	}

	public String author() {
		return metadata("AUTHOR");
	}

	public String description() {
		return metadata("DESCRIPTION");
	}

	public String firstLine() {
		return lines.get(0);
	}

	public String metadata(String metadataName) {
		for (String line : lines) {
			Matcher matcher = METADATA_REGEX.matcher(line);
			if (matcher.matches()) {
				String currentMetadata = matcher.group(1).trim();
				if (currentMetadata.equalsIgnoreCase(metadataName))
					return matcher.group(2).trim();
			}
		}
		
		throw new NoSuchMetadataException("Unable to find metadata '" + metadataName + "'");
	}

	@SuppressWarnings("serial")
	public static class NoSuchMetadataException extends RuntimeException {

		public NoSuchMetadataException(String message) {
			super(message);
		}

	}

}
