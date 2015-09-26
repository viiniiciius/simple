package br.com.vitulus.simple.jdbc.template;

import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public interface DataTemplate {

	public DataTemplate addData(String columnName,Object value);
	public DataTemplate addData(SqlColumn column,Object value);
	public DataTemplate addData(SqlColumn column,SqlExpression value);
	
}