package br.com.vitulus.simple.jdbc.sql.old;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.SqlExecutor;
import br.com.vitulus.simple.jdbc.dao.AbstractDao;
@Deprecated
public class QueryExecutor<T extends Entity> implements SqlExecutor<T> {

	private static final long serialVersionUID = 6886442844218218600L;
	private AbstractDao<T> dao;

	public QueryExecutor(AbstractDao<T> dao) {
		this.dao = dao;
	}

	@Override
	public List<T> executarNamedQuery(String name, Collection<Object> params,
			String... fields) throws SQLException {
		return executarNamedQuery(name, params, null, fields);
	}

	@Override
	public List<T> executarNamedQuery(String name, Collection<Object> params,Integer quant, String... fields) throws SQLException {
		String root = CrudQuery.getSelectRoot(dao.getClasse(), fields);
		String where = AbstractDao.getStoredQuerysMap().get(name);
		String sql = root + " " + where;
		return executarQuery(sql, params, quant);
	}

	@Override
	public List<T> executarQuery(IQuery query) throws SQLException {
		return executarQuery(query.getQuery(), query.getParams(), null);
	}

	public List<T> executarQuery(String query, Object valorParametro)
			throws SQLException {
		return executarQuery(query, valorParametro, null);
	}

	public List<T> executarQuery(String query, Object valorParametro,
			Integer quant) throws SQLException {
		List<Object> params = new ArrayList<Object>();
		params.add(valorParametro);
		return executarQuery(query, params, quant);
	}

	public List<T> executarQuery(String query, Collection<Object> params)
			throws SQLException {
		return executarQuery(query, params, null);
	}

	public List<T> executarQuery(String query, Collection<Object> params,Integer quant) throws SQLException {
		List<T> pList = new ArrayList<T>();
		executarQuery(query,params,quant,pList,true);
		return pList;
	}
	
	public List<Map<String, Object>> executarUntypedQuery(String query, Collection<Object> params,Integer quant)throws SQLException {
		List<Map<String, Object>> pList = new ArrayList<Map<String,Object>>();
		executarQuery(query,params,quant,pList,false);
		return pList;
	}
	
	public List<Map<String, Object>> executarUntypedQuery(String query, Collection<Object> params)throws SQLException {
		return executarUntypedQuery(query,params,null);
	}
	
	public List<Map<String, Object>> executarUntypedQuery(IQuery query)throws SQLException {
		return executarUntypedQuery(query.getQuery(),query.getParams(),null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void executarQuery(String query, Collection<Object> params,Integer quant,List lista,boolean formated){
		if (params == null) {
			return;
		}
		try {
			ResultSet rs = dao.executeQuery(query, params, quant);
			while (rs.next()) {
				if(formated){
				    T entity = dao.parseEntity(rs);
				    lista.add(entity);
				}else{
					lista.add(dao.formatResultSet(rs));
				}
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}