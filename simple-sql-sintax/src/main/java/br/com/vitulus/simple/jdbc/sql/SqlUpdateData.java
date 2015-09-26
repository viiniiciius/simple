package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression;

public class SqlUpdateData implements SqlElement{

	private Map<SqlColumn,SqlExpression>   	data;
	
	public SqlUpdateData() {
		this.data = new HashMap<SqlColumn, SqlExpression>();
	}
	
	public void addData(SqlColumn column,SqlExpression value){
		data.put(column, value);
	}
	
	public void addData(SqlColumn column,Object value){
		data.put(column, new LiteralValueExpression(value));
	}
	
	public void clearData(){
		data.clear();
	}
	
	public Map<SqlColumn,SqlExpression> getData(){
		return Collections.unmodifiableMap(data);
	}
	
	@Override
	public void append(Appendable p) throws IOException {
		Iterator<Entry<SqlColumn, SqlExpression>> i = data.entrySet().iterator();
		while(i.hasNext()){
			Entry<SqlColumn, SqlExpression> e = i.next();
			p.append(e.getKey().getName());
			p.append(" = ");
			e.getValue().append(p);			
			if(i.hasNext()){
				p.append(",");
			}
			p.append(" ");
		}
	}

}