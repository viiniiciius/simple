package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlElement;
import br.com.vitulus.simple.jdbc.sql.SqlSelectClausule;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression.LiteralFactory;


public class InExpression implements SqlExpression{

	public static final String IN = "IN";
	public static final String NOT = "NOT";
	
	private SqlExpression 			from;
	private boolean       			not;
	private InMultipleContent		content;
	
	public InExpression(String columnName,InMultipleContent content){
		this(new SqlColumn(null, columnName),content);		
	}			
	
	public InExpression(SqlColumn column,InMultipleContent content){
		this.from = new ColumnExpression(column);
		this.content = content;
	}			
	
	public InExpression(String columnName){
		this(new SqlColumn(null, columnName));		
	}			
	
	public InExpression(SqlColumn column){
		this.from = new ColumnExpression(column);		
	}			
	
	public InExpression(String columnName,LiteralFactory lfact,Object... values) {
		this(new SqlColumn(null, columnName),lfact,values);
	} 

	public InExpression(SqlColumn column,LiteralFactory lfact,Object... values) {		
		this(column);
		ExpressionInContent content = new ExpressionInContent();
		for(Object v : values){
			content.addExpression(lfact.getLiteral(v));
		}
		this.content = content;
	}
	
	public InExpression(String columnName,LiteralFactory lfact,Collection<Object> values) {
		this(new SqlColumn(null, columnName),lfact,values);
	} 

	public InExpression(SqlColumn column,LiteralFactory lfact,Collection<Object> values) {
		this(column);
		ExpressionInContent content = new ExpressionInContent();
		for(Object v : values){
			content.addExpression(lfact.getLiteral(v));
		}
		this.content = content;
	}
	
	public void setFrom(SqlExpression from) {
		this.from = from;
	}
	
	public SqlExpression getFrom() {
		return from;
	}
	
	public void setContent(InMultipleContent content) {
		this.content = content;
	}
	
	public InMultipleContent getContent() {
		return content;
	}
	
	public void setNot(boolean not) {
		this.not = not;
	}
	
	public boolean isNot() {
		return not;
	}
	
	protected interface InMultipleContent extends SqlElement{} 
	
	public static class ExpressionInContent implements InMultipleContent{
		private List<SqlExpression> expressions;
		private LiteralFactory 		lfact;
		
		public ExpressionInContent(LiteralFactory lfact){
			this();
			this.lfact = lfact;		
		}
		
		public void setLfact(LiteralFactory lfact) {
			this.lfact = lfact;
		}
		
		public LiteralFactory getLfact() {
			return lfact;
		}
		
		public ExpressionInContent() {
			this.expressions = new ArrayList<SqlExpression>();
		}
		
		public ExpressionInContent(SqlExpression... expressions) {
			this();
			this.expressions.addAll(Arrays.asList(expressions));
		}
		
		public void addExpression(SqlExpression expression){
			expressions.add(expression);
		}
		
		public void add(Object value){
			if(lfact == null){
				throw new IllegalAccessError("The LiteralFactory is not informed");
			}
			expressions.add(lfact.getLiteral(value));
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			Iterator<SqlExpression> i = expressions.iterator();
			p.append("(");
			while(i.hasNext()){
				i.next().append(p);
				if(i.hasNext()){
					p.append(",");
				}
			}
			p.append(")");
		}		
	}
	
	public static class SelectInContent implements InMultipleContent{
		private SqlSelectClausule selectStmt;
		
		public SelectInContent(SqlSelectClausule selectStmt) {
			this.selectStmt = selectStmt;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append("(");
			selectStmt.append(p);
			p.append(")");
		}		
	}
	
	public static class TableInContent implements InMultipleContent{
		private SqlTable table;		
		
		public TableInContent(SqlTable table) {
			this.table = table;
		}

		public TableInContent(String tableName) {
			this.table = new SqlTable(tableName);
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			table.append(p);
		}
		
	}
	
	
	@Override
	public void append(Appendable p) throws IOException{
		from.append(p);
		if(isNot()){
			p.append(" ").append(NOT);
		}
		p.append(" ").append(IN).append(" ");
		content.append(p);
	}

}