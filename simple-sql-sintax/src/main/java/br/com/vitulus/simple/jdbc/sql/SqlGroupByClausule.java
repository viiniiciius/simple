package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.vitulus.simple.jdbc.sql.expression.ColumnExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class SqlGroupByClausule implements SqlElement{

	public static final String GROUP_BY = "GROUP BY";
	
	private List<SqlExpression> expressions;
	
	public SqlGroupByClausule() {
		this.expressions = new ArrayList<SqlExpression>();
	}
	
	public SqlGroupByClausule(SqlColumn column) {
		this();
		addGroupBy(new ColumnExpression(column));
	}
	
	public SqlGroupByClausule(String columname) {
		this(new SqlColumn(null, columname));
	}
	
	public SqlGroupByClausule(SqlExpression expression) {
		this();
		addGroupBy(expression);
	}
	
	public List<SqlExpression> getExpressions() {
		return expressions;
	}
	
	public SqlGroupByClausule addGroupBy(SqlExpression expression){
		this.expressions.add(expression);
		return this;
	}
	
	public void clear(){
		this.expressions.clear();
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		if(!expressions.isEmpty()){
			p.append(GROUP_BY);
			p.append(" ");
			Iterator<SqlExpression> i = expressions.iterator();
			while(i.hasNext()){
				SqlExpression expression = i.next();
				expression.append(p);
				if(i.hasNext()){
					p.append(",");
				}
			}
		}
	}

}