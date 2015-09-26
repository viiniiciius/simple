package br.com.vitulus.simple.jdbc.sql.statement;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.DMLStatement;
import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.SqlUpdateData;
import br.com.vitulus.simple.jdbc.sql.SqlWhereClausule;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class UpdateStatement extends AbstractSqlStatement implements DMLStatement {

	public static final String UPDATE 	= "UPDATE";
	public static final String SET 		= "SET";
	
	private SqlTable 						table;
	private SqlUpdateData					data;
	private SqlWhereClausule 				where;
	
	public UpdateStatement(SqlTable table) {
		this.table = table;
		this.data = new SqlUpdateData();
	}

	public UpdateStatement addDataValue(SqlColumn column, SqlExpression value){
		return this;		
	}
	
	public void setWhere(SqlWhereClausule where) {
		this.where = where;
	}
	
	public SqlWhereClausule getWhere() {
		return where;
	}
	
	public SqlUpdateData getData() {
		return data;
	}
	
	public SqlTable getTable() {
		return table;
	}

	public void setTable(SqlTable table) {
		this.table = table;
	}	
	
}