package br.com.vitulus.simple.jdbc.template;

import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.SqlWhereClausule;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.statement.DeleteStatement;

public class DeleteTemplate extends AbstractTemplate{
	
	private DeleteStatement 	statement;
	private ExpressionBuilder 	whereBuilder;
	
	DeleteTemplate(String tableName) {
		this(new SqlTable(tableName));
	}
	
	DeleteTemplate(SqlTable table) {
		this();
		setTable(table);
	}
	
	DeleteTemplate() {
		this.statement = new DeleteStatement();
		this.whereBuilder = newExpressionBuilder();
		this.statement.setWhere(new SqlWhereClausule(null));
		this.whereBuilder.addListener(new ExpressionBuilderListener(){
			@Override
			public void onExpressionChange(SqlExpression old, SqlExpression now) {
				statement.getWhere().setExpression(now);
			}			
		});
	}
	
	public DeleteTemplate setWhere(SqlWhereClausule where){
		this.statement.setWhere(where);
		return this;
	}
	
	public SqlWhereClausule getWhere(){
		return this.statement.getWhere();
	}
	
	public DeleteTemplate setTable(SqlTable table){
		this.statement.setTable(table);
		return this;
	}
	
	public ExpressionBuilder getWhereBuilder() {
		return whereBuilder;
	}
	
	public StringBuilder getSql(){
		StringBuilder sql = new StringBuilder();
		try{
			sql.append(DeleteStatement.DELETE_FROM);
			sql.append(" ");
			statement.getTable().append(sql);
			sql.append(" ");
			statement.getWhere().append(sql);			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return sql;
	}
	
	public DeleteStatement getStatement() {
		return statement;
	}
	
	public static void main(String[] args) {
		SqlTable table = new SqlTable("modelo");
		SqlColumn column = new SqlColumn(table,"id");
		DeleteTemplate template = new DeleteTemplate(table);
		template.getWhereBuilder()
		.appendEqual(column,5)
		.AND()
		.appendGreaterThan(column, 10)
		.AND(true)
		.appendLessEqual(column, "vinicius")
		.OR(true)
		.appendPosFixed(column, "joao");
		
		PreparedSql query = template.buildSql();		
		System.out.println(query.getSql());
		System.out.println(query.getValues());
	}
	
}