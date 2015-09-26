package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;

public class BracketsExpression implements SqlExpression{

	private SqlExpression expression;
	
	public BracketsExpression() {
	}
	
	public BracketsExpression(SqlExpression expression) {
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
		p.append("(");
		expression.append(p);
		p.append(")");
	}

}