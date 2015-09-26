package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;

public class BetweenExpression implements SqlExpression{

	public static final String BETWEEN = "BETWEEN";
	
	private SqlExpression from;
	private SqlExpression left;
	private SqlExpression right;
	private boolean       not;
	
	public BetweenExpression(SqlExpression from,SqlExpression left,SqlExpression right) {
		this.from = from;
		this.left = left;
		this.right = right;
	}

	public void setNot(boolean not) {
		this.not = not;
	}
	
	public SqlExpression getFrom() {
		return from;
	}
	
	public SqlExpression getLeft() {
		return left;
	}
	
	public SqlExpression getRight() {
		return right;
	}
	
	public boolean isNot() {
		return not;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		from.append(p);
		p.append(" ");
		if(isNot()){
			UnaryPrefixOperator.NOT.append(p);
			p.append(" ");
		}
		p.append(BETWEEN);
		p.append(" ");
		left.append(p);
		p.append(" ");
		BinaryOperator.AND.append(p);
		p.append(" ");
		right.append(p);
	}

}