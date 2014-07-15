package com.github.sixro.util;

import java.io.File;

public class ExtendedFileUtils {

	private ExtendedFileUtils() { }

	public static String getRelativePath(File file, File directory) {
		String relativePath = file.getPath().substring((int) directory.getPath().length());
		if (relativePath.startsWith("/") || relativePath.startsWith("\\"))
			relativePath = relativePath.substring(1);
		return relativePath;
	}
	
}
