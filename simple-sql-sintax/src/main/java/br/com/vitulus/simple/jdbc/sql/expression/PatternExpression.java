package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;

public class PatternExpression implements SqlExpression{

	public static final String ESCAPE = "ESCAPE";
	
	private BinaryOperator 	operator;
	private boolean			not;
	private SqlExpression	left;
	private SqlExpression	right;
	private SqlExpression	escape;
	
	public PatternExpression(SqlExpression left,SqlExpression right) {
		this.left = left;
		this.right = right;
		setLike();
	}
	
	public void setLike(){
		operator = BinaryOperator.LIKE;
	}
	
	public void setGlob(){
		operator = BinaryOperator.GLOB;
	}
	
	public void setRegexp(){
		operator = BinaryOperator.REGEXSP;
	}
	
	public void setMatch(){
		operator = BinaryOperator.MATCH;
	}
	
	public void setNot(boolean not){
		this.not = not;
	}
	
	public void setEscape(SqlExpression escape){
		this.escape = escape;
	}
	
	public SqlExpression getEscape() {
		return escape;
	}
	
	public SqlExpression getLeft() {
		return left;
	}
	
	public SqlExpression getRight() {
		return right;
	}
	
	public BinaryOperator getOperator() {
		return operator;
	}
	
	public boolean isNot() {
		return not;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		left.append(p);
		p.append(" ");
		if(isNot()){
			UnaryPrefixOperator.NOT.append(p);
			p.append(" ");
		}		
		operator.append(p);
		p.append(" ");
		right.append(p);
		if(escape != null){
			p.append(" ");
			p.append(ESCAPE);
			p.append(" ");
			escape.append(p);
		}
	}

}