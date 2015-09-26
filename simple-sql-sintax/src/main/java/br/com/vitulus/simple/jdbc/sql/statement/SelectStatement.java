package br.com.vitulus.simple.jdbc.sql.statement;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.DMLStatement;
import br.com.vitulus.simple.jdbc.sql.SqlLimitClausule;
import br.com.vitulus.simple.jdbc.sql.SqlOrderByClausule;
import br.com.vitulus.simple.jdbc.sql.SqlSelectClausule;
import br.com.vitulus.simple.jdbc.sql.SqlSelectClausule.SELECT_TYPE;

public class SelectStatement extends AbstractSqlStatement implements DMLStatement {

	private SqlSelectClausule 		select;
	private SqlOrderByClausule		orderBy;
	private SqlLimitClausule		limit;
	
	public SelectStatement() {
		this.select = new SqlSelectClausule(SELECT_TYPE.ALL);
	}
	
	public SqlSelectClausule getSelect() {
		return select;
	}
	
	public SqlOrderByClausule getOrderBy() {
		return orderBy;
	}
	
	public void setOrderBy(SqlOrderByClausule orderBy) {
		this.orderBy = orderBy;
	}
	
	public SqlLimitClausule getLimit() {
		return limit;
	}
	
	public void setLimit(SqlLimitClausule limit) {
		this.limit = limit;
	}
	
}