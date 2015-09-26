package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;

public class BinaryExpression implements SqlExpression{

	private SqlExpression 		left;
	private BinaryOperator 		operator;
	private SqlExpression 		right;	
	
	public BinaryExpression(SqlExpression left, BinaryOperator operator, SqlExpression right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public SqlExpression getLeft() {
		return left;
	}

	public void setLeft(SqlExpression left) {
		this.left = left;
	}

	public BinaryOperator getOperator() {
		return operator;
	}

	public void setOperator(BinaryOperator operator) {
		this.operator = operator;
	}

	public SqlExpression getRight() {
		return right;
	}

	public void setRight(SqlExpression right) {
		this.right = right;
	}

	@Override
	public void append(Appendable p) throws IOException {
		left.append(p);
		if(right != null){
			p.append(" ");
			operator.append(p);
			p.append(" ");
			right.append(p);	
		}
	}

}