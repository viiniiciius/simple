package br.com.vitulus.simple.jdbc.sql.statement;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import br.com.vitulus.simple.jdbc.sql.AbstractSqlStatement;
import br.com.vitulus.simple.jdbc.sql.DMLStatement;
import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlElement;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class InsertStatement extends AbstractSqlStatement implements DMLStatement {

	private static final ValueElement DEFAULT_VALUES = new DefaultValues();
	
	public static final String INSERT_INTO 		= "INSERT INTO";
	public static final String DEFAULT 			= "DEFAULT";
	public static final String VALUES			= "VALUES";
	
	private 	SqlTable 		table;
	private 	ValueElement	values;
	
	public InsertStatement(SqlTable table,boolean isDefault) {
		this.table = table;
		this.values = isDefault ? DEFAULT_VALUES : new DefinedValues();
	}	
	
	public InsertStatement(SqlTable table,SelectStatement select) {
		this.table = table;
		this.values = new SqlValues(select);
	}
	
	public InsertStatement(SqlTable table,Map<SqlColumn,SqlExpression> data) {
		this.table = table;
		this.values = new DefinedValues(data);
	}
	
	public SqlTable getTable() {
		return table;
	}
	
	public ValueElement getValues() {
		return values;
	}
	
	public boolean addDataValue(SqlColumn column, SqlExpression value){
		if(getValues() != null && getValues() instanceof DefinedValues){
			DefinedValues values = (DefinedValues) getValues();
			values.addDataValue(column, value);
			return true;
		}
		return false;
	}
	
	public interface DataValue extends SqlElement{};
	public interface DataDefinition extends SqlElement{
		Collection<SqlColumn> getDataColumns();
	};	
	public interface ValueElement extends SqlElement{
		DataDefinition 	getDefinition();
		DataValue 		getValue();
	}
	
	protected class DefinedValues implements ValueElement{		
		
		private  InformedDataDefinition 			infoDataDefinition;
		private  InformedDataValues    				infoDataValue;
		private  Map<SqlColumn,SqlExpression>		data;
		
		public DefinedValues(Map<SqlColumn,SqlExpression> data) {
			this.infoDataDefinition 	= new InformedDataDefinition();
			this.infoDataValue			= new InformedDataValues();
			this.data					= data;
		}
		
		public DefinedValues() {
			this(new LinkedHashMap<SqlColumn, SqlExpression>());
		}		
		
		public void addDataValue(SqlColumn column, SqlExpression value){
			data.put(column, value);
		}
		
		@Override
		public void append(Appendable p) throws IOException {
			if(!getDefinition().getDataColumns().isEmpty()){
				getDefinition().append(p);
				p.append(" ");				
			}
			getValue().append(p);
		}
		@Override
		public DataDefinition getDefinition() {
			return infoDataDefinition;
		}
		@Override
		public DataValue getValue() {
			return infoDataValue;
		}
		
		protected Set<SqlColumn> getDataColumns(){
			return data.keySet();
		}		
		
		protected Collection<SqlExpression> getDataValues(){
			return data.values();
		}
		
		protected class InformedDataDefinition implements DataDefinition{	
			public InformedDataDefinition() {		
			}
			@Override
			public Set<SqlColumn> getDataColumns() {
				return DefinedValues.this.getDataColumns();
			}
			@Override
			public void append(Appendable p) throws IOException {				
				p.append("(");
				Iterator<SqlColumn> i = getDataColumns().iterator();
				while(i.hasNext()){
					SqlColumn column = i.next();
					p.append(column.getName());
					if(i.hasNext()){
						p.append(",");
					}
				}
				p.append(")");
			}
		}

		
		protected class InformedDataValues implements DataValue{			
			public InformedDataValues() {				
			}			
			@Override
			public void append(Appendable p) throws IOException {
				p.append(VALUES);
				p.append("(");
				Iterator<SqlExpression> i = getDataValues().iterator();
				while(i.hasNext()){
					SqlExpression value = i.next();
					value.append(p);
					if(i.hasNext()){
						p.append(",");
					}
				}
				p.append(") ");
			}			
		}		
		
	}	
	
	protected class SqlValues extends DefinedValues{
		
		private SelectStatement select;
		private SelectValues	values;
		
		public SqlValues(SelectStatement select) {
			this.select = select;
			this.values = new SelectValues();
		}
		
		@Override
		public DataValue getValue() {
			return values;
		}

		@Override
		protected Set<SqlColumn> getDataColumns() {
			return super.getDataColumns();
		}
		
		protected class SelectValues implements DataValue{
			@Override
			public void append(Appendable p) throws IOException {
				p.append(select.toString());
			}			
		}
	}
	
	protected static class DefaultValues implements ValueElement{		
		private final DataDefinition defaultDadataDefinition 	= new DefaultDataDefinition();
		private final DataValue      defaultDataValue			= new DefaultDataValue();		
		@Override
		public DataDefinition getDefinition() {
			return defaultDadataDefinition;
		}
		@Override
		public DataValue getValue() {
			return defaultDataValue;
		}		
		@Override
		public void append(Appendable p) throws IOException {
			getDefinition().append(p);
			p.append(" ");
			getValue().append(p);
		}		
		protected class DefaultDataValue implements DataValue{
			@Override
			public void append(Appendable p) throws IOException {
				p.append(VALUES);
			}
		}		
		protected class DefaultDataDefinition implements DataDefinition{			
			@Override
			public void append(Appendable p) throws IOException {
				p.append(DEFAULT);
			}

			
			@Override
			@SuppressWarnings("unchecked")
			public Collection<SqlColumn> getDataColumns() {
				return Collections.EMPTY_SET;
			}			
		}
		
	}
	
}