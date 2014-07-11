package com.github.sixro;

import java.io.*;

import org.apache.commons.io.*;
import org.apache.maven.plugin.*;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal package
 * @phase install
 */
public class PackageMojo extends AbstractMojo {

	/**
	 * The system.
	 * 
	 * @parameter expression="${vodafonecanvass.system}"
	 * @required
	 */
	private String system;

	/**
	 * The version.
	 * 
	 * @parameter expression="${vodafonecanvass.version}"
	 * @required
	 */
	private String version;

	/**
	 * The canvass date in format {@code yyyyMMdd}.
	 * 
	 * @parameter expression="${vodafonecanvass.date}"
	 * @required
	 */
	private String date;

	/**
	 * Location of the output directory.
	 * 
	 * @parameter expression="${project.build.directory}/vodafonecanvass"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Release notes template.
	 * 
	 * <p>
	 * It is a word document where some parts has to be changed. E.g. version,
	 * system the list of files released, etc...
	 * </p>
	 * 
	 * @parameter expression="${vodafonecanvass.releasenotes.template}"
	 * @required
	 */
	private File releaseNotesTemplate;

	private VodafoneCanvass vodafoneCanvass = new VodafoneCanvass();

	public void execute() throws MojoExecutionException {
		getLog().info("Packaging for Vodafone Canvass");
		getLog().info("  Release Notes Template .......: " + releaseNotesTemplate);

		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		File releaseNotes = new File(outputDirectory, vodafoneCanvass.standardFileName(releaseNotesTemplate.getName(), system, version, date));
		if (releaseNotes.exists())
			delete(releaseNotes);
		copy(releaseNotesTemplate, releaseNotes);
	}

	private void copy(File source, File dest) {
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			throw new RuntimeException("unable to copy source file " + source + " to dest file " + dest, e);
		}
	}

	private void delete(File file) {
		if (!file.delete())
			throw new RuntimeException("unable to delete file " + file);
	}

}
