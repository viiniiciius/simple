package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

public class SqlTable implements SqlElement,Cloneable{

	private SqlDatabase database;
	private String		alias;
	private String	  	name;
	
	public SqlTable(SqlDatabase database, String name) {
		this.database 	= database;
		this.name 		= name;
	}	

	public SqlTable(String name) {
		this(null,name);
	}
	
	public SqlDatabase getDatabase() {
		return database;
	}
	public void setDatabase(SqlDatabase database) {
		this.database = database;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public void append(Appendable p) throws IOException {
		if(getDatabase() != null){
			getDatabase().append(p);
			p.append(".");
		}
	    p.append(getName());
	}
	
	@Override
	public SqlTable clone(){
		try {
			return (SqlTable)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		SqlTable o = new SqlTable(new SqlDatabase("db"),"vinicius");
		System.out.println(o.getDatabase());
		System.out.println(o.clone().getDatabase());
	}
	
}