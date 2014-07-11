package com.github.sixro;

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

}
