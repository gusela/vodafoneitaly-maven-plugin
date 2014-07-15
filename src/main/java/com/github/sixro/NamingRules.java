package com.github.sixro;

import org.joda.time.LocalDate;
import org.joda.time.format.*;

public class NamingRules {

	private NamingRules() { }
	
	public static String standardFileName(String unversionedFileName, String system, String version, String date) {
		int dotIndex = unversionedFileName.lastIndexOf('.');
		return new StringBuilder(unversionedFileName.substring(0, dotIndex))
			.append('-')
			.append(system)
			.append("-V")
			.append(version)
			.append('-')
			.append(date)
			.append(unversionedFileName.substring(dotIndex))
			.toString();
	}

	public static String standardFileName(String unversionedFileName, String system, String version, LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		int dotIndex = unversionedFileName.lastIndexOf('.');
		return new StringBuilder(unversionedFileName.substring(0, dotIndex))
			.append('-')
			.append(system)
			.append("-V")
			.append(version)
			.append('-')
			.append(formatter.print(date))
			.append(unversionedFileName.substring(dotIndex))
			.toString();
	}

}
