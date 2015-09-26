package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SqlSelectClausule implements SqlElement{

	public enum SELECT_TYPE implements SqlElement{
		ALL,
		DISTINCT;
		@Override
		public void append(Appendable p) throws IOException {
			p.append(this.toString());
		}
	}
	
	private SqlSelectCore 		core;
	private CompoundSelect		compound;
	
	public SqlSelectClausule(SELECT_TYPE type) {
		this.core = new SqlSelectCore(type);
	}
	
	public SqlSelectCore getCore() {
		return core;
	}	
	
	@Override
	public void append(Appendable p) throws IOException {
		core.append(p);
		if(compound != null){
			p.append(" ");
			compound.append(p);			
		}
	}
	
	public SqlSelectClausule bindCompound(CompoundSelect compound){
		this.compound = compound;
		return this;
	}
	
	public class SqlSelectCore implements SqlElement{
		
		public static final String SELECT 	= "SELECT";	
		
		private SELECT_TYPE 			type;		
		private List<SqlResultColumn> 	resultColumns;
		private SqlFromClausule			from;
		private SqlWhereClausule		where;
		private SqlGroupByClausule		groupBy;
		private SqlHavingClausule		having;
		
		public SqlSelectCore(SELECT_TYPE type) {
			this.type  = type;
			this.resultColumns = new ArrayList<SqlResultColumn>();
		}

		public void setDistinctType(){
			type = SELECT_TYPE.DISTINCT;
		}
		
		public void setAllType(){
			type = SELECT_TYPE.ALL;
		}
		
		public void setDefaultType(){
			type = null;
		}
		
		public void addProjection(SqlResultColumn projection){
			resultColumns.add(projection);
		}
		
		public List<SqlResultColumn> getResultColumns() {
			return Collections.unmodifiableList(resultColumns);
		}
		
		public SqlFromClausule getFrom() {
			return from;
		}

		public void setFrom(SqlFromClausule from) {
			this.from = from;
		}

		public SqlWhereClausule getWhere() {
			return where;
		}

		public void setWhere(SqlWhereClausule where) {
			this.where = where;
		}

		public SqlGroupByClausule getGroupBy() {
			return groupBy;
		}

		public void setGroupBy(SqlGroupByClausule groupBy) {
			this.groupBy = groupBy;
		}		
		
		public SqlHavingClausule getHaving() {
			return having;
		}

		public void setHaving(SqlHavingClausule having) {
			this.having = having;
		}

		public SELECT_TYPE getType() {
			return type;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append(SELECT);
			p.append(" ");
			if(getType() != null){
				getType().append(p);
				p.append(" ");
			}
			Iterator<SqlResultColumn> i = resultColumns.iterator();
			while(i.hasNext()){
				SqlResultColumn column = i.next();
				column.append(p);
				if(i.hasNext()){
					p.append(",");
				}
			}			
			if(from != null){
				p.append(" ");
				from.append(p);
			}
			if(where != null && where.getExpression() != null){
				p.append(" ");
				where.append(p);
			}
			if(groupBy != null){
				p.append(" ");
				groupBy.append(p);
			}
			if(having != null){
				p.append(" ");
				having.append(p);
			}
		}		
	}
	
	public class CompoundSelect implements SqlElement{		
		private SqlSelectClausule reference;
		private CompoundOperator  operator;
		@Override
		public void append(Appendable p) throws IOException {			
			operator.append(p);
			p.append(" ");
			reference.append(p);			
		}
		
	}
	
	public enum CompoundOperator implements SqlElement{
		UNION("UNION"),
		UNION_ALL("UNION ALL"),
		INTERSECT("INTERSECT"),
		EXCEPT("EXCEPT");
		
		private String symbol;
		
		private CompoundOperator(String symbol) {
			this.symbol = symbol;
		}
		
		public String getSymbol() {
			return symbol;
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			p.append(getSymbol());
		}
	}
	
}