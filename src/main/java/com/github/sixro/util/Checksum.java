package com.github.sixro.util;

import java.io.*;

import org.apache.commons.lang3.StringUtils;

public class Checksum {

	// TODO verificare se non sia un duplicato di FileUtils.checksum...
	
	public static String valueOf(File file) {
		try {
			Process process = exec("cksum " + file.getPath());
			return firstPart(firstLineOutput(process));
		} catch (InterruptedException e) {
			throw new RuntimeException("unable to calculate cksum of file " + file, e);
		} catch (IOException e) {
			throw new RuntimeException("unable to calculate cksum of file " + file, e);
		}
	}

	private static Process exec(String string) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(string);
		process.waitFor();
		return process;
	}

	private static String firstPart(String line) {
		return StringUtils.split(line, ' ')[0];
	}

	private static String firstLineOutput(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = reader.readLine();
		reader.close();
		return line;
	}
	
}
