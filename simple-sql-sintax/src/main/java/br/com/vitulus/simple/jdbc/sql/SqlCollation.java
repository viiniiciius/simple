package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression.UnaryPostFixOperator;

public class SqlCollation implements SqlElement{

	private static final SqlExpression.UnaryPostFixOperator operator = UnaryPostFixOperator.COLLATE;
	
	private String collationName;
	
	public SqlCollation(String collationName) {
		this.collationName = collationName;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		operator.append(p);
		p.append(" ");
		p.append(collationName);
	}

	
	
}