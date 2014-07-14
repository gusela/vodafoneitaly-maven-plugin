package com.github.sixro;

import java.io.*;
import java.util.Properties;

import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

import com.github.sixro.util.Word;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal package
 * @phase install
 */
public class PackageMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 */
	private MavenProject mavenProject;

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

	public void execute() throws MojoExecutionException {
		getLog().info("Packaging for Vodafone Canvass");
		getLog().info("  Release Notes Template .......: " + releaseNotesTemplate);

		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		File releaseNotesFile = new File(outputDirectory, NamingRules.standardFileName(releaseNotesTemplate.getName(), system, version, date));
		if (releaseNotesFile.exists())
			delete(releaseNotesFile);

		Properties properties = mavenProject.getProperties();
		Properties enhancedProperties = new Properties(properties);
		enhancedProperties.setProperty("vodafonecanvass.system", system);
		enhancedProperties.setProperty("vodafonecanvass.version", version);
		enhancedProperties.setProperty("vodafonecanvass.date", date);
		
		ReleaseNotes releaseNotes = new ReleaseNotes(openWord(releaseNotesTemplate));
		releaseNotes.replaceAll(enhancedProperties);
		releaseNotes.save(releaseNotesFile);
	}

	private Word openWord(File file) {
		try {
			return new Word(file);
		} catch (IOException e) {
			throw new RuntimeException("unable to open word document '" + file + "'", e);
		}
	}

	private void delete(File file) {
		if (!file.delete())
			throw new RuntimeException("unable to delete file " + file);
	}

}
