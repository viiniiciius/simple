package br.com.vitulus.simple.jdbc.orm.old;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.SqlExecutor;
import br.com.vitulus.simple.jdbc.dao.AbstractDao;
import br.com.vitulus.simple.jdbc.dao.DaoEntity;
import br.com.vitulus.simple.jdbc.dao.DaoFactory;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.EntityParser;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.QueryResultParser;
import br.com.vitulus.simple.jdbc.sql.old.CrudQuery;
import br.com.vitulus.simple.jdbc.sql.old.IQuery;
import br.com.vitulus.simple.jdbc.sql.old.QueryExecutor;
@Deprecated
public abstract class DaoCrud<T extends Entity> extends AbstractDao<T> {

	private static final long serialVersionUID = 7282308921887713737L;

	private SqlExecutor<T> 	sqlExecutor;
	protected List<Field> 	fields;
	private boolean 		useTransaction;

	public DaoCrud(Class<T> classe) {
		super(classe);
		sqlExecutor = new QueryExecutor<T>(this);
		fields = OrmResolver.getAllFields(new LinkedList<Field>(), getClasse(), true);
	}

	public boolean exists(T o) throws SQLException {
		OrmFormat orm = new OrmFormat(o);
		Map<String, Object> keyMap = orm.formatKey();
		IQuery query = CrudQuery.getSelectQuery(super.getClasse(), keyMap);
		if(isEverKeyNull(query.getParams())){
			return false;
		}
		return executeQuery(query.getQuery(), query.getParams().toArray()).next();		
	}
	
	public T findByKey(OrmFormat orm) throws SQLException{
		Map<String, Object> keyMap = orm.formatKey();
		IQuery query = CrudQuery.getSelectQuery(getClasse(), keyMap,"*");
		List<T> results = getSqlExecutor().executarQuery(query.getQuery(), query.getParams(), 1);
		return results.size() == 1 ? results.get(0) : null;
	}
	
	protected void beforeUpdate(T o) throws Exception {
	}

	@Override
	public void alterar(T o) throws Exception {
		if (o != null && exists(o)) {
			Savepoint save = null;
			boolean beforeAutoComit = getConnection().getAutoCommit();
			try {
				if(isUseTransaction()){
					if(beforeAutoComit){
						getConnection().setAutoCommit(false);
					}
					save = getConnection().setSavepoint("Before Update - Savepoint");
				}				
				beforeUpdate(o);
				OrmFormat orm = new OrmFormat(o);
				update(o, orm.formatKey());
				afterUpdate(o);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (isUseTransaction() && save != null) {
					getConnection().rollback(save);
				}
				throw ex;
			}finally{
				if(isUseTransaction() && beforeAutoComit){
					getConnection().setAutoCommit(true);
				}
			}			
		} else if (o != null) {
			inserir(o);
		}
	}

	protected void afterUpdate(T o) throws Exception {
	}

	protected void beforeInsert(T o) throws Exception {
	}

	@Override
	public void inserir(T o) throws Exception {
		if (o != null && exists(o)) {
			alterar(o);
		} else if (o != null) {
			Savepoint save = null;
			boolean beforeAutoComit = getConnection().getAutoCommit();
			try {
				if(isUseTransaction()){
					if(beforeAutoComit){
						getConnection().setAutoCommit(false);
					}
					save = getConnection().setSavepoint("Before Insert - Savepoint");
				}				
				beforeInsert(o);
				insert(o);
				afterInsert(o);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (isUseTransaction() && save != null) {
					getConnection().rollback(save);
				}
				throw ex;
			}finally{
				if(isUseTransaction() && beforeAutoComit){
					getConnection().setAutoCommit(true);
				}
			}			
		}

	}

	protected void afterInsert(T o) throws Exception {
	}

	@Override
	public SqlExecutor<T> getSqlExecutor() {
		return sqlExecutor;
	}

	protected void beforeList() throws Exception {
	}

