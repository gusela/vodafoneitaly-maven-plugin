package com.github.sixro;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.*;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;
import org.joda.time.*;
import org.joda.time.format.*;

import com.github.sixro.util.*;

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
	 * The SG/ST related to this release.
	 * 
	 * <p>
	 * PAY ATTENTION: in SQL files there are a redundant information, but SQL scripts could be related to more than one SG/ST.
	 * Here we need this information in order to generate MD document where SG/ST appear as the first column of each file.
	 * </p>
	 * 
	 * @parameter expression="${vodafonecanvass.sgst}"
	 * @required
	 */
	private String sgst;

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

	public void execute() throws MojoExecutionException {
		getLog().info("Packaging for Vodafone Canvass");
		getLog().info("  Release Notes Template .......: " + releaseNotesTemplate);
		getLog().info("  Kit directory ................: " + kitDirectory);
		getLog().info("  Output directory .............: " + outputDirectory);

		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		Properties enhancedProperties = enhanceProjectProperties();
		
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
			generateMD(softwaresDirectory, docsOutputDirectory, mdTemplate, sgst, system, version, localDate);
			copyAllKitFiles(kitDirectory, outputDirectory, system, version, localDate, enhancedProperties);
		} catch (IOException e) {
			throw new RuntimeException("unable to create kit", e);
		}
	}

	protected void generateAllSQ(File kitDirectory, File outputDirectory, File sqTemplate, String version, LocalDate date) throws IOException {
		Map<SqlMetadataForSQ, List<File>> metadata2files = newMapOfMetadata2files(kitDirectory);

		for (SqlMetadataForSQ metadata : metadata2files.keySet()) {
			SQ sq = new SQ(sqTemplate, metadata.system, metadata.database, version, date);
			List<File> files = metadata2files.get(metadata);
			for (File file : files)
				sq.addSQL(new SQL(file));
			File sqFile = sq.saveTo(outputDirectory);
			
			getLog().info("generated SQ file '" + sqFile + "'");
		}
	}

	@SuppressWarnings("unchecked")
	protected File generateMD(File softwareDirectory, File docsOutputDirectory, File mdTemplate, String sgst, String system, String version, LocalDate date) throws IOException {
		Collection<File> softwares = FileUtils.listFiles(softwareDirectory, TrueFileFilter.INSTANCE, null);
		
		MD md = new MD(mdTemplate, sgst, system, version, date);
		for (File software : softwares) md.addSoftware(software);
		return md.saveTo(docsOutputDirectory);
	}

	@SuppressWarnings("unchecked")
	protected void copyAllKitFiles(File kitDirectory, File outputDirectory, String system, String version, LocalDate date, Properties properties) throws IOException {
		Collection<File> files = FileUtils.listFiles(kitDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			String filename = file.getName();
			String extension = FilenameUtils.getExtension(filename);
			String relativePath = ExtendedFileUtils.getRelativePath(file, kitDirectory);
			File outputFile = new File(FilenameUtils.concat(outputDirectory.getPath(), relativePath));
			if (hasToBeRenamed(file))
				outputFile = new File(outputFile.getParentFile(), NamingRules.standardFileName(filename, system, version, date));
			
			File outputFileDirectory = outputFile.getParentFile();
			if (! outputFileDirectory.exists())
				outputFileDirectory.mkdirs();
			
			if (extension.equalsIgnoreCase("docx")) {
				Word word = new Word(file);
				for (String property : properties.stringPropertyNames())
					word.replaceText(property, properties.getProperty(property));
				word.save(outputFile);
			} else if (extension.equalsIgnoreCase("sql")) {
				SQL sql = new SQL(file);
				sql.updateFirstRowAsRequiredByDBA();
				sql.save(outputFile);
			} else {
				FileUtils.copyFile(file, outputFile);
			}
		}
	}

	protected boolean hasToBeRenamed(File file) {
		String baseName = FilenameUtils.getBaseName(file.getName());
		return ! StringUtils.containsAny(baseName, "0123456789");
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

	// TODO questa classe potrebbe esser un MainMetadata e il reperimento potrebbe esser uno static della classe SQ dato che per fare il look up di SYSTEM e DATABASE potrebbe esser usato il metodo interno
	public static class SqlMetadataForSQ {
		
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
