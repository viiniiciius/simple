package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

public class SqlDatabase implements SqlElement{

	private String schema;
	private String name;

	public SqlDatabase(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema.trim();
	}

	@Override
	public void append(Appendable p) throws IOException {
		if(schema != null && !schema.isEmpty()){
			p.append(".");
			p.append(schema);
		}
	    p.append(getName());
	}	
	
}