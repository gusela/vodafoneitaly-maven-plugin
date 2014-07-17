package com.github.sixro.vodafoneitalymavenplugin;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.joda.time.*;
import org.joda.time.format.*;

import com.github.sixro.vodafoneitalymavenplugin.util.Excel;

public class SQ {

	private static final String COLUMN_SGST = System.getProperty("vodafonecanvass.sq.column.sgst", "C");
	private static final String COLUMN_DATABASE = System.getProperty("vodafonecanvass.sq.column.database", "D");
	private static final String COLUMN_SCRIPT_NAME = System.getProperty("vodafonecanvass.sq.column.scriptName", "E");
	private static final String COLUMN_VERSION = System.getProperty("vodafonecanvass.sq.column.version", "F");
	private static final String COLUMN_SCRIPT_TYPE = System.getProperty("vodafonecanvass.sq.column.scriptType", "H");
	private static final String COLUMN_SCHEMA = System.getProperty("vodafonecanvass.sq.column.schema", "I");
	private static final String COLUMN_STORAGE = System.getProperty("vodafonecanvass.sq.column.storage", "J");
	private static final String COLUMN_REFERRER = System.getProperty("vodafonecanvass.sq.column.referrer", "K");
	private static final String COLUMN_DURATION = System.getProperty("vodafonecanvass.sq.column.duration", "L");
	private static final String COLUMN_DESCRIPTION = System.getProperty("vodafonecanvass.sq.column.description", "M");

	private static final int SCRIPTS_START_ROW = Integer.getInteger("vodafonecanvass.sq.scripts.startRow", 10);

	private static final String TYPE_SQL = "SQL";
	private static final String NO_STORAGE = "no";
	private static final String DEFAULT_DURATION = System.getProperty("vodafonecanvass.sq.default.duration", "1 minuto");

	private static final LocalTime MIDNIGHT = new LocalTime(0, 0, 0, 0);

	private static final Comparator<SQL> BY_FILENAME = new Comparator<SQL>() {
		@Override
		public int compare(SQL o1, SQL o2) {
			return o1.filename().compareTo(o2.filename());
		}
	};

	private final File template;
	private final String system;
	private final String database;
	private final String version;
	private final LocalDate date;
	
	private final List<SQL> sqls;

	public SQ(File template, String system, String database, String version, LocalDate date) {
		super();
		this.template = template;
		this.system = system;
		this.database = database;
		this.version = version;
		this.date = date;

		this.sqls = new LinkedList<SQL>();
	}

	public void addSQL(SQL sql) {
		sqls.add(sql);
	}
	
	public File saveTo(File directory) throws IOException {
		File output = new File(directory, filename());

		Excel excel = new Excel(template);
		excel.setCellByName("date", date.toLocalDateTime(MIDNIGHT));
		int count = 0;
		
		Collections.sort(sqls, BY_FILENAME);
		
		for (SQL sql : sqls) {
			int row = count + SCRIPTS_START_ROW;
			excel.setCellByRef(COLUMN_SGST + row, sql.sgst());
			excel.setCellByRef(COLUMN_DATABASE + row, database);
			excel.setCellByRef(COLUMN_SCRIPT_NAME + row, FilenameUtils.getBaseName(sql.filename()));

			String version = sql.version();
			excel.setCellByRef(COLUMN_VERSION + row, version);
			excel.setCellByRef(COLUMN_SCRIPT_TYPE + row, TYPE_SQL);
			excel.setCellByRef(COLUMN_SCHEMA + row, sql.schema());
			excel.setCellByRef(COLUMN_STORAGE + row, NO_STORAGE);
			
			String authorMetadataValue = sql.author();
			String author = authorMetadataValue.substring(0, authorMetadataValue.indexOf("(")).trim();
			excel.setCellByRef(COLUMN_REFERRER + row, author);
			
			String descriptionMetadataValue = sql.description();
			int durationStartIndex = descriptionMetadataValue.indexOf('[');
			String descriptionInSQL = descriptionMetadataValue;
			String duration = DEFAULT_DURATION;
			if (durationStartIndex >= 0) {
				descriptionInSQL = descriptionMetadataValue.substring(0, durationStartIndex).trim();
				duration = descriptionMetadataValue.substring(durationStartIndex +1, descriptionMetadataValue.indexOf(']')).trim();
			}
			
			excel.setCellByRef(COLUMN_DURATION + row, duration);
			excel.setCellByRef(COLUMN_DESCRIPTION + row, descriptionInSQL);
			
			count++;
		}
		
		excel.save(output);
		
		return output;
	}

	private String filename() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");		
		String filename = new StringBuilder("SQ-")
			.append(system)
			.append("_")
			.append(database)
			.append("-V")
			.append(version)
			.append("-")
			.append(formatter.print(date))
			.append(".xls")
			.toString();
		return filename;
	}
	
}
