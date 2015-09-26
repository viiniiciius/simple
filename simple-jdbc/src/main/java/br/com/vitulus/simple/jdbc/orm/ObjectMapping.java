package br.com.vitulus.simple.jdbc.orm;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import br.com.vitulus.simple.jdbc.Entity;

public class ObjectMapping<T extends Entity>{

	private Class<T> 					classe;
	private Class<? extends Entity> 	inheritance;
	private List<ObjectField>			fields;
	private List<ObjectField>			relationships;
	private String[]					fieldsName;
	private String[]					relationshipsName;
	
	
	public ObjectMapping(Class<T> classe,ObjectField[] fields,ObjectField[] relationships) {
		this.classe = classe;
		fieldsName = new String[fields.length];
		relationshipsName = new String[relationships.length];
		for(int i = 0;i < fields.length;i++ ){
			fieldsName[i] = fields[i].path;
		}
		for(int i = 0;i < relationships.length;i++ ){
			relationshipsName[i] = relationships[i].path;
		}
		this.relationships = Arrays.asList(relationships);
		this.fields = Arrays.asList(fields);		
	}	
	
	public Class<T> getClasse() {
		return classe;
	}
	
	public Class<? extends Entity> getInheritance() {
		return inheritance;
	}
	
	public void setInheritance(Class<T> inheritance) {
		this.inheritance = inheritance;
	}
	
	public List<ObjectField> getFields() {
		return fields;
	}
	
	public String[] getFieldsName(){
		return fieldsName;
	}	
	
	public List<ObjectField> getRelationships() {
		return relationships;
	}
	
	public String[] getRelationshipsName() {
		return relationshipsName;
	}
	
	@Override
	public String toString() {
		return "ObjectMapping [classe=" + classe + ", inheritance="
				+ inheritance + ", fields=" + fields + ", relationships="
				+ relationships + ", fieldsName=" + Arrays.toString(fieldsName)
				+ ", relationshipsName=" + Arrays.toString(relationshipsName)
				+ "]";
	}

	public static class ObjectField{
		private String 		    path;
		private Class<?> 	    type;
		private List<Class<?>>  genericTypes;
		boolean                 collection;
		boolean                 map;
		ObjectField(String path, Class<?> type,Type... genericTypes) {
			this.path = path;
			this.type = type;
			if(!(collection = Collection.class.isAssignableFrom(type))){
				map = Collection.class.isAssignableFrom(type);
			}
			List<Class<?>>  localGenericTypes = new LinkedList<Class<?>>();
			if(genericTypes != null)
				for(Type t : genericTypes)
					if(!(t instanceof Class<?>))
						throw new IllegalArgumentException("'" + t + "' Não é um parametro genérico válido. Não são suportados WidlCards nem Variáveis.");
					else
						localGenericTypes.add((Class<?>) t);
			this.genericTypes = Collections.unmodifiableList(localGenericTypes);
		}
		public String getPath() {
			return path;
		}
		public Class<?> getType() {
			return type;
		}
		public boolean isCollection(){
			return collection;
		}
		public boolean isMap(){
			return map;
		}
		public List<Class<?>> getGenericTypes() {			
			return genericTypes;
		}
		@Override
		public String toString() {
			return "ObjectField [path=" + path + ", type=" + type + "]";
		}
	}
	
	
}