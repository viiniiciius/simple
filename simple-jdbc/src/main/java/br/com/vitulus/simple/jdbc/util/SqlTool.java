package br.com.vitulus.simple.jdbc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

public class SqlTool {

	private static final Pattern comments = Pattern.compile("/\\*.*\\*/[;\\s\r\n]*");
	private static final Pattern constraint = Pattern.compile("CONSTRAINT \".+?\" ", Pattern.CASE_INSENSITIVE);
	private static final Pattern primaryKey = Pattern.compile("(PRIMARY KEY {1,})(\\()(.*)(\\))", Pattern.CASE_INSENSITIVE);
	private static final Pattern doubleComma = Pattern.compile(",,");
	private static final Pattern bracketComma = Pattern.compile("\\(,");
	private static final Pattern emptyPrimaryKey = Pattern.compile("PRIMARY KEY  \\(\\),?", Pattern.CASE_INSENSITIVE);
	private static final Pattern aloneKey = Pattern.compile("  KEY .*,\r\n", Pattern.CASE_INSENSITIVE);
	private static final Pattern blackLine = Pattern.compile("  \r\n");
	private static final Pattern commaBlankLine = Pattern.compile(",\r\n\\);");
	private static final Pattern intType = Pattern.compile(" int\\([0-9]+\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern tinyintType = Pattern.compile(" tinyint\\([0-9]+\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern varcharType = Pattern.compile(" varchar\\([0-9]+\\)", Pattern.CASE_INSENSITIVE);
	@SuppressWarnings("unused")
	private static final Pattern trigger = Pattern.compile(" ON.*ACTION", Pattern.CASE_INSENSITIVE);
	private static final Pattern autoIncrement = Pattern.compile("auto_increment", Pattern.CASE_INSENSITIVE);
	
	
	public static String mysql2SqliteDDL(String ddl) throws Exception {		
		ddl = comments.matcher(ddl).replaceAll("");		
		ddl = constraint.matcher(ddl).replaceAll("");
		ddl = primaryKey.matcher(ddl).replaceAll("UNIQUE$2$3$4");
		ddl = doubleComma.matcher(ddl).replaceAll(",");
		ddl = bracketComma.matcher(ddl).replaceAll("(");
		ddl = emptyPrimaryKey.matcher(ddl).replaceAll("");
		ddl = aloneKey.matcher(ddl).replaceAll("");
		ddl = blackLine.matcher(ddl).replaceAll("");
		ddl = commaBlankLine.matcher(ddl).replaceAll("\r\n);");
		ddl = intType.matcher(ddl).replaceAll(" INTEGER");
		ddl = tinyintType.matcher(ddl).replaceAll(" INTEGER");
		ddl = varcharType.matcher(ddl).replaceAll(" TEXT");
		//ddl = trigger.matcher(ddl).replaceAll(" ");
		ddl = autoIncrement.matcher(ddl).replaceAll("PRIMARY KEY autoincrement");
		return ddl;		
	}
	
	public static String mysql2SqliteDDL(File mysqlDDLFile) throws Exception {
		StringBuilder stb = new StringBuilder();
		BufferedReader r = new BufferedReader(new FileReader(mysqlDDLFile));
		String line;
		while((line = r.readLine()) != null){
			stb.append(line).append("\r\n");			
		}
		r.close();
		String ddl = stb.toString();
		return mysql2SqliteDDL(ddl);
	}

	public static void main(String[] args) {
		try {
			String sqliteDDL = mysql2SqliteDDL(new File("ddl/comerce.db.ddl_mysql.sql"));
			System.out.println(sqliteDDL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}