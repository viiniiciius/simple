package br.com.vitulus.simple.jdbc.template;

import br.com.vitulus.simple.jdbc.sql.SqlGroupByClausule;
import br.com.vitulus.simple.jdbc.sql.SqlLimitClausule;
import br.com.vitulus.simple.jdbc.sql.SqlStatement;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression;

public abstract class TemplateFactory implements SqlStatement{

	public static DeleteTemplate getDeleteTemplate(String tableName){
		return new DeleteTemplate(tableName);
	}
	
	public static DeleteTemplate getDeleteTemplate(SqlTable table){
		return new DeleteTemplate(table);
	}
	
	public static DeleteTemplate getDeleteTemplate(String table, String whereClause){
		DeleteTemplate template = new DeleteTemplate(table);
		if(whereClause != null && !whereClause.isEmpty()){
			LiteralValueExpression selectionExpr = new LiteralValueExpression(whereClause, false);
			template.getWhere().setExpression(selectionExpr);
		}
		return template;
	}
	
	public static InsertTemplate getInsertTemplate(SqlTable table){
		return new InsertTemplate(table);
	}
	
	public static InsertTemplate getInsertTemplate(String tableName){
		return new InsertTemplate(tableName);
	}
	
	public static UpdateTemplate getUpdateTemplate(String tableName){
		return new UpdateTemplate(tableName);
	}
	
	public static UpdateTemplate getUpdateTemplate(String table , String selection){
		UpdateTemplate template = new UpdateTemplate(table);
		if(selection != null && !selection.isEmpty()){
			LiteralValueExpression selectionExpr = new LiteralValueExpression(selection, false);
			template.getWhere().setExpression(selectionExpr);
		}
		return template;
	}	
	
	public static SelectTemplate getSelectTemplate(String tableName){
		return new SelectTemplate(tableName);
	}
	
	public static SelectTemplate getSelectTemplate(boolean distinct, String table, String[] columns, String selection , String groupBy, String having, String orderBy, String limit){
		SelectTemplate template = new SelectTemplate(table);
		template.setDistinct(distinct);
		for(String column : columns){
			template.addProjection(column);
		}
		if(selection != null && !selection.isEmpty()){
			LiteralValueExpression selectionExpr = new LiteralValueExpression(selection, false);
			template.getWhere().setExpression(selectionExpr);
		}
		if(groupBy != null && !groupBy.isEmpty()){
			LiteralValueExpression groupByExpr = new LiteralValueExpression(groupBy, false);
			template.getSelect().setGroupBy(new SqlGroupByClausule(groupByExpr));
		}
		if(limit != null && !limit.isEmpty()){
			template.getStatement().setLimit(new SqlLimitClausule(limit));
		}
		return template;
	}
	
}