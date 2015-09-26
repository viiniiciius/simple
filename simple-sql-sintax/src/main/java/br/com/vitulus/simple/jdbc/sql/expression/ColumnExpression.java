package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;

import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;

public class ColumnExpression implements SqlExpression{

	private SqlColumn column;
	
	public ColumnExpression(String columnname) {
		this.column = new SqlColumn(null, columnname); // ! Perigoso ! - Avaliar
	}	
	public ColumnExpression(String tablename,String columnname) {
		SqlTable table = new SqlTable(tablename);
		this.column = new SqlColumn(table, columnname);
	}		
	public ColumnExpression(SqlColumn column) {
		this.column = column;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		column.append(p);
	}

}