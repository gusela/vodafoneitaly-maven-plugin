package com.github.sixro;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.*;

public class NamingRules {

	private NamingRules() { }
	
	public static String standardFileName(String unversionedFileName, String system, String version, String date) {
		String basename = FilenameUtils.getBaseName(unversionedFileName);
		int underscoreIndex = basename.indexOf('_');
		String prefix = (underscoreIndex < 0)
				? basename + '-' + system
				: basename.substring(0, underscoreIndex) + '-' + system + basename.substring(underscoreIndex);
		return new StringBuilder(prefix)
			.append("-V")
			.append(version)
			.append('-')
			.append(date)
			.append('.')
			.append(FilenameUtils.getExtension(unversionedFileName))
			.toString();
	}

	public static String standardFileName(String unversionedFileName, String system, String version, LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		String dateAsText = formatter.print(date);
		return standardFileName(unversionedFileName, system, version, dateAsText);
	}

	public static String md5filename(String system, String version, LocalDate date, String releasePhase) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		String dateAsText = formatter.print(date);
		return new StringBuilder("CS-")
			.append(system)
			.append("_Kit")
			.append(releasePhase)
			.append('-')
			.append(dateAsText)
			.append("-v.")
			.append(version)
			.append(".md5")
			.toString();
	}

}
