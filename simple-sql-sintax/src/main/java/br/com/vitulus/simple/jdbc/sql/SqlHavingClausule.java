package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class SqlHavingClausule implements SqlElement {
	
	public static final String HAVING = "HAVING";
	
	private SqlExpression expression;
	
	public SqlHavingClausule(SqlExpression expression) {
		this.expression = expression;
	}
	
	public SqlExpression getExpression() {
		return expression;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		if(expression != null){
			p.append(HAVING);
			expression.append(p);
		}
	}

}