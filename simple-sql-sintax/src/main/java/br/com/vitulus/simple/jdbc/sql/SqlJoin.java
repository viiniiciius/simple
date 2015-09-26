package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import br.com.vitulus.simple.jdbc.sql.SqlJoinSource.SqlSingleSource;
import br.com.vitulus.simple.jdbc.sql.expression.BinaryExpression;
import br.com.vitulus.simple.jdbc.sql.expression.ColumnExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression.BinaryOperator;

public class SqlJoin implements SqlElement{

	public static final String JOIN 		= "JOIN";
	public static final String ON 			= "ON";
	public static final String USING		= "USING";
	public static final String COMMA 		= ",";
	
	public enum JoinType implements SqlElement{
		NONE,
		LEFT,
		LEFT_OUTER,
		RIGHT,
		INNER;
		@Override
		public void append(Appendable p) throws IOException {
			if(this != NONE){
				p.append(this.toString());
			}
		}
	}
	
	private SqlJoinOperation 	operation;
	private SqlSingleSource 	source;
	private SqlJoinConstraint	constraint;
	
	public SqlJoin(JoinType type,SqlTable table,SqlJoinConstraint constraint) {
		this.operation = new SqlJoinOperation(type);
		this.source = new SqlJoinSource.SqlSimpleSource(table);
		this.constraint = constraint;
	}
	
	public SqlJoin(SqlJoinOperation operation, SqlSingleSource source, SqlJoinConstraint constraint) {
		this.operation = operation;
		this.source = source;
		this.constraint = constraint;
	}	
	
	public SqlJoinOperation getOperation() {
		return operation;
	}

	public void setOperation(SqlJoinOperation operation) {
		this.operation = operation;
	}

	public SqlSingleSource getSource() {
		return source;
	}

	public void setSource(SqlSingleSource source) {
		this.source = source;
	}

	public SqlJoinConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(SqlJoinConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public void append(Appendable p) throws IOException {
		operation.append(p);
		p.append(" ");
		source.append(p);
		p.append(" ");
		constraint.append(p);
	}
	
	public static class SqlJoinOperation implements SqlElement{
		
		private JoinType type;
		
		public SqlJoinOperation(JoinType type) {
			this.type = type;
		}
		
		public SqlJoinOperation() {
		}		
		
		public void setType(JoinType type) {
			this.type = type;
		}
		
		public void setLeftJoin(){
			setType(JoinType.LEFT);
		}
		
		public void setLeftOuterJoin(){
			setType(JoinType.LEFT_OUTER);
		}
		
		public void setRightJoin(){
			setType(JoinType.RIGHT);
		}
		
		public void setInnerJoin(){
			setType(JoinType.INNER);
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			if(type == null){
				p.append(COMMA);
			}else{
				type.append(p);
				p.append(" ");
				p.append(JOIN);
			}
		}		
	}
	
	protected interface SqlJoinConstraint extends SqlElement{};
	
	public static class SqlOnJoinConstraint implements SqlJoinConstraint{
		
		private SqlExpression expression;
		
		public SqlOnJoinConstraint(SqlExpression expression) {
			this.expression = expression;
		}
		
		public SqlOnJoinConstraint(SqlColumn local,SqlColumn joined) {
			ColumnExpression localExpr = new ColumnExpression(local);
			ColumnExpression joinedExpr = new ColumnExpression(joined);
			this.expression = new BinaryExpression(localExpr, BinaryOperator.EQ, joinedExpr);
		}
		
		public SqlOnJoinConstraint(String columnNameLocal,String columnNameJoined) {
			ColumnExpression localExpr = new ColumnExpression(columnNameLocal);
			ColumnExpression joinedExpr = new ColumnExpression(columnNameJoined);
			this.expression = new BinaryExpression(localExpr, BinaryOperator.EQ, joinedExpr);
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			if(expression != null){
				p.append(ON);
				p.append(" ");
				expression.append(p);				
			}
		}		
	}
	
	public static class SqlUsingJoinConstraint implements SqlJoinConstraint{
		
		private List<String> columnsNames;
		
		public SqlUsingJoinConstraint() {
			this.columnsNames = new ArrayList<String>();
		}
		
		public void clear(){
			columnsNames.clear();
		}
		
		public void add(String columnName){
			columnsNames.add(columnName);
		}
		
		public List<String> getColumnsNames() {
			return Collections.unmodifiableList(columnsNames);
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			if(!columnsNames.isEmpty()){
				p.append(USING);
				p.append("(");
				Iterator<String> i = columnsNames.iterator();
				while(i.hasNext()){
					String columnName = i.next();
					p.append(columnName);
					if(i.hasNext()){
						p.append(",");
					}
				}
				p.append(")");
			}			
		}		
	}
	
}