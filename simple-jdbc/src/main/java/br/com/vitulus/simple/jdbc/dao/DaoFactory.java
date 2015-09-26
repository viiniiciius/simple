package br.com.vitulus.simple.jdbc.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.database.ConnectionFactory;
import br.com.vitulus.simple.jdbc.database.IDatabase;
import br.com.vitulus.simple.jdbc.orm.BeanFormat;
import br.com.vitulus.simple.jdbc.orm.OrmResolver;
import br.com.vitulus.simple.jdbc.setup.ContextSetup;


public class DaoFactory {
	
	private static DaoFactory instance;
	private Map<Class<? extends Entity>,Set<Class<? extends DaoEntity<?>>>> 	daoMap;
	private Map<Class<? extends DaoEntity<?>>,Set<String>>                  	schemaMap;
	private Map<DaoKey,Class<? extends DaoEntity<?>>>                  			schemaIndex;

	private DaoFactory() {
		daoMap = new HashMap<Class<? extends Entity>, Set<Class<? extends DaoEntity<?>>>>();
		schemaMap = new HashMap<Class<? extends DaoEntity<?>>, Set<String>>();
		schemaIndex = new HashMap<DaoKey, Class<? extends DaoEntity<?>>>();
	}

	public static DaoFactory instance() {
		if (instance == null) {
			instance = new DaoFactory();
		}
		return instance;
	}

	public <T extends Entity>boolean registerDao(Class<T> modelo,Class<? extends DaoEntity<T>> daoEntity,String schema){		
		DaoKey key = new DaoKey(modelo, schema);
		if(!schemaIndex.containsKey(key)){
			if(ConnectionFactory.getInstance(schema).getConfig() == null){
				System.err.println("WARNING : Você está registrando um Dao para o schema '"+
									schema+
									"', o qual ainda não foi registrado na ConnectionFactory .");
			}
			getDaoEntitiesByModeloClass(modelo).add(daoEntity);		
			getSchemasByDaoEntityClass(daoEntity).add(schema);
			schemaIndex.put(key, daoEntity);
			System.out.println(new Date() 	+
								" [DAO] Registrado - " 	+
								modelo.getCanonicalName() +
								" : " + daoEntity.getCanonicalName() +
								" - Schema : " + schema);
			return true;
		}
		return false;
	}
	
	private Set<String> getSchemasByDaoEntityClass(Class<? extends DaoEntity<?>> daoEntity){
		Set<String> schemas;
		if((schemas = schemaMap.get(daoEntity)) == null){
			schemaMap.put(daoEntity, schemas = new HashSet<String>());
		}
		return schemas;
	}
	
	private Set<Class<? extends DaoEntity<?>>> getDaoEntitiesByModeloClass(Class<? extends Entity> modelo){
		Set<Class<? extends DaoEntity<?>>> daoEntities;
		if((daoEntities = daoMap.get(modelo)) == null){
			daoMap.put(modelo, daoEntities = new HashSet<Class<? extends DaoEntity<?>>>());
		}
		return daoEntities;
	}
	
	public Set<String> getSchemasByDao(Class<? extends DaoEntity<?>> classe){
		return schemaMap.get(classe);
	}

	public Set<Class<? extends DaoEntity<?>>> getDaosByEntity(Class<? extends Entity> modelo){
		return daoMap.get(modelo);
	}	
	
	private <T extends Entity> DaoEntity<T> buildDao(DaoEntity<T> dao,String schema) {
		RelationalSqlDatabase db = IDatabase.DatabaseFactory.getDatabase(schema);
		return dao.whith(db);
	}	
	
	private <T extends Entity> DaoEntity<T> buildDefaultDao(Class<T> modelo) {
		BeanFormat<T> type = OrmResolver.getBeanFormat(modelo);
		DaoCrud<T> dao = new DaoCrud<T>(type);
		return buildDao(dao,ContextSetup.DEFAULT_SCHEMA);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity> DaoEntity<T> getDao(Class<T> modelo,String schema) {		
		DaoKey key = new DaoKey(modelo, schema);
		if(schemaIndex.containsKey(key)){
			Class<? extends DaoEntity<?>> daoClass = schemaIndex.get(key);
			try {
				DaoEntity<T> dao = (DaoEntity<T>)daoClass.newInstance(); 
				return buildDao(dao,schema);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}else if(schema.equals(ContextSetup.DEFAULT_SCHEMA)){
			return buildDefaultDao(modelo);
		}else{
			throw new IllegalArgumentException("Não existem DAOs registrados para o schema '" + schema + "'.");
		}
		return null;		
	}
	
	public <T extends Entity> DaoEntity<T> getDao(Class<T> modelo) {
		return getDao(modelo, ContextSetup.DEFAULT_SCHEMA);
	}
	
	private static class DaoKey{
		Class<? extends Entity> 	modelo;
		String 						schema;		
		private DaoKey(Class<? extends Entity> modelo, String schema) {
			this.modelo = modelo;
			this.schema = schema;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((modelo == null) ? 0 : modelo.hashCode());
			result = prime * result
					+ ((schema == null) ? 0 : schema.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DaoKey other = (DaoKey) obj;
			if (modelo == null) {
				if (other.modelo != null)
					return false;
			} else if (!modelo.equals(other.modelo))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "DaoKey [modelo=" + modelo + ", schema=" + schema + "]";
		}
		
	}	
}