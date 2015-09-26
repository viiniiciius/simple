package br.com.vitulus.simple.jdbc.dao;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteDatabase extends RelationalSqlDatabase{

	private File path;
	
	protected SqliteDatabase(String schema,File path) {
		DatabaseContext context = new DatabaseContext();
		context.setProperty("class", getClass());
		context.setProperty("schema", schema);
		context.setProperty("path", path);
		initialize(context);
	}
	
	protected SqliteDatabase(String schema) {
		this(schema,null);		
	}	

	protected SqliteDatabase() {
	}
	
	@Override
	public SqliteDatabase initialize(DatabaseContext context) {
		super.initialize(context);
		this.path = context.getProperty("path");
		return this;
	}
	
	public int getVersion() {
		try {
			ResultSet rs = rawQuery("PRAGMA schema_version");
			if(!rs.next())
				throw new IllegalStateException("PRAGMA Version should never be empty.");
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	
	public void setVersion(int version){
		try {
			execSQL("PRAGMA schema_version="+version);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void clearDatabaseLeakTransactions(){
		try {
			getConnection().rollback();
		} catch (SQLException e) {}
	}

	public File getPath() {
		return path;
	}
	
	
}