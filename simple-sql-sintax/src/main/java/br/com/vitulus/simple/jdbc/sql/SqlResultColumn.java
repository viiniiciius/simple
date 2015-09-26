package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;

import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class SqlResultColumn implements SqlElement{
	
	private ResultProjection projection;
	
	public SqlResultColumn() {
		this.projection = new AllProjection();
	}
	
	public SqlResultColumn(SqlTable tableAll) {
	}
	
	public SqlResultColumn(SqlExpression expression,String alias){
		this.projection = new ExpressionProjection(expression,alias);
	}
	
	public ResultProjection getProjection() {
		return projection;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		this.projection.append(p);
	}
	
	private interface ResultProjection extends SqlElement{};

	protected class AllProjection implements ResultProjection{
		@Override
		public void append(Appendable p) {
		}		
	}
	
	protected class TableAllProjection implements ResultProjection{		
		private SqlTable table;		
		public TableAllProjection(SqlTable table) {
			this.table = table;
		}		
		@Override
		public void append(Appendable p) throws IOException {
			if(table.getAlias() != null){
				p.append(table.getAlias());
			}else{
				table.append(p);
			}
			p.append(".*");
		}
	}
	
	protected class ExpressionProjection implements ResultProjection{
		private SqlExpression 	expression;
		private String 			alias;
		public ExpressionProjection(SqlExpression expression,String alias) {
			this.expression = expression;
			this.alias = alias;
		}
		@Override
		public void append(Appendable p) throws IOException {
			expression.append(p);
			if(alias != null){
				p.append(" AS ");
				p.append(alias);
			}
		}
	}
	
}