package com.github.sixro;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.*;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;
import org.joda.time.*;
import org.joda.time.format.*;

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
	 * @parameter expression="${vodafonecanvass.output.directory}" default-value="${project.build.directory}/vodafonecanvass"
	 */
	private File outputDirectory;

	/**
	 * Location of the directory of the kit.
	 * 
	 * @parameter expression="${vodafonecanvass.kit.directory}" default-value="src/main/kit"
	 */
	private File kitDirectory;
	
	/**
	 * Release notes template.
	 * 
	 * <p>
	 * It is a word document where some parts has to be changed. E.g. version,
	 * system the list of files released, etc...
	 * </p>
	 * 
	 * @parameter expression="${vodafonecanvass.releasenotes.template}" default-value="src/main/templates/RN.docx"
	 */
	private File releaseNotesTemplate;
	
	/**
	 * SQ template.
	 * 
	 * @parameter expression="${vodafonecanvass.sq.template}" default-value="src/main/templates/SQ.xls"
	 */
	private File sqTemplate;
	
	/**
	 * Docs sub-directory.
	 * 
	 * @parameter expression="${vodafonecanvass.docs.subdirectory}" default-value="KitForOperations/${vodafonecanvass.system}/DOCS/Delivery"
	 */
	private String docsSubdirectory;

	/**
	 * MD template.
	 * 
	 * @parameter expression="${vodafonecanvass.md.template}" default-value="src/main/templates/MD.xls"
	 */
	private File mdTemplate;

	/**
	 * Softwares sub-directory.
	 * 
	 * @parameter expression="${vodafonecanvass.softwares.subdirectory}" default-value="KitForOperations/${vodafonecanvass.system}/SOFTWARE"
	 */
	private String softwaresSubdirectory;

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException {
		getLog().info("Packaging for Vodafone Canvass");
		getLog().info("  Release Notes Template .......: " + releaseNotesTemplate);
		getLog().info("  Kit directory ................: " + kitDirectory);
		getLog().info("  Output directory .............: " + outputDirectory);

		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		Properties enhancedProperties = enhanceProjectProperties();
		
		// * generate MD
		// * copy all files
		//     * if they are MS Word replace all enhanced properties
		//     * if they are MS Excel replace all enhanced properties
		//     * if they are SQL transform in DOS mode and replace the first row looking for information within the file

		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyyMMdd");
		LocalDate localDate = LocalDate.parse(date, parser);
		
		String realDocsSubdirectory = StrSubstitutor.replace(docsSubdirectory, enhancedProperties);
		File docsOutputDirectory = new File(outputDirectory, realDocsSubdirectory);
		if (! docsOutputDirectory.exists())
			docsOutputDirectory.mkdirs();

		String realSoftwaresSubdirectory = StrSubstitutor.replace(softwaresSubdirectory, enhancedProperties);
		File softwaresDirectory = new File(kitDirectory, realSoftwaresSubdirectory);

		try {
			generateAllSQ(kitDirectory, docsOutputDirectory, sqTemplate, version, localDate);
		} catch (IOException e) {
			throw new RuntimeException("unable to generate all SQ files", e);
		}
		
		try {
			generateMD(softwaresDirectory, docsOutputDirectory, mdTemplate, system, version, localDate);
		} catch (IOException e) {
			throw new RuntimeException("unable to generate all SQ files", e);
		}

		Collection<File> files = FileUtils.listFiles(kitDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			String path = file.getPath();
			String subpath = path.substring(kitDirectory.getPath().length());
			if (subpath.startsWith("/") || subpath.startsWith("\\"))
				subpath = subpath.substring(1);
			
			File outputFile = new File(outputDirectory, subpath);
			File outputDir = outputFile.getParentFile();
			String outputFilename = outputFile.getName();
			int indexOfDot = outputFilename.lastIndexOf('.');
			String filename = outputFilename.substring(0, indexOfDot);
			String extension = outputFilename.substring(indexOfDot +1);
			if (!extension.equalsIgnoreCase("sql") && ! StringUtils.containsAny(filename, "0123456789"))
				outputFilename = NamingRules.standardFileName(outputFilename, system, version, localDate);
			outputFile = new File(outputDir, outputFilename);
			
			System.out.println("  copying " + subpath + " to " + outputFile);
		}
		
		File releaseNotesFile = new File(outputDirectory, NamingRules.standardFileName(releaseNotesTemplate.getName(), system, version, localDate));
		if (releaseNotesFile.exists())
			delete(releaseNotesFile);

		ReleaseNotes releaseNotes = new ReleaseNotes(openWord(releaseNotesTemplate));
		releaseNotes.replaceAll(enhancedProperties);
		releaseNotes.save(releaseNotesFile);
	}

	protected void generateAllSQ(File kitDirectory, File outputDirectory, File sqTemplate, String version, LocalDate date) throws IOException {
		Map<SqlMetadataForSQ, List<File>> metadata2files = newMapOfMetadata2files(kitDirectory);

		for (SqlMetadataForSQ metadata : metadata2files.keySet()) {
			SQ sq = new SQ(sqTemplate, metadata.system, metadata.database, version, date);
			List<File> sqls = metadata2files.get(metadata);
			for (File sql : sqls)
				sq.addSQL(sql);
			File sqFile = sq.saveTo(outputDirectory);
			
			getLog().info("generated SQ file '" + sqFile + "'");
		}
	}

	protected File generateMD(File softwareDirectory, File docsOutputDirectory, File mdTemplate, String system, String version, LocalDate date) throws IOException {
		//Collection<File> softwares = FileUtils.listFiles(softwareDirectory, TrueFileFilter.INSTANCE, null);
		
		MD md = new MD(mdTemplate, system, version, date);
		File mdFile = md.saveTo(docsOutputDirectory);
		return mdFile;
	}

	@SuppressWarnings("unchecked")
	private Map<SqlMetadataForSQ, List<File>> newMapOfMetadata2files(File kitDirectory) {
		Collection<File> files = FileUtils.listFiles(kitDirectory, new String[]{ "sql" }, true);
		Map<SqlMetadataForSQ, List<File>> metadata2files = new HashMap<PackageMojo.SqlMetadataForSQ, List<File>>();
		for (File file : files) {
			getLog().info("... found SQL file '" + file + "'...");
			
			SqlMetadataForSQ metadata = findSqlMetadataForSQ(file);
			List<File> list = metadata2files.get(metadata);
			if (list == null) {
				list = new LinkedList<File>();
				metadata2files.put(metadata, list);
			}
			list.add(file);
		}
		
		getLog().info("map of metadata 2 SQL files: " + metadata2files);
		return metadata2files;
	}

	@SuppressWarnings("unchecked")
	private SqlMetadataForSQ findSqlMetadataForSQ(File file) {
		try {
			List<String> lines = FileUtils.readLines(file);
			String system = null;
			String database = null;
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty()) continue;
				
				if (line.startsWith("SYSTEM")) {
					system = line.substring(line.indexOf(':') +1).trim();
				}
				if (line.startsWith("DATABASE")) {
					database = line.substring(line.indexOf(':') +1).trim();
				}
			}
			if (StringUtils.isBlank(system) || StringUtils.isBlank(database))
				throw new IllegalStateException("Unable to process SQL " + file + ": SYSTEM or DATABASE headers are not valued!");
			
			SqlMetadataForSQ metadata = new SqlMetadataForSQ(system, database);
			return metadata;
		} catch (IOException e) {
			throw new RuntimeException("unable to read SQL file " + file, e);
		}
	}

	private Properties enhanceProjectProperties() {
		Properties properties = mavenProject.getProperties();
		Properties enhancedProperties = new Properties(properties);
		enhancedProperties.setProperty("vodafonecanvass.system", system);
		enhancedProperties.setProperty("vodafonecanvass.version", version);
		enhancedProperties.setProperty("vodafonecanvass.date", date);
		return enhancedProperties;
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

	private static class SqlMetadataForSQ {
		
		public final String system;
		public final String database;

		public SqlMetadataForSQ(String system, String database) {
			super();
			this.system = system;
			this.database = database;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
		
	}
	
}
