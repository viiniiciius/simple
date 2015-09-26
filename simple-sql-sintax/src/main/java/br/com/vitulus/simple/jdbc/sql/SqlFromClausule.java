package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

public class SqlFromClausule implements SqlElement{

	public static final String FROM = "FROM";
	
	private SqlJoinSource source;
	
	public SqlFromClausule(SqlJoinSource source) {
		this.source = source;
	}
	
	public SqlFromClausule(String tableName) {
		this(new SqlJoinSource(tableName));
	}
	
	public SqlFromClausule(SqlTable table){
		this(new SqlJoinSource(table));
	}
	
	public SqlJoinSource getSource() {
		return source;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		p.append(FROM);
		p.append(" ");
		source.append(p);
	}

}