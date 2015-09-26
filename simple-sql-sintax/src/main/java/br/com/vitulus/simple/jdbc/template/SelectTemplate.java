package br.com.vitulus.simple.jdbc.template;

import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlFromClausule;
import br.com.vitulus.simple.jdbc.sql.SqlJoin;
import br.com.vitulus.simple.jdbc.sql.SqlLimitClausule;
import br.com.vitulus.simple.jdbc.sql.SqlOrderByClausule;
import br.com.vitulus.simple.jdbc.sql.SqlResultColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.SqlWhereClausule;
import br.com.vitulus.simple.jdbc.sql.SqlJoin.JoinType;
import br.com.vitulus.simple.jdbc.sql.SqlJoin.SqlOnJoinConstraint;
import br.com.vitulus.simple.jdbc.sql.SqlOrderByClausule.ORDERING_MODE;
import br.com.vitulus.simple.jdbc.sql.SqlSelectClausule.SqlSelectCore;
import br.com.vitulus.simple.jdbc.sql.expression.ColumnExpression;
import br.com.vitulus.simple.jdbc.sql.expression.InExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.statement.SelectStatement;

public class SelectTemplate extends AbstractTemplate{

	private SelectStatement 	statement;
	private ExpressionBuilder 	whereBuilder;
	private SqlTable			table;
	private SqlWhereClausule    where;
	private SqlSelectCore       select;
	
	SelectTemplate() {
		this.statement = new SelectStatement();
		this.whereBuilder = newExpressionBuilder();
		select = this.statement.getSelect().getCore();
		select.setWhere(where = new SqlWhereClausule(null));
	}

	SelectTemplate(String tableName) {
		this(tableName,null);
		
	}
	
	SelectTemplate(String tableName,ExpressionBuilder whereBuilder) {
		this(new SqlTable(tableName),whereBuilder);
	}
	
	SelectTemplate(SqlTable table) {
		this(table,null);
		
	}
	
	SelectTemplate(SqlTable table,ExpressionBuilder whereBuilder) {
		this();
		this.table = table;
		if((this.whereBuilder = whereBuilder) == null){
			this.whereBuilder = newExpressionBuilder();
		}
		this.whereBuilder.addListener(new ExpressionBuilderListener(){
			@Override
			public void onExpressionChange(SqlExpression old, SqlExpression now) {
				where.setExpression(now);
			}			
		});
		select.setFrom(new SqlFromClausule(table));
	}	
	
	public void setDistinct(boolean distinct){
		if(distinct){
			select.setDistinctType();
		}else{
			select.setDefaultType();
		}
	}
	
	public ExpressionBuilder getWhereBuilder() {
		return whereBuilder;
	}
	
	public void addLeftJoin(SqlTable table,SqlColumn local,SqlColumn joined){
		addJoin(table, JoinType.LEFT, local, joined);
	}
	
	public void addRightJoin(SqlTable table,SqlColumn local,SqlColumn joined){
		addJoin(table, JoinType.RIGHT, local, joined);
	}
	
	public void addInnerJoin(SqlTable table,SqlColumn local,SqlColumn joined){
		addJoin(table, JoinType.INNER, local, joined);
	}
	
	public void addJoin(SqlTable table,JoinType type,SqlColumn local,SqlColumn joined){
		SqlOnJoinConstraint constraint = new SqlOnJoinConstraint(local, joined);
		addJoin(table, type, constraint);		
	}
	
	public void addJoin(SqlTable table,JoinType type,SqlExpression expression){
		SqlOnJoinConstraint constraint = new SqlOnJoinConstraint(expression);
		addJoin(table, type, constraint);		
	}

	private void addJoin(SqlTable table, JoinType type,	SqlOnJoinConstraint constraint) {
		SqlJoin join = new SqlJoin(type,table, constraint);		
		select.getFrom().getSource().addJoin(join);
	}
	
	public void addProjection(String columnName){
		addProjection(new SqlColumn(table, columnName));
	}
	
	public void addProjection(SqlColumn column){
		ColumnExpression expression = new ColumnExpression(column);
		SqlResultColumn projection = new SqlResultColumn(expression, null);
		select.addProjection(projection);
	}
	
	public void addProjection(SqlExpression expression){
		SqlResultColumn projection = new SqlResultColumn(expression, null);
		select.addProjection(projection);
	}
	
	@Override
	public SelectStatement getStatement() {
		return statement;
	}

	SqlWhereClausule getWhere() {
		return where;
	}
	
	SqlSelectCore getSelect() {
		return select;
	}
	
	public void setLimit(int offset,int limit){
		statement.setLimit(new SqlLimitClausule(getLiteral(limit), getLiteral(offset)));
	}
	
	public void setOrderBy(String columnName){
		setOrderBy(columnName, null);
	}
	
	public void setOrderBy(SqlColumn column){
		setOrderBy(column, null);
	}
	
	public void setOrderBy(String columnName,ORDERING_MODE mode){
		statement.setOrderBy(new SqlOrderByClausule(columnName,mode));
	}
	
	public void setOrderBy(SqlColumn column,ORDERING_MODE mode){
		statement.setOrderBy(new SqlOrderByClausule(column,mode));
	}
	
	@Override
	public StringBuilder getSql() {
		StringBuilder sql = new StringBuilder();
		try{
			statement.getSelect().append(sql);
			if(statement.getOrderBy() != null){
				sql.append(" ");
				statement.getOrderBy().append(sql);
			}
			if(statement.getLimit() != null){
				sql.append(" ");
				statement.getLimit().append(sql);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return sql;
	}
	
	
	public static void main(String[] args) {
		SqlTable marca = new SqlTable("marca");
		SqlColumn idM = new SqlColumn(marca, "id");
		SqlColumn nomeM = new SqlColumn(marca, "nome");
		
		SqlTable carro = new SqlTable("carro");
		SqlColumn idC = new SqlColumn(carro, "id");
		SqlColumn fkM = new SqlColumn(carro, "fk_marca");
		SqlColumn cor = new SqlColumn(carro, "cor");
		SqlColumn modelo = new SqlColumn(carro, "modelo");
		SqlColumn potencia = new SqlColumn(carro, "potencia");
		
		SelectTemplate template = new SelectTemplate(carro);
		template.setLimit(5, 10);
		template.setDistinct(false);
		
		template.addProjection(cor);
		template.addProjection(potencia);
		template.getWhereBuilder().appendEqual(idC, 5);
		template.addProjection(modelo);
		template.addLeftJoin(marca, fkM, idM);
		template.getWhereBuilder().AND().appendGreaterEqual(potencia, 1.0);
		template.addProjection(nomeM);		
		
		SelectTemplate templateIn = new SelectTemplate(marca,template.whereBuilder);
		templateIn.addProjection(nomeM);	
		InExpression inExpr = new InExpression(nomeM);
		inExpr.setContent(new InExpression.SelectInContent(templateIn.statement.getSelect()));
		template.getWhereBuilder().append(inExpr);
		
		
		PreparedSql query = template.buildSql();		
		System.out.println(query.getSql());
		System.out.println(query.getValues());
	}
	
}