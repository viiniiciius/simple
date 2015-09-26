package br.com.vitulus.simple.jdbc.template;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.SqlWhereClausule;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.statement.UpdateStatement;

public class UpdateTemplate extends AbstractTemplate implements DataTemplate{

	private UpdateStatement 	statement;
	private ExpressionBuilder 	whereBuilder;
	
	UpdateTemplate() {
		this.statement = new UpdateStatement(null);
		this.whereBuilder = newExpressionBuilder();
		this.statement.setWhere(new SqlWhereClausule(null));
		this.whereBuilder.addListener(new ExpressionBuilderListener(){
			@Override
			public void onExpressionChange(SqlExpression old, SqlExpression now) {
				statement.getWhere().setExpression(now);
			}			
		});
	}
	
	UpdateTemplate(SqlTable table) {
		this();
		setTable(table);
	}

	UpdateTemplate(String tableName){
		this(new SqlTable(tableName));
	}
	
	public UpdateTemplate setTable(SqlTable table){
		this.statement.setTable(table);
		return this;
	}
	
	public UpdateTemplate setWhere(SqlWhereClausule where){		
		this.statement.setWhere(where);
		return this;
	}
	
	public UpdateTemplate addData(SqlColumn column,Object value){
		statement.getData().addData(column, getLiteral(value));
		return this;
	}
	
	public UpdateTemplate addData(SqlColumn column,SqlExpression expr){
		statement.getData().addData(column, expr);
		return this;
	}
	
	@Override
	public DataTemplate addData(String columnName, Object value) {
		statement.getData().addData(new SqlColumn(statement.getTable(),columnName), getLiteral(value));
		return this;
	}
	
	public ExpressionBuilder getWhere() {
		return whereBuilder;
	}
	
	@Override
	public AbstractSqlStatement getStatement() {
		return statement;
	}

	@Override
	public CharSequence getSql() {
		StringBuilder sql = new StringBuilder();
		try {
			sql.append(UpdateStatement.UPDATE).append(" ");
			statement.getTable().append(sql);
			sql.append(" ").append(UpdateStatement.SET).append(" ");
			statement.getData().append(sql);
			statement.getWhere().append(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sql;
	}
	
	public static void main(String[] args) {
		SqlTable carro = new SqlTable("carro");
		SqlColumn id = new SqlColumn(carro, "id");
		SqlColumn cor = new SqlColumn(carro, "cor");
		SqlColumn modelo = new SqlColumn(carro, "modelo");
		SqlColumn potencia = new SqlColumn(carro, "potencia");
		
		UpdateTemplate template = new UpdateTemplate(carro);
		template.addData(cor, "branca");
		template.addData(potencia, 1.6);
		template.getWhere()
			.appendEqual(id, 5);
		template.addData(modelo, "Gol Power");
		template.getWhere()
			.AND()
			.appendGreaterEqual(potencia, 1.0);

		PreparedSql query = template.buildSql();
		
		System.out.println(query.getSql());
		System.out.println(query.getValues());
	}


}