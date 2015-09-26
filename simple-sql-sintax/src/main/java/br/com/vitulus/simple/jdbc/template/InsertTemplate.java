package br.com.vitulus.simple.jdbc.template;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.statement.InsertStatement;
import br.com.vitulus.simple.jdbc.sql.statement.SelectStatement;

public class InsertTemplate extends AbstractTemplate implements DataTemplate{

	private InsertStatement 	statement;
	
	private InsertTemplate(){
	}
	
	InsertTemplate(SqlTable table,SelectStatement select) {
		this();
		this.statement = new InsertStatement(table, select);
	}
	
	InsertTemplate(SqlTable table) {
		this();
		this.statement = new InsertStatement(table, false);		
	}
	
	InsertTemplate(String tableName) {
		this(new SqlTable(tableName));	
	}
	
	public InsertTemplate addData(String columnName,Object value){
		statement.addDataValue(new SqlColumn(statement.getTable(),columnName), getLiteral(value));
		return this;
	}
	
	public InsertTemplate addData(SqlColumn column,Object value){
		statement.addDataValue(column, getLiteral(value));
		return this;
	}
	
	public InsertTemplate addData(SqlColumn column,SqlExpression value){
		statement.addDataValue(column, value);
		return this;
	}
	
	@Override
	public AbstractSqlStatement getStatement() {
		return statement;
	}

	@Override
	public StringBuilder getSql() {
		StringBuilder sql = new StringBuilder();
		try{			
			sql.append(InsertStatement.INSERT_INTO).append(" ");
			statement.getTable().append(sql);
			statement.getValues().append(sql);
		}catch (Exception e) {
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
		
		InsertTemplate template = new InsertTemplate(carro);
		template.addData(id, 5);
		template.addData(cor, "preto");
		template.addData(modelo, "gol");
		template.addData(potencia, "1");
		
		PreparedSql query = template.buildSql();		
		System.out.println(query.getSql());
		System.out.println(query.getValues());
		
	}

}