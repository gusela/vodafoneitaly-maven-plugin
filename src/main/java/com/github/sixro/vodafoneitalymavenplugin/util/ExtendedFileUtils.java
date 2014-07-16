package com.github.sixro.vodafoneitalymavenplugin.util;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class ExtendedFileUtils {

	private ExtendedFileUtils() { }

	public static String getRelativePath(File file, File directory) {
		String relativePath = file.getPath().substring((int) directory.getPath().length());
		if (relativePath.startsWith("/") || relativePath.startsWith("\\"))
			relativePath = relativePath.substring(1);
		return relativePath;
	}

	@SuppressWarnings("unchecked")
	public static Collection<File> listFiles(File directory) {
		return FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, null);
	}

	@SuppressWarnings("unchecked")
	public static Collection<File> listFilesRecursive(File directory) {
		return FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	}
	
}
