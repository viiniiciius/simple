package br.com.vitulus.simple.jdbc.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.expression.BetweenExpression;
import br.com.vitulus.simple.jdbc.sql.expression.BinaryExpression;
import br.com.vitulus.simple.jdbc.sql.expression.BracketsExpression;
import br.com.vitulus.simple.jdbc.sql.expression.ColumnExpression;
import br.com.vitulus.simple.jdbc.sql.expression.InExpression;
import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;
import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression.LiteralFactory;
import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression.LiteralListener;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression.BinaryOperator;

public abstract class AbstractTemplate implements LiteralFactory{

	private List<Object>					values;
	private PreparedLiteralListener			literalListener;
	
	public AbstractTemplate() {
		this.values = new LinkedList<Object>();
		this.literalListener = new PreparedLiteralListener();
	}
	
	public PreparedSql buildSql(){
		fireOnQueryBuildStart();
		String sql = getSql().toString();
		List<Object> values = new ArrayList<Object>(this.values);
		Collections.copy(values, this.values);
		return new PreparedSql(sql, values);
	}
	
	public abstract AbstractSqlStatement 	getStatement();	
	protected abstract CharSequence 			getSql();	
	
	protected void fireOnQueryBuildStart(){
		clearExpressionsValues();
	}
	
	private void clearExpressionsValues(){
		values.clear();
	}
	
	public ExpressionBuilder newExpressionBuilder(){
		return new ExpressionBuilder();
	}	

	public LiteralValueExpression getLiteral(Object value){
		LiteralValueExpression literal = new LiteralValueExpression(value);
		literal.setListener(literalListener);
		return literal;
	}
	
	public class PreparedSql{
		private String 				sql;
		private Collection<Object> 	values;
		private PreparedSql(String sql, Collection<Object> values) {
			this.sql = sql;
			this.values = values;
		}
		public String getSql() {
			return sql;
		}
		public Collection<Object> getValues() {
			return values;
		}
		@Override
		public String toString() {
			return "PreparedSql [sql=" + sql + ", values=" + values + "]";
		}	
	}
	
	private class PreparedLiteralListener implements LiteralListener{
		@Override
		public void onAppend(Object value) {
			values.add(value);
		}		
	}
	
	/**
	 * Classe utilitária que auxilia principalmente na construção de expression para a clausula where.
	 * Como as expressions podem ser extremamente amplas esta classe ainda pode ser muito melhorada.
	 * @author vinicius
	 *
	 */
	public class ExpressionBuilder{		
		private SqlExpression 						expression;
		private List<ExpressionBuilderListener> 	listeners;
		
		protected ExpressionBuilder() {
			this.listeners = new ArrayList<ExpressionBuilderListener>();
		}
		
		public void addListener(ExpressionBuilderListener listener){
			this.listeners.add(listener);
		}
		
		public void removeAllListeners(){
			listeners.clear();
		}
		
		/**
		 * Igual
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendEqual(SqlColumn column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.EQ,value));
			return this;
		}
		
		/**
		 * Igual
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendEqual(String column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.EQ,value));
			return this;
		}

		/**
		 * Menor ou igual
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendLessEqual(String column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LE,value));
			return this;
		}
		
		/**
		 * Menor ou igual
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendLessEqual(SqlColumn column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LE,value));
			return this;
		}

		/**
		 * Menor
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendLessThan(String column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LT,value));
			return this;
		}
		
		/**
		 * Menor
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendLessThan(SqlColumn column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LT,value));
			return this;
		}
		
		/**
		 * Maior
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendGreaterThan(String column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.GT,value));
			return this;
		}		

		/**
		 * Maior
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendGreaterThan(SqlColumn column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.GT,value));
			return this;
		}
		
		/**
		 * Maior ou igual
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendGreaterEqual(String column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.GE,value));
			return this;
		}

		/**
		 * Maior ou igual
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendGreaterEqual(SqlColumn column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.GE,value));
			return this;
		}

		/**
		 * Diferente
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendNotEqual(String column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.NE,value));
			return this;
		}
		
		/**
		 * Diferente
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendNotEqual(SqlColumn column, Object value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.NE,value));
			return this;
		}
		
		/**
		 * Pre fixado (like %#)
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendPreFixed(String column, String value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LIKE,"%"+value));
			return this;
		}
		
		/**
		 * Pre fixado (like %#)
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendPreFixed(SqlColumn column, String value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LIKE,"%"+value));
			return this;
		}

		/**
		 * Pos fixado (like #%)
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendPosFixed(String column, String value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LIKE,value+"%"));
			return this;
		}
		
		/**
		 * Pos fixado (like #%)
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendPosFixed(SqlColumn column, String value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LIKE,value+"%"));
			return this;
		}

		/**
		 * Pre e Pos fixado (like %#%)
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendPrePosFixed(String column, String value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LIKE,"%"+value+"%"));
			return this;
		}
		
		/**
		 * Pre e Pos fixado (like %#%)
		 * @param value
		 * @return
		 */
		public ExpressionBuilder appendPrePosFixed(SqlColumn column, String value) {
			this.append(getBinaryOpExpr(column,BinaryOperator.LIKE,"%"+value+"%"));
			return this;
		}

