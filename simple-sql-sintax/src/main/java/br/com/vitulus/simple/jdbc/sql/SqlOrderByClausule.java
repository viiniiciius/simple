package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.vitulus.simple.jdbc.sql.expression.ColumnExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class SqlOrderByClausule implements SqlElement {
	
	public static final String ORDER_BY = "ORDER BY";	
	public enum ORDERING_MODE implements SqlElement{
		ASC,
		DESC;
		@Override
		public void append(Appendable p) throws IOException {
			p.append(this.toString());
		}		
	}
	
	private List<SqlOrderingTerm> terms;	
	
	public SqlOrderByClausule() {
		this.terms = new ArrayList<SqlOrderingTerm>();
	}
	
	public SqlOrderByClausule(SqlColumn column){
		this(column,null);
	}
	
	public SqlOrderByClausule(String columnname){
		this(columnname,null);
	}
	
	public SqlOrderByClausule(SqlColumn column,ORDERING_MODE mode){
		this();
		terms.add(new SqlOrderingTerm(new ColumnExpression(column),mode));
	}
	
	public SqlOrderByClausule(String columnname,ORDERING_MODE mode){
		this();
		terms.add(new SqlOrderingTerm(new ColumnExpression(columnname),mode));
	}
	
	public List<SqlOrderingTerm> getTerms() {
		return terms;
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		if(!terms.isEmpty()){
			p.append(ORDER_BY);
			p.append(" ");
			Iterator<SqlOrderingTerm> i = terms.iterator();
			while(i.hasNext()){
				SqlOrderingTerm term = i.next();
				term.append(p);
				if(i.hasNext()){
					p.append(",");
				}
			}
		}
	}
	
	public class SqlOrderingTerm implements SqlElement{

		private SqlExpression 	expression;
		private SqlCollation 	collation;
		private ORDERING_MODE	mode;
		
		public SqlOrderingTerm(SqlExpression expression) {
			this.expression = expression;
		}

		public SqlOrderingTerm(SqlExpression expression,ORDERING_MODE mode) {
			this(expression);
			this.mode = mode;
		}
		
		public ORDERING_MODE getMode() {
			return mode;
		}
		
		public SqlOrderingTerm setModeASC() {
			this.mode = ORDERING_MODE.ASC;
			return this;
		}
		
		public SqlOrderingTerm setModeDESC(){
			this.mode = ORDERING_MODE.DESC;
			return this;
		}
		
		public SqlCollation getCollation() {
			return collation;
		}
		
		public void setCollation(SqlCollation collation) {
			this.collation = collation;
		}
		
		public SqlExpression getExpression() {
			return expression;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			expression.append(p);			
			if(getCollation() != null){
				p.append(" ");
				getCollation().append(p);
			}
			if(getMode() != null){
				p.append(" ");
				getMode().append(p);
			}
		}
		
	}

}