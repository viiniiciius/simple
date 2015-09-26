package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;


public final class SqlWhereClausule implements SqlElement {

	public static final String WHERE  = "WHERE";	
	
	private SqlExpression expression;
	
	public SqlWhereClausule(SqlExpression expression) {
		this.expression = expression;
	}
	
	public SqlExpression getExpression() {
		return expression;
	}

	public void setExpression(SqlExpression expression) {
		this.expression = expression;
	}

	@Override
	public void append(Appendable p) throws IOException {
		if(expression != null){
			p.append(WHERE);
			p.append(" ");
			expression.append(p);
		}
	}

}