	public List<T> listar(boolean lazy, String... fields) {
		List<T> tList = null;
		try {
			getConnection().setReadOnly(true);
			beforeList();
			tList = listar(fields);
			if (tList != null && !lazy) {
				for (T t : tList) {
					try {
						afterLoad(t);
					} catch (Exception ex) {
					    ex.printStackTrace();
					}
				}
			}
			afterList();
			getConnection().setReadOnly(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tList;
	}
	
	@Override
	public List<T> listar() {
		return listar(false, "*");
	}

	protected void afterList() throws Exception {
	}

	protected void beforeLoad(T o) throws Exception {
	}

	public void load(T o) throws Exception {
		if (o == null) {
			throw new RuntimeException("Registro Inválido");
		}
		OrmFormat orm = new OrmFormat(o);
		Map<String, Object> key = orm.formatKey();
		if (key == null || key.size() == 0) {
			throw new RuntimeException("Registro Inválido");
		}
		IQuery query = CrudQuery.getSelectQuery(super.getClasse(), key, "*");
		List<T> lista = getSqlExecutor().executarQuery(query.getQuery(),query.getParams());
		if (lista.size() == 1) {
			beforeLoad(o);
			T loaded = lista.get(0);
			OrmFormat ormLoaded = new OrmFormat(loaded);
			orm.parse(OrmFormat.getCleanFormat(ormLoaded.format()));
			afterLoad(o);
		}
	}

	protected void afterLoad(T o) throws Exception {
	}

	protected boolean beforeRemove(T o, Map<String, Object> params)	throws Exception {
		return false;
	}

	@Override
	public void remover(T o) throws Exception {
		Savepoint save = null;
		boolean beforeAutoComit = getConnection().getAutoCommit();
		try {
			if(isUseTransaction()){
				if(beforeAutoComit){
					getConnection().setAutoCommit(false);
				}
				save = getConnection().setSavepoint("Before Remove - Savepoint");
			}			
			OrmFormat orm = new OrmFormat(o);
			Map<String, Object> params = orm.formatKey();
			boolean tolerance = beforeRemove(o, params);
			remove(params, tolerance);
			afterRemove(o);
		} catch (Exception ex) {
			ex.printStackTrace();
			if (isUseTransaction() && save != null) {
				getConnection().rollback(save);
			}
			throw ex;
		}finally{
			if(isUseTransaction() && beforeAutoComit){
				getConnection().setAutoCommit(true);
			}
		}

	}

	protected void afterRemove(T o) throws Exception {
	}

	public List<Field> getFields() {
		return fields;
	}
	
	/**
	 * Faz uma select na classe recebida em 'relacao' com join para classe atual do DAO .
	 * Para filtrar o join ( clausula 'ON') usa a declaracao de foregin key presente no field 'name'.
	 * @param <R> O Tipo de retorno
	 * @param relacao
	 * @param name
	 * @param whereParams
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <R extends Entity> void getRelacionamento(Class<R> relacao,String name,Map<String,Object> whereParams,Set lista,boolean formated) throws SQLException{
		StringBuilder sql = new StringBuilder(CrudQuery.getSelectRoot(relacao, "*"));
		OrmTranslator translator = new OrmTranslator(fields);
		sql.append(CrudQuery.buildJoin(translator,name,relacao));
		List<Object> params = new ArrayList<Object>();
		CrudQuery.makeWhereOfQuery(whereParams, params, sql);
		DaoEntity<R> dao = DaoFactory.instance().getDao(relacao);
		if(formated){
			QueryResultParser result = dao.getReader().query(sql.toString(), params.toArray());
			EntityParser parser;
			while((parser = result.next()) != null){
				parser.getString(" ??? "); // E Agora josé ?
				lista.add(null); //TODO Grande problema pra arrumar
			}
		}else{
			List<Map<String,Object>> result = dao.getReader().queryUntype(sql.toString(), params.toArray());
			lista.addAll(result);
		}	
	}	
	
	/**
	 * Inicialmente faz uma consulta filtrando pelo exemplo de <E>;
	 * Em seguida pega os resultados da consulta anterior e tenta relacionar cada resultado com <T>.
	 * Usa o nome do field 'fname' para recuperar a foregin key da classe atual com a classe <E>
	 * fornecendo assim a juncao inicial para a filtragem a partir do exemplo de 'relacao'.
	 * @param <R>
	 * @param <E>
	 * @param example
	 * @param relacao
	 * @param fname
	 * @param rname
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	public <R extends Entity,E extends Entity>void getRelacionamento(E example,R relacao,String fname,String rname,Set lista,boolean formated) throws SQLException{
		OrmTranslator translator = new OrmTranslator(fields);
		OrmTranslator exampleTranslator = new OrmTranslator(OrmResolver.getAllFields(new LinkedList<Field>(), example.getClass(), true));
		List<Field> keyFields = OrmResolver.getKeyFields(new LinkedList<Field>(), example.getClass(), true);
		String[] fkFields = new String[keyFields.size()];
		for(int i = 0 ; i < fkFields.length ; i++){
			fkFields[i] = keyFields.get(i).getName();
		}		
		OrmFormat format = new OrmFormat(example);
		OrmFormat formatRelacao = new OrmFormat(relacao);
		IQuery query = CrudQuery.getSelectQuery(example.getClass(), format.formatNotNull(), fkFields);		
		DaoEntity<E> dao = DaoFactory.instance().getDao((Class<E>)example.getClass());
		List<Map<String, Object>> results = dao.getReader().queryUntype(query.getQuery(), query.getParams().toArray());	
		for(Map<String, Object> result : results){
			Map<String, Object> whereParams = new HashMap<String, Object>();
			whereParams.put(translator.getColumn(fname), result.get(exampleTranslator.getColumn(fkFields[0])));
			whereParams.putAll(formatRelacao.formatNotNull());
			getRelacionamento((Class<R>)relacao.getClass(),rname,whereParams,lista,formated);
		}
	}
	
	public <R extends Entity> List<R> getRelacionamento(Class<R> relacao,String name,Map<String,Object> whereParams) throws SQLException{
		Set<R> lista = new HashSet<R>();
		getRelacionamento(relacao,name,whereParams,lista,true);
		return new ArrayList<R>(lista);

	}
	
	public <R extends Entity,E extends Entity>List<R> getRelacionamento(E example,R relacao,String fname,String rname) throws SQLException{
		Set<R> lista = new HashSet<R>();
		getRelacionamento(example,relacao,fname,rname,lista,true);
		return new ArrayList<R>(lista);
	}
	
	public <R extends Entity> List<Map<String,Object>> getAtypeRelacionamento(Class<R> relacao,String name,Map<String,Object> whereParams) throws SQLException{
		Set<Map<String,Object>> lista = new HashSet<Map<String,Object>>();
		getRelacionamento(relacao,name,whereParams,lista,false);
		return new ArrayList<Map<String,Object>>(lista);

	}
	
	public <R extends Entity,E extends Entity>List<Map<String,Object>> getAtypeRelacionamento(E example,R relacao,String fname,String rname) throws SQLException{
		Set<Map<String,Object>> lista = new HashSet<Map<String,Object>>();
		getRelacionamento(example,relacao,fname,rname,lista,false);
		return new ArrayList<Map<String,Object>>(lista);
	}

	public boolean isUseTransaction() {
		return useTransaction;
	}

	public void setUseTransaction(boolean useTransaction) {
		this.useTransaction = useTransaction;
	}
	
	
	
}