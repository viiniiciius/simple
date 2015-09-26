package br.com.vitulus.simple.jdbc.sql.old;

import java.util.Arrays;
import java.util.List;
@Deprecated
public class Query implements IQuery {

	private String query;
	private List<Object> params;
	private String table;

	public Query(String query, List<Object> params, String table) {
		this.query = query;
		this.params = params;
		this.table = table;
	}
	
	public Query(String query, String table,Object... params){
		this.query = query;
		this.params = Arrays.asList(params);
		this.table = table;
	}

	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	@Override
	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

}