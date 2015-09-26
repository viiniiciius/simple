package br.com.vitulus.simple.jdbc.orm;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import br.com.vitulus.simple.jdbc.annotation.Column;
import br.com.vitulus.simple.jdbc.annotation.Enumerator;
import br.com.vitulus.simple.jdbc.annotation.Id;
import br.com.vitulus.simple.jdbc.annotation.Inheritance;
import br.com.vitulus.simple.jdbc.annotation.PrimitiveTable;
import br.com.vitulus.simple.jdbc.annotation.Relationship;
import br.com.vitulus.simple.jdbc.annotation.Table;

public class RelationalMapping {

	private Table 							table;
	private Inheritance						inheritance;
	private List<RelationalField>			fields;
	private List<RelationalField>			relationships;
	
	public RelationalMapping(Table table,RelationalField[] fields,RelationalField[] relationships) {
		this.table = table;
		this.fields = Arrays.asList(fields);
		this.relationships = Arrays.asList(relationships);
	}
	
	public Table getTable() {
		return table;
	}
	
	public List<RelationalField> getFields() {		
		return fields;
	}
	
	public Inheritance getInheritance() {
		return inheritance;
	}
	
	public void setInheritance(Inheritance inheritance) {
		this.inheritance = inheritance;
	}
	
	public List<RelationalField> getRelationships() {
		return relationships;
	}
	
	@Override
	public String toString() {
		return "RelationalMapping [table=" + table + ", inheritance="
				+ inheritance + ", fields=" + fields + ", relationships="
				+ relationships + "]";
	}

	public static class RelationalField{
		private Column 			column;
		private Enumerator 		enumerator;
		private Id				id;
		private Relationship	relationship;
		private PrimitiveTable  primitiveTable;
		
		RelationalField(String fieldName) {
			this(new FieldNameColumn(fieldName));
		}

		RelationalField(String fieldName, Id id) {
			this(new FieldNameColumn(fieldName),id);
		}
		
		RelationalField(Column column) {
			this.column = column;
		}

		RelationalField(Column column, Id id) {
			this(column);
			this.id = id;
		}

		RelationalField(Column column,Enumerator enumerator) {
			this(column);
			this.enumerator = enumerator;
		}

		RelationalField(Column column,Enumerator enumerator, Id id) {
			this(column,id);
			this.enumerator = enumerator;			
		}

		RelationalField(String fieldName,Enumerator enumerator) {
			this(fieldName);
			this.enumerator = enumerator;
		}

		RelationalField(String fieldName,Enumerator enumerator, Id id) {
			this(fieldName,id);
			this.enumerator = enumerator;			
		}
		
		RelationalField(String fieldName,Relationship relationship) {
			this(fieldName);
			this.relationship = relationship;
		}
		RelationalField(String fieldName,Relationship relationship,PrimitiveTable  primitiveTable) {
			this(fieldName,relationship);
			this.primitiveTable = primitiveTable;
		}		
		public Column getColumn() {
			return column;
		}
		
		public Enumerator getEnumerator() {
			return enumerator;
		}
		
		public Id getId() {
			return id;
		}
		
		public Relationship getRelationship() {
			return relationship;
		}		
		
		public PrimitiveTable getPrimitiveTable() {
			return primitiveTable;
		}
		
		public boolean hasEnum(){
			return enumerator != null;
		}
		public boolean hasKey(){
			return id != null;
		}
		public boolean hasRelationship(){
			return relationship != null;
		}

		@Override
		public String toString() {
			return "RelationalField [column=" + column + ", enumerator="
					+ enumerator + ", id=" + id + ", relationship="
					+ relationship + "]";
		}
		
	}
	
	
	private static class FieldNameColumn implements Column{		
		private String name;		
		FieldNameColumn(String name){
			this.name = name;
		}		
		@Override
		public Class<? extends Annotation> annotationType() {
			return Column.class;
		}
		@Override
		public String name() {
			return name;
		}
		@Override
		public String alias() {
			return null;
		}
		@Override
		public String toString() {
			return "FieldNameColumn [name=" + name + "]";
		}		
	}	
	
}