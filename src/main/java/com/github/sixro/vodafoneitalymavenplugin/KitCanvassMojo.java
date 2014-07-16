package com.github.sixro.vodafoneitalymavenplugin;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;
import org.javatuples.Pair;
import org.joda.time.LocalDate;
import org.joda.time.format.*;

import com.github.sixro.vodafoneitalymavenplugin.util.Checksum.MD5;
import com.github.sixro.vodafoneitalymavenplugin.util.*;

/**
 * Goal which generate a kit for canvass.
 * 
 * @goal kitcanvass
 * @phase package
 */
public class KitCanvassMojo extends AbstractMojo {

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
	 * The release phase (e.g. CutOff, Eccezione1, Eccezione2, etc...).
	 * 
	 * @parameter expression="${vodafonecanvass.releasePhase}"
	 * @required
	 */
	private String releasePhase;

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
			createMD5(outputDirectory, system, version, localDate, releasePhase);
		} catch (IOException e) {
			throw new RuntimeException("unable to create kit", e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void generateAllSQ(File kitDirectory, File outputDirectory, File sqTemplate, String version, LocalDate date) throws IOException {
		Map<Pair<String, String>, List<SQL>> systemsAndDatabasesToSQLs = new LinkedHashMap<Pair<String,String>, List<SQL>>();
		Collection<File> files = FileUtils.listFiles(kitDirectory, new String[]{ "sql" }, true);
		for (File file : files) {
			SQL sql = new SQL(file);
			Pair<String,String> systemAndDatabase = sql.systemAndDatabase();
			List<SQL> sqls = systemsAndDatabasesToSQLs.get(systemAndDatabase);
			if (sqls == null) {
				sqls = new LinkedList<SQL>();
				systemsAndDatabasesToSQLs.put(systemAndDatabase, sqls);
			}
			
			sqls.add(sql);
		}
		
		for (Pair<String, String> systemAndDatabase : systemsAndDatabasesToSQLs.keySet()) {
			SQ sq = new SQ(sqTemplate, systemAndDatabase.getValue0(), systemAndDatabase.getValue1(), version, date);
			List<SQL> sqls = systemsAndDatabasesToSQLs.get(systemAndDatabase);
			for (SQL sql : sqls)
				sq.addSQL(sql);
			File sqFile = sq.saveTo(outputDirectory);
			
			getLog().info("generated SQ file '" + sqFile + "'");
		}
	}

	protected File generateMD(File softwareDirectory, File docsOutputDirectory, File mdTemplate, String sgst, String system, String version, LocalDate date) throws IOException {
		Collection<File> softwares = ExtendedFileUtils.listFiles(softwareDirectory);
		
		MD md = new MD(mdTemplate, sgst, system, version, date);
		for (File software : softwares) md.addSoftware(software);
		return md.saveTo(docsOutputDirectory);
	}

	protected void copyAllKitFiles(File kitDirectory, File outputDirectory, String system, String version, LocalDate date, Properties properties) throws IOException {
		Collection<File> files = ExtendedFileUtils.listFilesRecursive(kitDirectory);
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

	/**
	 * Create an MD5 file reading all files in specified directory and putting MD5 in the same specified dir.
	 * @param directory directory
	 * @return an MD5 file
	 * @throws IOException 
	 */
	protected File createMD5(File directory, String system, String version, LocalDate date, String releasePhase) throws IOException {
		File md5file = new File(directory, NamingRules.md5filename(system, version, date, releasePhase));
		createMD5(directory, md5file);
		return md5file;
	}
	
	protected void createMD5(File directory, File md5File) throws IOException {
		Collection<File> files = ExtendedFileUtils.listFilesRecursive(directory);
		PrintWriter writer = new PrintWriter(md5File);
		for (File file : files) {
			String md5checksum = MD5.valueOf(file);
			String line = new StringBuilder(md5checksum)
				.append(' ')
				.append(ExtendedFileUtils.getRelativePath(file, directory))
				.toString();
			writer.println(line);
		}
		writer.close();
	}

	protected boolean hasToBeRenamed(File file) {
		String baseName = FilenameUtils.getBaseName(file.getName());
		return ! StringUtils.containsAny(baseName, "0123456789");
	}

	private Properties enhanceProjectProperties() {
		Properties properties = mavenProject.getProperties();
		Properties enhancedProperties = new Properties(properties);
		enhancedProperties.setProperty("vodafonecanvass.system", system);
		enhancedProperties.setProperty("vodafonecanvass.version", version);
		enhancedProperties.setProperty("vodafonecanvass.date", date);
		return enhancedProperties;
	}

}
