package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import br.com.vitulus.simple.jdbc.sql.statement.SelectStatement;

public class SqlJoinSource implements SqlElement{

	private SqlSingleSource		source;
	private List<SqlJoin> 		joins;
	
	private SqlJoinSource() {
		this.joins = new ArrayList<SqlJoin>();
	}
	
	public SqlJoinSource(SqlSingleSource source) {
		this();
		this.source = source;
	}
	
	public SqlJoinSource(SqlTable table) {
		this(new SqlSimpleSource(table));
	}
	
	public SqlJoinSource(String tableName) {
		this(new SqlSimpleSource(tableName));
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		source.append(p);
		if(!joins.isEmpty()){
			p.append(" ");
			Iterator<SqlJoin> i = joins.iterator();
			while(i.hasNext()){				
				SqlJoin join = i.next();
				join.append(p);
				if(i.hasNext()){
					p.append(" ");
				}
			}
		}
	}

	public void clearJoin(){
		joins.clear();
	}
	
	public void addJoin(SqlJoin join){
		joins.add(join);
	}
	
	public List<SqlJoin> getJoins() {
		return Collections.unmodifiableList(joins);
	}
	
	public interface SqlSingleSource extends SqlElement{}
	
	
	public static class SqlSimpleSource implements SqlSingleSource{
		
		private SqlTable table;
		
		public SqlSimpleSource(SqlTable table) {
			this.table = table;		
		}
		
		public SqlSimpleSource(String tableName) {
			this.table = new SqlTable(tableName);		
		}
		
		public SqlTable getTable() {
			return table;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			table.append(p);
			if(table.getAlias() != null && !table.getAlias().isEmpty()){
				p.append(" AS ");
				p.append(table.getAlias());
			}
		}		
	}
	
	public static class SqlSelectSource implements SqlSingleSource{
		
		private SelectStatement 	select;
		private String 				alias;
		
		public SqlSelectSource(SelectStatement select) {
			this(select,null);
		}
		
		public SqlSelectSource(SelectStatement select,String alias) {
			this.select = select;
			this.alias = alias;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append("(");
			p.append(select.toString());
			p.append(")");
			if(alias != null && !alias.isEmpty()){
				p.append(" AS ");
				p.append(alias);
			}
		}		
	}
	
	public static class SqlBracketsSource implements SqlSingleSource{
		
		private SqlElement source;
		
		public SqlBracketsSource(SqlJoinSource source) {
			this.source = source;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append("(");
			source.append(p);
			p.append(")");
		}		
	}
	
}