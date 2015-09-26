package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;

import br.com.vitulus.simple.jdbc.sql.SqlElement;

public interface SqlExpression extends SqlElement{

	public enum BinaryOperator implements SqlElement{
		NONE(""),
		CONCATENET("||"),
		MULTIPLIE("*"),DIVIDE("/"),MOD("%"),
		PLUS("+"),MINUS("-"),
		GGT(">>"),LLT("<<"),
		EQUALITY("="), LT("<"), LE("<="),GT(">"), GE(">="), NE("<>"),EQ("="),
		IS("IS"),IS_NOT("IS NOT"),IN("IN"),LIKE("LIKE"),MATCH("MATCH"),GLOB("GLOB"),REGEXSP("REGEXSP"),
		AND("AND"),	OR("OR");
		
		private String symbol;
		
		private BinaryOperator(String symbol) {
			this.symbol = symbol;
		}
		
		public String getSymbol() {
			return symbol;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append(getSymbol());
		}		
	}
	
	public enum UnaryPrefixOperator implements SqlElement{
		MINUS("-"),
		PLUS("+"),
		TILDE("~"),
		NOT("NOT");

		private String symbol;
		
		private UnaryPrefixOperator(String symbol) {
			this.symbol = symbol;
		}
		
		public String getSymbol() {
			return symbol;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append(getSymbol());
		}		
	}
	
	public enum UnaryPostFixOperator implements SqlElement{
		COLLATE("COLLATE");
		
		private String symbol;

		private UnaryPostFixOperator(String symbol) {
			this.symbol = symbol;
		}
		
		public String getSymbol() {
			return symbol;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append(getSymbol());
		}
	}
	
}