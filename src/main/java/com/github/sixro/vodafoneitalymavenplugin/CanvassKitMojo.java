package com.github.sixro.vodafoneitalymavenplugin;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.io.*;
import org.apache.commons.lang3.StringUtils;
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
 * @goal canvass-kit
 * @phase package
 */
public class CanvassKitMojo extends AbstractMojo {

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
	 * @parameter expression="${vodafoneitaly.canvass.sgst}"
	 * @required
	 */
	private String sgst;

	/**
	 * The system.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.system}"
	 * @required
	 */
	private String system;

	/**
	 * The version.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.version}"
	 * @required
	 */
	private String version;

	/**
	 * The canvass date in format {@code yyyyMMdd}.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.date}"
	 * @required
	 */
	private String date;

	/**
	 * The release phase (e.g. CutOff, Eccezione1, Eccezione2, etc...).
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.releasePhase}"
	 * @required
	 */
	private String releasePhase;

	/**
	 * Location of the directory of the kit.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.sourceDirectory}" default-value="src/main/kit"
	 */
	private File kitDirectory;
	
	// FIXME portarsi dietro i template di default
	/**
	 * SQ template.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.sq.template}" default-value="src/main/templates/SQ.xls"
	 */
	private File sqTemplate;
	
	/**
	 * Docs sub-directory.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.docsSubdirectory}" default-value="KitForOperations/${vodafoneitaly.canvass.system}/DOCS/Delivery"
	 */
	private String docsSubdirectory;

	/**
	 * MD template.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.md.template}" default-value="src/main/templates/MD.xls"
	 */
	private File mdTemplate;

	/**
	 * Softwares sub-directory.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.softwaresSubdirectory}" default-value="KitForOperations/${vodafoneitaly.canvass.system}/SOFTWARE"
	 */
	private String softwaresSubdirectory;

	/**
	 * Location of the working directory where the kit is assembled.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.workingDirectory}" default-value="${project.build.directory}/vodafoneitaly/canvass-kit-exploded"
	 */
	private File workingDirectory;

	/**
	 * Location of the outputdirectory.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.outputDirectory}" default-value="${project.build.directory}/vodafoneitaly/canvass-kit"
	 */
	private File outputDirectory;

	/**
	 * Target filename.
	 * 
	 * @parameter expression="${vodafoneitaly.canvass.kit.targetFileName}" default-value="${project.build.finalName}.zip"
	 */
	private String targetFileName;

	public void execute() throws MojoExecutionException {
		Properties projectProperties = mavenProject.getProperties();
		fillPropertiesWithThatOfPlugin(projectProperties);
		printProperties(projectProperties);
		
		createDirectory(workingDirectory);
		createDirectory(outputDirectory);

		File docsDirectory = new File(workingDirectory, projectProperties.getProperty("vodafoneitaly.canvass.kit.docsSubdirectory"));
		createDirectory(docsDirectory);
		getLog().info("    " + StringUtils.rightPad("docsDirectory ", 50, '.') + ": " + docsDirectory);

		File softwaresDirectory = new File(kitDirectory, projectProperties.getProperty("vodafoneitaly.canvass.kit.softwaresSubdirectory"));
		createDirectory(softwaresDirectory);
		getLog().info("    " + StringUtils.rightPad("softwaresDirectory ", 50, '.') + ": " + softwaresDirectory);

		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyyMMdd");
		LocalDate localDate = LocalDate.parse(date, parser);
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		projectProperties.setProperty("vodafoneitaly.canvass.dateIso", formatter.print(localDate));
		
		try {
			generateAllSQ(kitDirectory, docsDirectory, sqTemplate, version, localDate);
			generateMD(softwaresDirectory, docsDirectory, mdTemplate, sgst, system, version, localDate);
			copyAllKitFiles(kitDirectory, workingDirectory, system, version, localDate, projectProperties);
			createMD5(workingDirectory, system, version, localDate, releasePhase);
			
			File targetFile = new File(outputDirectory, targetFileName);
			projectProperties.setProperty("vodafoneitaly.canvass.kit.targetFile", targetFile.getPath());
			compressAllFiles(workingDirectory, targetFile);
			
			getLog().info("created kit: " + targetFile);
		} catch (IOException e) {
			throw new RuntimeException("unable to create kit", e);
		}
	}

	private void printProperties(Properties projectProperties) {
		List<String> propertyNames = new LinkedList<String>(projectProperties.stringPropertyNames());
		Collections.sort(propertyNames);
		for (String propertyName : propertyNames)
			getLog().debug("    " + StringUtils.rightPad(propertyName + ' ', 50, '.') + ": " + projectProperties.getProperty(propertyName));
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
			createDirectory(outputFileDirectory);
			
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

	protected void compressAllFiles(File directory, File zipFile) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
		zip.setMethod(ZipOutputStream.DEFLATED);
		Collection<File> files = ExtendedFileUtils.listFilesRecursive(directory);
		for (File file : files) {
			ZipEntry zipEntry = new ZipEntry(ExtendedFileUtils.getRelativePath(file, directory));
			zip.putNextEntry(zipEntry);

			FileInputStream fileIn = new FileInputStream(file);
			IOUtils.copy(fileIn, zip);
			fileIn.close();
			
			zip.closeEntry();
		}

		zip.close();
	}

	protected boolean hasToBeRenamed(File file) {
		String baseName = FilenameUtils.getBaseName(file.getName());
		return ! StringUtils.containsAny(baseName, "0123456789");
	}

	private void fillPropertiesWithThatOfPlugin(Properties properties) {
		addIfNotFound(properties, "vodafoneitaly.canvass.sgst",                      sgst);
		addIfNotFound(properties, "vodafoneitaly.canvass.system",                    system);
		addIfNotFound(properties, "vodafoneitaly.canvass.version",                   version);
		addIfNotFound(properties, "vodafoneitaly.canvass.date",                      date);
		addIfNotFound(properties, "vodafoneitaly.canvass.releasePhase",              releasePhase);
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.sourceDirectory",       kitDirectory.getPath());
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.sq.template",           sqTemplate.getPath());
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.docsSubdirectory",      docsSubdirectory);
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.md.template",           mdTemplate.getPath());
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.softwaresSubdirectory", softwaresSubdirectory);
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.workingDirectory",      workingDirectory.getPath());
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.outputDirectory",       outputDirectory.getPath());
		addIfNotFound(properties, "vodafoneitaly.canvass.kit.targetFileName",        targetFileName);
	}

	private void addIfNotFound(Properties properties, String propertyName, String propertyValue) {
		String value = properties.getProperty(propertyName);
		if (value == null)
			properties.setProperty(propertyName, propertyValue);
	}

	private void createDirectory(File dir) {
		if (!dir.exists())
			dir.mkdirs();
	}

}