		/**
		 * Beteween
		 * @param value
		 * @return
		 */
		public ExpressionBuilder bt(SqlColumn column, Object left, Object right) {
			ColumnExpression fromExpr = new ColumnExpression(column);
			LiteralValueExpression beginExpr = getLiteral(left);
			LiteralValueExpression endExpr = getLiteral(right);
			this.append(new BetweenExpression(fromExpr, beginExpr, endExpr));
			return this;
		}
		
		/**
		 * Beteween
		 * @param value
		 * @return
		 */
		public ExpressionBuilder bt(String column, Object left, Object right) {
			ColumnExpression fromExpr = new ColumnExpression(column);
			LiteralValueExpression beginExpr = getLiteral(left);
			LiteralValueExpression endExpr = getLiteral(right);
			this.append(new BetweenExpression(fromExpr, beginExpr, endExpr));
			return this;
		}
		
		/**
		 * AND
		 * @param brackets
		 * @return
		 */
		public ExpressionBuilder AND(boolean brackets){
			append(BinaryOperator.AND,brackets);
			return this;
		}
		
		/**
		 * OR
		 * @param brackets
		 * @return
		 */
		public ExpressionBuilder OR(boolean brackets){
			append(BinaryOperator.OR,brackets);
			return this;
		}
		
		public ExpressionBuilder AND(){
			return AND(false);
		}		
		
		public ExpressionBuilder OR(){
			return OR(false);
		}
		
		public ExpressionBuilder IN(String columnName,boolean bracket,Object... values){
			InExpression expr = new InExpression(columnName, AbstractTemplate.this, values);
			this.append(bracket ? new BracketsExpression(expr) : expr);
			return this;
		}
		
		public ExpressionBuilder IN(SqlColumn column,boolean bracket,Object... values){
			InExpression expr = new InExpression(column, AbstractTemplate.this, values);
			this.append(bracket ? new BracketsExpression(expr) : expr);
			return this;
		}
		
		public ExpressionBuilder IN(String columnName,boolean bracket,Collection<?> values){
			InExpression expr = new InExpression(columnName, AbstractTemplate.this, values);
			this.append(bracket ? new BracketsExpression(expr) : expr);
			return this;
		}
		
		public ExpressionBuilder IN(SqlColumn column,boolean bracket,Collection<?> values){
			InExpression expr = new InExpression(column, AbstractTemplate.this, values);
			this.append(bracket ? new BracketsExpression(expr) : expr);
			return this;
		}
		
		public ExpressionBuilder IN(String columnName,Object... values){
			return IN(columnName,false,values);
		}
		
		public ExpressionBuilder IN(SqlColumn column,Object... values){
			return IN(column,false,values);
		}
		
		public ExpressionBuilder IN(String columnName,Collection<?> values){
			return IN(columnName,false,values);
		}
		
		public ExpressionBuilder IN(SqlColumn column,Collection<?> values){
			return IN(column,false,values);
		}
		
		public ExpressionBuilder append(BinaryOperator op,boolean brackets){
			SqlExpression expression = brackets ? new BracketsExpression(getExpression()) : getExpression();
			setExpression(new BinaryExpression(expression, op, null));
			return this;
		}
		
	
		public ExpressionBuilder append(SqlExpression expr){
			if(getExpression() == null){
				setExpression(expr);
			}else if(getExpression() instanceof BinaryExpression){
				BinaryExpression binary = (BinaryExpression)getExpression();				
				if(binary.getRight() == null ||
						binary.getOperator() == BinaryOperator.OR  ||
						binary.getOperator() == BinaryOperator.AND){
					binary.setRight(expr);					
				}else{
					BinaryExpression noneOp = new BinaryExpression(binary,BinaryOperator.NONE , expr);
					setExpression(noneOp);
				}
			}
			return this;
		}
		
		private SqlExpression getBinaryOpExpr(String column,BinaryOperator operator,Object value){
			ColumnExpression colExpr = new ColumnExpression(column);
			LiteralValueExpression valExpr = getLiteral(value);
			return new BinaryExpression(colExpr, operator, valExpr);
		}
		
		private SqlExpression getBinaryOpExpr(SqlColumn column,BinaryOperator operator,Object value){
			ColumnExpression colExpr = new ColumnExpression(column);
			LiteralValueExpression valExpr = getLiteral(value);
			return new BinaryExpression(colExpr, operator, valExpr);
		}		
		
		private void fireEvent(SqlExpression expression){
			Iterator<ExpressionBuilderListener> i = listeners.iterator();
			while(i.hasNext()){
				i.next().onExpressionChange(this.expression,expression);
			}
		}
		
		public void setExpression(SqlExpression expression) {
			if(this.expression == null || this.expression != expression){
				fireEvent(expression);
			}
			this.expression = expression;
		}
		
		public SqlExpression getExpression() {
			return expression;
		}
		
		public void clear(){
			setExpression(null);
		}
		
		public LiteralValueExpression getLiteral(Object value){
			return AbstractTemplate.this.getLiteral(value);
		}
		
	}
	
	public static interface ExpressionBuilderListener{
		void onExpressionChange(SqlExpression old,SqlExpression now);
	}

}