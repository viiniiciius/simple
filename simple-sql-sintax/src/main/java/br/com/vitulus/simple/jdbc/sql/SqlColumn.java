package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

public class SqlColumn implements SqlElement{

	private SqlTable 				owner;
	private String 					name;
	
	public SqlColumn() {
	}	
	
	public SqlColumn(SqlTable owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	public SqlTable getOwner() {
		return owner;
	}
	public void setOwner(SqlTable owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String label) {
		this.name = label;
	}
	@Override
	public void append(Appendable p) throws IOException {
		if(owner != null){
			owner.append(p);
			p.append(".");			
		}
		p.append(getName());
	}
	
}