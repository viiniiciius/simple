package br.com.vitulus.simple.jdbc.sql.statement;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.DMLStatement;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.SqlWhereClausule;

public class DeleteStatement extends AbstractSqlStatement implements DMLStatement{

	public static final String DELETE_FROM = "DELETE FROM";
	
	private SqlTable 					table;
	private SqlWhereClausule 			where;

	public DeleteStatement() {
	}

	public void setWhere(SqlWhereClausule where) {
		this.where = where;
	}
	
	public SqlWhereClausule getWhere() {
		return where;
	}
	
	public SqlTable getTable() {
		return table;
	}
	
	public void setTable(SqlTable table) {
		this.table = table;
	}
	
}