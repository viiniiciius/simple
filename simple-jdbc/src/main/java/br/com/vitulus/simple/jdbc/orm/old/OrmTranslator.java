package br.com.vitulus.simple.jdbc.orm.old;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.vitulus.simple.jdbc.annotation.Column;
import br.com.vitulus.simple.jdbc.annotation.Relationship;
import br.com.vitulus.simple.jdbc.annotation.Transient;
@Deprecated
public class OrmTranslator {

	private List<Field>                     fields;
	private Map<String, Field>              columnsMap;
	private Map<String, Field>              namesMap;
	private Map<String,Map<String, Field>>  tableColumnsMap;
	@SuppressWarnings("unused")
	private Map<String,Map<String, Field>>  classNamesMap;

	public OrmTranslator(List<Field> fields) {
		this.fields = fields;
		this.columnsMap = new HashMap<String, Field>();
		this.namesMap = new HashMap<String, Field>();
		this.tableColumnsMap = new HashMap<String, Map<String,Field>>();
		this.classNamesMap = new HashMap<String, Map<String,Field>>();
		resolveColumns();
	}

	private String resolveColumnName(Field field) {
		Column column;
		if ((column = field.getAnnotation(Column.class)) != null) {
			return column.name();
		} else if (!Modifier.isFinal(field.getModifiers())
				&& !Modifier.isStatic(field.getModifiers())) {
			return field.getName();
		} else {
			return null;
		}
	}

	private void resolveColumns() {
		for (Field field : fields) {
			if(OrmResolver.hasAnnotation(field , Transient.class)){
				continue;
			}else if (OrmResolver.hasAnnotation(field , Relationship.class)) {
				// TODO tratar relacionamentos
			} else {
				String fullColumnName = getColumn(field);
				String fieldName = field.getName();
				if (fullColumnName != null) {
					int tableIndex;
					String tableName = (tableIndex = fullColumnName.indexOf(".")) > 0 ? fullColumnName.substring(0,tableIndex) : null;
					String columnName = tableIndex > 0 ? fullColumnName.substring(tableIndex+1) : fullColumnName;
					getTableColumnMap(tableName).put(columnName, field);
					columnsMap.put(columnName, field);
				}
				namesMap.put(fieldName, field);
			}
		}
	}

	private Map<String, Field> getTableColumnMap(String table){
		if(!tableColumnsMap.containsKey(table)){
			return createTableColumnMap(table);
		}else{
			return tableColumnsMap.get(table);
		}
	}
	
	private Map<String, Field> createTableColumnMap(String table){
		Map<String, Field> tableColumnMap;
		tableColumnsMap.put(table, (tableColumnMap = new HashMap<String, Field>()));
		return tableColumnMap;
	}
	
	private Field findFieldByAnnotation(String column) {
		return columnsMap.get(column);
	}
	
	private Field findFieldByName(String name) {
		return namesMap.get(name);
	}

	public String getColumn(Field field) {
		if (field == null) {
			return null;
		}
		String columnName = resolveColumnName(field);
		return columnName;
	}

	public String getColumn(String field) {
		for (Field f : fields) {
			if (f.getName().equals(field)) {
				return getColumn(f);
			}
		}
		return null;
	}

	public String getFieldName(String column) {
		Field field = findFieldByAnnotation(column);
		if (field != null) {
			return field.getName();
		}
		return null;
	}

	public Field getFieldByColumnName(String columnName) {
		return findFieldByAnnotation(columnName);
	}
	
	public Field getFieldByFieldName(String fieldName) {
		return findFieldByName(fieldName);
	}
	
	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public Map<String, Field> getColumnsMap() {
		return columnsMap;
	}

	public void setColumnsMap(Map<String, Field> columnsMap) {
		this.columnsMap = columnsMap;
	}

}