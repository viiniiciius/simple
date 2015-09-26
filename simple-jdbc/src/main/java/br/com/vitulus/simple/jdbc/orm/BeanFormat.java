package br.com.vitulus.simple.jdbc.orm;

import java.util.Date;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.orm.ObjectMapping.ObjectField;
import br.com.vitulus.simple.jdbc.orm.RelationalMapping.RelationalField;

public interface BeanFormat<T> {

	public abstract void format(T o, EntityFormater f);

	public abstract void formatWithoutKey(T o, EntityFormater f);
	
	public abstract void formatKey(T o, EntityFormater f);
	
	public abstract void formatEscapeAutoincrement(T o, EntityFormater f);
	
	public abstract void formatEscapeDefaults(T o, EntityFormater f);	

	public abstract T parse(EntityParser p);
	
	public abstract T parse(EntityParser p,T instance);

	public abstract T newInstance();
	
	public abstract OrmDescriptor getDescriptor();	

	public interface OrmDescriptor{
		ObjectMapping<? extends Entity> getObjectMapping();
		RelationalMapping				getRelationaMapping();
		ObjectField 					getObjectByPath(String fieldPath);		
		RelationalField 				getRelationalByColumn(String columnName);				
		ObjectField 					getObjectByColumn(String columnName);
		RelationalField 				getRelationalByPath(String fieldPath);
	}
	
	public interface EntityParser {
		int getInt(String name);

		long getLong(String name);
		
		double getDouble(String name);

		String getString(String name);

		boolean getBoolean(String name);

		Date getDate(String name);
	}

	public interface EntityFormater {
		void setInt(Class<?> dtype,String name, int value);

		void setLong(Class<?> dtype,String name, long value);
		
		void setDouble(Class<?> dtype,String name, double value);

		void setString(Class<?> dtype,String name, String value);

		void setBoolean(Class<?> dtype,String name, boolean value);

		void setDate(Class<?> dtype,String name, Date date);
	}

	public interface QueryResultParser {
		EntityParser next();
	}

}