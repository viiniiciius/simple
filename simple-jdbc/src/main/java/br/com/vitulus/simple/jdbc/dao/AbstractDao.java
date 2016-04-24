package br.com.vitulus.simple.jdbc.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.EntityManager;
import br.com.vitulus.simple.jdbc.annotation.ForwardKey;
import br.com.vitulus.simple.jdbc.annotation.Inheritance;
import br.com.vitulus.simple.jdbc.annotation.Table;
import br.com.vitulus.simple.jdbc.database.ConnectionFactory;
import br.com.vitulus.simple.jdbc.orm.old.OrmFormat;
import br.com.vitulus.simple.jdbc.orm.old.OrmResolver;
import br.com.vitulus.simple.jdbc.setup.ContextSetup;
import br.com.vitulus.simple.jdbc.sql.old.CrudQuery;
import br.com.vitulus.simple.jdbc.sql.old.IQuery;

/**
 * 
 * @author Vinicius
 */
@Deprecated
public abstract class AbstractDao<T extends Entity> implements Serializable,EntityManager<T> {

	private static final long serialVersionUID = 186038189036166890L;
	public  static final int MAX_RESULTS = 1000000; 
	public  static volatile int times;
	
	private static Map<String, String> 			storedQuerysMap;
	private static Map<String, List<String>> 	metaTableAICache;
	private static DataSource					datasource;
	private static ThreadLocal<Connection>		currentConnection = new ThreadLocal<Connection>();
	
	static {
		storedQuerysMap = new HashMap<String, String>();
		metaTableAICache = new HashMap<String, List<String>>();
	}

	private Class<T> classe;

	public AbstractDao(Class<T> classe) {
		this.classe = classe;
	}

	public static void registerDatasource(DataSource datasource){
		AbstractDao.datasource = datasource;
	}
	
