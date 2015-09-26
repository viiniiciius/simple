package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;


public class UnaryExpression implements SqlExpression{

	private UnaryPrefixOperator operator;
	private SqlExpression right;
	
	public UnaryExpression(UnaryPrefixOperator operator, SqlExpression right) {
		this.operator = operator;
		this.right = right;
	}
	
	public SqlExpression getRight() {
		return right;
	}

	public UnaryPrefixOperator getOperator() {
		return operator;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		operator.append(p);
		right.append(p);
	}

}