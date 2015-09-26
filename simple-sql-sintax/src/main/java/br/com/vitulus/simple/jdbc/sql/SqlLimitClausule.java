package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.regex.Pattern;

import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class SqlLimitClausule implements SqlElement {

	private static final Pattern sLimitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");
	public static String LIMIT = "LIMIT";
	
	private SqlExpression limit;
	private SqlExpression offset;
	
	public SqlLimitClausule(SqlExpression limit) {
		this.limit = limit;
	}
	
	public SqlLimitClausule(SqlExpression limit, SqlExpression offset) {
		this(limit);
		setOffset(offset);
	}

	public SqlLimitClausule(String limit) {
		if(sLimitPattern.matcher(limit).matches()){
			String[] s = limit.split(",");
			setLimit(s[0].trim());
			setOffset(s[1].trim());
		}		
	}
	
	public SqlLimitClausule(String limit, String offset) {
		this(limit);
		setOffset(offset);
	}
	
	public SqlLimitClausule(int limit) {
		this.limit = new LiteralValueExpression(limit);
	}
	
	public SqlLimitClausule(int limit, int offset) {
		this(limit);
		setOffset(offset);
	}
	
	public void setLimit(SqlExpression limit) {
		this.limit = limit;
	}
	
	public void setLimit(int limit) {
		this.limit = new LiteralValueExpression(limit);
	}
	
	public void setLimit(String limit) {
		this.limit = new LiteralValueExpression(limit);
	}
	
	public void setOffset(SqlExpression offset) {
		this.offset = offset;
	}
	
	public void setOffset(int offset) {
		this.offset = new LiteralValueExpression(offset);
	}
	
	public void setOffset(String offset) {
		this.offset = new LiteralValueExpression(offset);
	}
	
	/*
	 * MySql Version 'LIMIT offset,limit
	@Override
	public void append(Appendable p) throws IOException {
		p.append(LIMIT);
		p.append(" ");
		if(offset != null){			
			offset.append(p);
			p.append(",");
		}
		limit.append(p);
	}
	*/
	
	/**
	 * SqLite Version 'LIMIT limit,offset
	 * @see http://www.sqlite.org/syntaxdiagrams.html#select-stmt
	 */
	@Override
	public void append(Appendable p) throws IOException {
		p.append(LIMIT);
		p.append(" ");
		limit.append(p);
		if(offset != null){
			p.append(",");
			offset.append(p);			
		}
	}
	
}