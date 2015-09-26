package br.com.vitulus.simple.jdbc.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.annotation.Dao;
import br.com.vitulus.simple.jdbc.util.ClassFinder;

public class DaoRegister {

	private List<String> 	pacotes;
	private String 			schema;
	
	public DaoRegister(String schema) {
		pacotes = new ArrayList<String>();
		this.schema = schema;
	}
	
	public DaoRegister(String schema,String... pacotes) {
		this(schema,Arrays.asList(pacotes));
	}
	
	public DaoRegister(String schema,List<String> pacotes) {
		this(schema);
		this.pacotes = pacotes;		
	}

	public List<Class<?>> registerEntityManagers(){
		List<Class<?>> registred = new ArrayList<Class<?>>();
		for(String pacote : getPacotes()){
			Class<?>[] classes = findClasses(pacote);
			if(classes != null){
				for(Class<?> scanned : classes){
					if(registerClass(scanned)){
						registred.add(scanned);
					}
				}
			}
		}
		return registred;
	}	
	
	public Class<?>[] findClasses(String pacote){
		Class<?>[] classes = null; 
		try {
			classes = ClassFinder.getClasses(pacote);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes;
	}	

	@SuppressWarnings("unchecked")
	public <T extends Entity>boolean registerClass(Class<?> scanned){		
		if(isDaoEntity(scanned)){			
			Class<? extends DaoEntity<T>> daoClass = (Class<? extends DaoEntity<T>>)scanned;
			return registerDao(daoClass);
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>boolean registerDao(Class<? extends DaoEntity<T>> daoClass){
		Dao dao;	
		if((dao = getDaoAnn(daoClass)) != null){
			Class<T> modelo = (Class<T>)dao.classe();
			return DaoFactory.instance().registerDao(modelo,daoClass,getSchema());
		}
		return false;
	}
	
	public Dao getDaoAnn(Class<?> daoClass){
		return daoClass.getAnnotation(Dao.class);
	}
	
	public boolean isDaoEntity(Class<?> scanned){
		return DaoEntity.class.isAssignableFrom(scanned);
	}

	public List<String> getPacotes() {
		return pacotes;
	}

	public void setPacotes(List<String> pacotes) {
		this.pacotes = pacotes;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}	
	
}