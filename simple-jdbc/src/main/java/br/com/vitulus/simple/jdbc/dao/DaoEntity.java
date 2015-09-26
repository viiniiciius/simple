package br.com.vitulus.simple.jdbc.dao;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.database.IDatabase;
import br.com.vitulus.simple.jdbc.orm.BeanFormat;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.EntityParser;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.QueryResultParser;
import br.com.vitulus.simple.jdbc.sql.SqlOrderByClausule.ORDERING_MODE;

public interface DaoEntity<T extends Entity> {
		
	public long 						insert(T o);
	public int 							update(T o);
	public int 							deleteById(int rowId);
	public int 							delete(T o);
	public int 							deleteAll();
	public void							close();

	public void							addDatabaseListener(DatabaseListener listener);
	public void							removeDatabaseListener(DatabaseListener listener);
	public List<DatabaseListener>		getAllDatabaseListeners();
	public void							addEntityParserListener(EntityParseListener listener);
	public void							removeEntityParserListener(EntityParseListener listener);
	public List<EntityParseListener>	getAllEntityParserListeners();
	public void 						beginTransaction();
	public void 						setTransactionSuccessful();
	public void 						endTransaction();
	
	public IDatabase					getDatabase();
	public BeanFormat<T> 				getType();
	public EntityReader<T>				getReader();
	
	public DaoEntity<T> whith(IDatabase db);

	public interface EntityReader<T extends Entity>{
		public boolean						exists(long rowId);
		public boolean						exists(T example,boolean juskey);
		public T            				get(long rowId);
		public T            				get(long rowId,String... projection);
		public Map<String,Object>	   		getUntyped(long rowId);
		public Map<String,Object>			getUntyped(long rowId,String... projection);	
		public T            				get(T example,boolean justKey);
		public T            				get(T example,boolean justKey,String... projection);	
		public Map<String,Object>	   		getUntyped(T example,boolean justKey);
		public Map<String,Object>			getUntyped(T example,boolean justKey,String... projection);
		public List<T> 						listAll();
		public List<T> 						list(int offset,int limit,String orderBy,ORDERING_MODE mode);
		public List<T> 						list(int offset,int limit,String orderBy,ORDERING_MODE mode,String... projection);
		public List<Map<String,Object>>		listUntypedAll();
		public List<Map<String,Object>>		listUntyped(int offset,int limit,String orderBy,ORDERING_MODE mode);
		public List<Map<String,Object>>		listUntyped(int offset,int limit,String orderBy,ORDERING_MODE mode,String... projection);
		public QueryResultParser			query(String sql,Object[] args);
		public List<Map<String,Object>>		queryUntype(String sql,Object[] args);
	}
	
	public interface EntityParseListener{
		public void onBeforeParse(Entity o,EntityParser parser,Object source);
		public void onAfterParse(Entity o,EntityParser parser,Object source);
		public void onBeforeUntypedParse(Map<String,Object> o,ResultSet mCursor,Object source);
		public void onAfterUntypedParse(Map<String,Object> o,ResultSet mCursor,Object source);
	}
	
	public interface DatabaseListener{
		public void onBeforeInsert(Entity o,Object source);
		public void onAfterInsert(Entity o,long generated,Object source);
		public void onBeforeUpdate(Entity o,Object source);
		public void onAfterUpdate(Entity o,int modified,Object source);
		public void onBeforeDelete(Entity o,Object source);
		public void onAfterDelete(Entity o,int modified,Object source);
	}
	
}