	public DataSource getDataSource() {
		if(datasource == null){
			ConnectionFactory cf = ConnectionFactory.getInstance(ContextSetup.DEFAULT_SCHEMA);
			try {
				return cf.getDataSource();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return datasource;
	}

	public synchronized Connection getConnection() {
		Connection con = null;
		try {
			if((con = currentConnection.get()) == null){
				currentConnection.set(con = getDataSource().getConnection());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	public synchronized void closeConnection() {
		Connection con = currentConnection.get();
		if(con != null){
			try {
				con.close();
				currentConnection.remove();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public ResultSet executeQuery(String sql, Collection<Object> params,Integer limit) throws SQLException {
		PreparedStatement preparedStatement = this.getConnection().prepareStatement(sql);
		if (limit != null) {
			preparedStatement.setMaxRows(limit > MAX_RESULTS ? MAX_RESULTS : limit);
		} else {
			preparedStatement.setMaxRows(MAX_RESULTS);
		}
		if (params != null) {
			for (int i = 1; i < params.size() + 1; i++) {
				setParams(preparedStatement, i, params.toArray());
			}
		}
		times++;
		return preparedStatement.executeQuery();
	}

	public ResultSet executeQuery(String sql, Object... params)	throws SQLException {
		PreparedStatement preparedStatement = this.getConnection().prepareStatement(sql);
		preparedStatement.setMaxRows(MAX_RESULTS);
		if (params != null) {
			for (int i = 1; i < params.length + 1; i++) {
				setParams(preparedStatement, i, params);
			}
		}
		times++;
		return preparedStatement.executeQuery();
	}

	public Map<String, Object> execute(IQuery query) throws SQLException {
		String sql = query.getQuery();
		Object[] params = query.getParams().toArray();
		boolean getGenKey = this.getConnection().getMetaData().supportsGetGeneratedKeys();
		PreparedStatement preparedStatement = getGenKey ? this.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : this.getConnection().prepareStatement(sql);
		if (params != null) {
			for (int i = 1; i < params.length + 1; i++) {
				setParams(preparedStatement, i, params);
			}
		}
		times++;
		try{
			preparedStatement.execute();	
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		String table = query.getTable();
		return getGeneratedAutoIncremetValues(preparedStatement, table);
	}

	private void setParams(PreparedStatement preparedStatement, int i,Object... params) throws SQLException {
		Object o = params[i - 1];
		if (o instanceof java.util.Date) {
			Date date = (Date)o;
			preparedStatement.setTimestamp(i, new java.sql.Timestamp(date.getTime()));
		}else if (o instanceof java.sql.Date) {
			preparedStatement.setDate(i, (java.sql.Date)o);
		}else if (o instanceof Integer) {
			preparedStatement.setInt(i, (Integer) o);
		}else if (o instanceof Long) {
			preparedStatement.setLong(i, (Long) o);
		}else if (o instanceof String) {
			preparedStatement.setString(i, (String) o);
		}else if (o instanceof Byte) {
			preparedStatement.setByte(i, (Byte) o);
		}else if (o instanceof Boolean) {
			preparedStatement.setBoolean(i, (Boolean) o);
		}else if (o instanceof Float) {
			preparedStatement.setFloat(i, (Float) o);
		}else if (o instanceof Double) {
			preparedStatement.setDouble(i, (Double)o);
		} else if(o != null){
			preparedStatement.setObject(i, params[i - 1]);
		}
	}
	
	public Map<String, Object> formatResultSet(ResultSet rs)throws SQLException {
		if (rs != null) {
			Map<String, Object> objects = new HashMap<String, Object>();
			ResultSetMetaData meta = rs.getMetaData();
			int count = meta.getColumnCount();
			for (int i = 1; i <= count; i++) {
				String columnName = meta.getColumnName(i);
				objects.put(columnName, getTypeSyncValue(rs, i));
			}
			return objects;
		}
		return null;
	}

	private List<String> getGeneratedNames(ResultSet rs) throws SQLException {
		List<String> generatedNames = new ArrayList<String>();
		while (rs.next()) {
			try{
				int col = rs.findColumn("IS_AUTOINCREMENT");
				boolean isAutoIncrement = col > 0 && rs.getString(col).equals("YES");
				if (isAutoIncrement) {
					String name = rs.getString("COLUMN_NAME");
					generatedNames.add(name);
				}
			}catch (SQLException e) {
			}
		}
		rs.close();
		return generatedNames;
	}

	private List<Object> getGeneratedValues(PreparedStatement preparedStatement)throws SQLException {
		ResultSet generatedValues = preparedStatement.getGeneratedKeys();
		List<Object> generatedKeys = new LinkedList<Object>();
		if (generatedValues != null) {
			while (generatedValues.next()) {
				int count = generatedValues.getMetaData().getColumnCount();
				for (int i = 1; i <= count; i++) {
					generatedKeys.add(getTypeSyncValue(generatedValues, i));
				}
			}
			generatedValues.close();
		}
		return generatedKeys;
	}

	private Map<String, Object> getGeneratedResult(List<String> names, List<Object> values) {
		if ((names.size() != values.size()) && values.size() > 0) {
			throw new RuntimeException("Erro recuperar valores gerados");
		}
		Map<String, Object> generateMap = new HashMap<String, Object>();
		Iterator<String> iteratorNames = names.iterator();
		Iterator<Object> iteratorValues = values.iterator();
		while (iteratorNames.hasNext() && iteratorValues.hasNext()) {
			generateMap.put(iteratorNames.next(), iteratorValues.next());
		}
		return generateMap;
	}

	private Map<String, Object> getGeneratedAutoIncremetValues(PreparedStatement ps, String table) throws SQLException {
		List<String> names;
		if (AbstractDao.metaTableAICache.containsKey(table)) {
			names = AbstractDao.metaTableAICache.get(table);
		} else {
			if(ps.getMetaData() != null){
				int count = ps.getMetaData().getColumnCount();
				names = new ArrayList<String>();
				for(int i = 1; i <= count;i++){
					names.add(ps.getMetaData().getColumnName(i).toLowerCase());
				}			
			}else{
				ResultSet rs = getConnection().getMetaData().getColumns(null, null,table, null);
				names = getGeneratedNames(rs);
			}
			AbstractDao.metaTableAICache.put(table, names);
		}
		List<Object> values = getGeneratedValues(ps);
		return getGeneratedResult(names, values);
	}

	private Object getTypeSyncValue(ResultSet rs, int i) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		Object value = null;
		switch (meta.getColumnType(i)) {
		case Types.BOOLEAN:
			value = rs.getBoolean(i);break;				
		case Types.CHAR:
			value = rs.getString(i);break;
		case Types.DATE:
			value = rs.getDate(i);break;
		case Types.DOUBLE:
			value = rs.getDouble(i);break;
		case Types.FLOAT:
			value = rs.getFloat(i);break;
		case Types.INTEGER:
			value = rs.getInt(i);break;
		case Types.TIME:
			value = rs.getTime(i);break;
		case Types.TIMESTAMP:
			value = rs.getTimestamp(i);break;
		case Types.VARCHAR:
			value = rs.getString(i);break;
		default:
			value = rs.getObject(i);
		}
		return value;
	}

	protected List<String> referencedConstraint(Class<?> type, Map<String, Object> keyParams) throws SQLException {
		List<String> constraintList = new ArrayList<String>();
		ResultSet rs = getConnection().getMetaData().getExportedKeys(null,	null, CrudQuery.getTableName(type));
		Map<String, Map<String, String>> joinMap = new HashMap<String, Map<String, String>>();
		Map<String, Object> whereParams = new HashMap<String, Object>();
		String tableName = CrudQuery.getTableName(type);
		while (rs.next()) {
			Map<String, Object> formatedRs = formatResultSet(rs);
			String fkTableName = formatedRs.get("FKTABLE_NAME").toString();
			Map<String, String> joinAttributteMap = new HashMap<String, String>();
			if ((joinAttributteMap = joinMap.get(fkTableName)) == null) {
				joinAttributteMap = new HashMap<String, String>();
				joinMap.put(fkTableName, joinAttributteMap);
			}
			String pk = formatedRs.get("PKCOLUMN_NAME").toString();
			String fk = formatedRs.get("FKCOLUMN_NAME").toString();
			String full_pk = tableName + "." + pk;
			if (keyParams.containsKey(full_pk)) {
				whereParams.put(full_pk, keyParams.get(full_pk));
			} else {
				throw new RuntimeException("O valor da coluna " + pk + " Ã© obrigatÃ³rio");
			}
			joinAttributteMap.put(pk, fk);
		}
		rs.close();
		for (Map.Entry<String, Map<String, String>> entry : joinMap.entrySet()) {
			IQuery query = CrudQuery.getInheritanceConstraintQuery(tableName, entry, whereParams);
			ResultSet rsi = executeQuery(query.getQuery(), query.getParams(), 1);
			if (rsi.next()) {
				constraintList.add(entry.getKey());
			}
			rsi.close();
		}
		return constraintList;
	}

	private void applyForwardKey(OrmFormat format, Class<?> type,Map<Class<?>, Map<String, Object>> object) {
		if (type.isAnnotationPresent(Inheritance.class)) {
			Class<?> inheriter = format.getOrmResolver().getInheritanceMap().get(type);
			ForwardKey[] fks = type.getAnnotation(Inheritance.class).joinFields();
			for (ForwardKey fk : fks) {
				Object value = object.get(inheriter).get(fk.foreginField());
				object.get(type).put(fk.tableField(), value);
			}
		}
	}

	private Map<String, Object> restoreInheritance(Class<?> type, OrmFormat format) throws SQLException {
		Map<String, Object> whereParams = format.formatKey();
		IQuery query = CrudQuery.getSelectQuery(type, whereParams, "*");
		Map<String, Object> formatedRs = new HashMap<String, Object>();
		if(!isEverKeyNull(query.getParams())){
			ResultSet rs = executeQuery(query.getQuery(), query.getParams(), 1);
			if (rs.next()) {
				formatedRs = formatResultSet(rs);
			}
			rs.close();
		}
		return formatedRs;
	}

	public List<T> listar(String... fields) throws SQLException {
		List<T> lista = new LinkedList<T>();
		IQuery query = CrudQuery.getListQuery(classe, fields);
		ResultSet rs = executeQuery(query.getQuery(), query.getParams(), null);
		while (rs.next()) {
			lista.add(parseEntity(rs));
		}
		rs.close();
		return lista;
	}

	public void update(T entity, Map<String, Object> whereParams) throws SQLException {
		if (whereParams == null || whereParams.isEmpty()) {
			throw new RuntimeException(
					"Parametro WHERE obrigatorio. Caso contrario causaria alteraÃ§Ã£o de todos os dados da tabela");
		}
		OrmFormat format = new OrmFormat(entity);
		Map<Class<?>, Map<String, Object>> object = format.formatDisjoin();
		LinkedList<Class<?>> sortedSet = new LinkedList<Class<?>>(object.keySet());
		Iterator<Class<?>> iterator = sortedSet.iterator();
		while (iterator.hasNext()) {
			Class<?> type = iterator.next();
			updateFormated(object.get(type),type,whereParams);
		}
	}
	
	private void updateFormated(Map<String, Object> formatedType,Class<?> type,Map<String, Object> whereParams) throws SQLException{
		Map<String, Object> localParams = disjoinAttributes(formatedType.keySet(), whereParams, type);
		IQuery query = CrudQuery.getUpdateQuery(formatedType,localParams, CrudQuery.getTableName(type));
		execute(query);
	}

	public void insert(T entity) throws SQLException {
		OrmFormat format = new OrmFormat(entity);
		Map<Class<?>, Map<String, Object>> object = format.formatDisjoin();
		LinkedList<Class<?>> sortedSet = new LinkedList<Class<?>>(object.keySet());
		Iterator<Class<?>> iterator = sortedSet.descendingIterator();
		while (iterator.hasNext()) {
			Class<?> type = iterator.next();
			applyForwardKey(format, type, object);
			Map<String, Object> readyInheritance;
			if ((readyInheritance = restoreInheritance(type, format)) != null && !readyInheritance.isEmpty()) {
				Map<String, Object> whereParams = format.formatKey();
				updateFormated(object.get(type),type,whereParams);
				object.put(type, readyInheritance);
				continue;
			}
			IQuery query = CrudQuery.getInsertQuery(object.get(type), CrudQuery.getTableName(type));
			Map<String, Object> generated = execute(query);
			format.parse(generated);
			for (String name : generated.keySet()) {
				object.get(type).put(name, generated.get(name));
			}
		}
	}

	public void remove(Map<String, Object> whereParams, boolean permissive)
			throws SQLException {
		if (whereParams == null || whereParams.isEmpty()) {
			throw new RuntimeException(
					"Parametro WHERE obrigatorio. Caso contrario causaria remoÃ§Ã£o de todos os dados da tabela");
		}
		OrmResolver orm = new OrmResolver(classe);
		LinkedList<Class<?>> sortedSet = new LinkedList<Class<?>>(orm.getInheritanceMap().keySet());
		Iterator<Class<?>> iterator = sortedSet.iterator();
		while (iterator.hasNext()) {
			Class<?> type = iterator.next();
			Set<String> keysColumns = orm.format(type, false).keySet();
			Map<String, Object> localParams = disjoinAttributes(keysColumns,whereParams, type);
			IQuery query = CrudQuery.getRemoveQuery(localParams, CrudQuery.getTableName(type));
			try {
				execute(query);
			} catch (SQLNonTransientException e) {
				if (sortedSet.size() > 1 && permissive) {
					break;
				} else {
					throw e;
				}
			}
		}
	}

	private Map<String, Object> disjoinAttributes(Set<String> keys,	Map<String, Object> attributes, Class<?> type) {
		Map<String, Object> localParams = new LinkedHashMap<String, Object>();
		Inheritance inherite = type.getAnnotation(Inheritance.class);
		for (String key : attributes.keySet()) {
			if (keys.contains(key)) {
				localParams.put(key, attributes.get(key));
			}
			if (inherite != null) {
				Table superTable = type.getSuperclass().getAnnotation(Table.class);
				Table typeTable = type.getAnnotation(Table.class);
				for (ForwardKey fk : inherite.joinFields()) {
					String foreginField = superTable.name()+ "." + fk.foreginField();
					String tableField = typeTable.name() + "." + fk.tableField();
					if (foreginField.equals(key)) {
						localParams.put(tableField, attributes.get(key));
					}
				}
			}
		}
		return localParams;
	}

	public static Map<String, String> getStoredQuerysMap() {
		return storedQuerysMap;
	}

	public T parseEntity(ResultSet rs) throws SQLException {
		return parseEntity(formatResultSet(rs));
	}
	
	public T parseEntity(Map<String, Object> data) throws SQLException {
		T entity = getNewEntity();
		OrmFormat format = new OrmFormat(entity);
		format.parse(data);
		return entity;
	}

	public Class<T> getClasse() {
		return classe;
	}

	protected boolean isEverKeyNull(List<Object> list){
		boolean ok = true;
		for(Object value : list){
			ok &= value != null;
		}
		return !ok;
	}
	
	public T getNewEntity(){
		T t = null;
		try {
			t = getClasse().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return t;
	}

}