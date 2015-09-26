package br.com.vitulus.simple.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import br.com.vitulus.simple.jdbc.database.ConnectionFactory;
import br.com.vitulus.simple.jdbc.database.ContentValues;
import br.com.vitulus.simple.jdbc.database.IDatabase;
import br.com.vitulus.simple.jdbc.database.TransactionListener;
import br.com.vitulus.simple.jdbc.template.DataTemplate;
import br.com.vitulus.simple.jdbc.template.DeleteTemplate;
import br.com.vitulus.simple.jdbc.template.InsertTemplate;
import br.com.vitulus.simple.jdbc.template.SelectTemplate;
import br.com.vitulus.simple.jdbc.template.TemplateFactory;
import br.com.vitulus.simple.jdbc.template.UpdateTemplate;
import br.com.vitulus.simple.jdbc.template.AbstractTemplate.PreparedSql;

public class RelationalSqlDatabase implements IDatabase{

	private class SqlTransaction{		
		SqlTransaction(boolean inTransaction, Savepoint save) {
			this.inTransaction = inTransaction;
			this.save = save;
		}		
		public SqlTransaction(TransactionListener transactionListener,boolean inTransaction, Savepoint save) {
			this(inTransaction,save);
			this.transactionListener = transactionListener;
		}

		TransactionListener 	transactionListener;
		boolean 				inTransaction;
		boolean 				transactionIsSuccessful;
		Savepoint 				save;
		boolean 				isNested(){
			return inTransaction;
		}
	}
	
	private String								schema;
	private LinkedList<SqlTransaction> 			transactions;
	private LinkedList<TransactionListener> 	transactionsListeners;
	
	protected RelationalSqlDatabase(String schema) {
		DatabaseContext context = new DatabaseContext();
		context.setProperty("class", getClass());
		context.setProperty("schema", schema);
		initialize(context);
	}
	
	protected RelationalSqlDatabase(DatabaseContext context) {
		initialize(context);
	}
	
	protected RelationalSqlDatabase() {
	}	
	
	private ConnectionFactory getConnectionFactory() {
		ConnectionFactory factory = ConnectionFactory.getInstance(schema);
		if(factory.getConfig() == null){
			throw new IllegalStateException("O schema '" + schema + "' não foi configurado." +
					"È preciso registrar uma instancia de StartupConfig ao schema.");
		}
		return factory;
	}
	
	protected Connection getConnection() {
		try {
			return this.getConnectionFactory().getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeConnection() {
		this.getConnectionFactory().closeConnection();
	}
	
	@Override
	public void beginTransaction() {
		beginTransactionWithListener(null);
	}

	@Override
	public void beginTransactionWithListener(TransactionListener transactionListener) {
		try {
			boolean inTransaction = inTransaction();
			if(!inTransaction){
				getConnection().setAutoCommit(false);
			}			
			if(transactionListener != null){
				transactionListener.onBegin();
			}
			Savepoint save = getConnection().getMetaData().supportsSavepoints() ? getConnection().setSavepoint() : null;
			transactions.add(new SqlTransaction(transactionListener,inTransaction,save));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endTransaction() {		
		if(!transactions.isEmpty()){
			SqlTransaction transaction = transactions.removeLast();
			try {
				transactionsListeners.add(transaction.transactionListener);
				if(transaction.transactionIsSuccessful){
					if(!transaction.isNested()){
						fireComitEvent();
						getConnection().setAutoCommit(true);
					}
				}else{
					fireRoolbackEvent();
					if(transaction.save != null){
						getConnection().rollback(transaction.save);
					}else{
						getConnection().rollback();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void fireComitEvent(){
		for(TransactionListener listener : transactionsListeners){
			if(listener != null){
				listener.onCommit();
			}						
		}
		transactionsListeners.clear();
	}
	
	private void fireRoolbackEvent(){
		TransactionListener listener = transactionsListeners.removeLast();
		if(listener != null){
			listener.onRollback();
		}					
	}

	@Override
	public void setTransactionSuccessful() {
		try{
			transactions.getLast().transactionIsSuccessful = true;
		}catch(NoSuchElementException nsee){
			throw new RuntimeException("Não há nenhuma transação aberta.",nsee);
		}
	}

	@Override
	public boolean inTransaction() {
		try {
			return !getConnection().getAutoCommit();
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void close() {
		transactions.clear();
		transactionsListeners.clear();
		this.closeConnection();
	}

	@Override
	public ResultSet query(boolean distinct, String table, String[] columns, String selection, Object[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		SelectTemplate template = TemplateFactory.getSelectTemplate(distinct, table, columns, selection, groupBy, having, orderBy, limit);
		try {
			return rawQuery(template.getSql().toString(),selectionArgs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ResultSet query(String table, String[] columns, String selection,Object[] selectionArgs, String groupBy, String having, String orderBy) {
		return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
	}

	@Override
	public ResultSet query(String table, String[] columns, String selection,Object[] selectionArgs, String groupBy, String having,String orderBy, String limit) {
		return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}

	@Override
	public ResultSet rawQuery(String sql, Object... selectionArgs) throws SQLException {
		PreparedStatement preparedStatement = this.getConnection().prepareStatement(sql);
		if (selectionArgs != null) {
			setStatementParams(selectionArgs, preparedStatement);
		}
		return preparedStatement.executeQuery();
	}

	@Override
	public long insert(String table, String nullColumnHack, ContentValues values) {       
		try {
			return insertOrThrow(table, nullColumnHack, values);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
		InsertTemplate template = TemplateFactory.getInsertTemplate(table);
        if(!applyContentValues(values, template)){
        	 if(nullColumnHack != null && !nullColumnHack.isEmpty()){           
                 template.addData(nullColumnHack, "NULL");
             }
        }
        PreparedSql query = template.buildSql();        
        PreparedStatement preparedStatement = getConnection().prepareStatement(query.getSql(), Statement.RETURN_GENERATED_KEYS);        
        Object[] selectionArgs = query.getValues().toArray();
        setStatementParams(selectionArgs, preparedStatement);
        preparedStatement.executeUpdate();
        return getGeneratedAutoIncremetValue(preparedStatement);
	}
	
	@Override
	public int delete(String table, String whereClause, Object[] whereArgs) {
		try{
			DeleteTemplate template = TemplateFactory.getDeleteTemplate(table, whereClause);       
	        PreparedStatement preparedStatement = getConnection().prepareStatement(template.getSql().toString());
	        setStatementParams(whereArgs, preparedStatement);
	        return preparedStatement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int update(String table, ContentValues values, String whereClause, Object[] whereArgs) {
		try{
			UpdateTemplate template = TemplateFactory.getUpdateTemplate(table,whereClause);
			applyContentValues(values, template);
			PreparedSql query = template.buildSql();
			PreparedStatement preparedStatement = getConnection().prepareStatement(query.getSql());
			Object[] params = concat(query.getValues().toArray(),whereArgs);
			setStatementParams(params, preparedStatement);
			return preparedStatement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int execSQL(String sql) throws SQLException {
		return getConnection().prepareStatement(sql).executeUpdate();
	}

	@Override
	public int execSQL(String sql, Object... bindArgs) throws SQLException {
		PreparedStatement preparedStatement =  getConnection().prepareStatement(sql);
		setStatementParams(bindArgs, preparedStatement);
		return preparedStatement.executeUpdate();
	}

	@Override
	public boolean isOpen() {
		try {
			return !getConnection().isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void setStatementParams(Object[] selectionArgs,	PreparedStatement preparedStatement) throws SQLException {
		for (int i = 1; i < selectionArgs.length + 1; i++) {
			setStatmentParam(preparedStatement, selectionArgs[i - 1],i);
		}
	}
	
	public static long getGeneratedAutoIncremetValue(PreparedStatement ps) throws SQLException {
		List<Object> values = getGeneratedValues(ps);
		return getGeneratedResult(values);
	}	
	
	static boolean applyContentValues(ContentValues values,DataTemplate template) {
		Set<Map.Entry<String, Object>> entrySet = null;
        if (values != null && values.size() > 0) {
            entrySet = values.valueSet();
            Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();
            while(entriesIter.hasNext()){
            	Map.Entry<String, Object> entry = entriesIter.next();
            	template.addData(entry.getKey(), entry.getValue());
            }
            return true;
        }
        return false;
	}
	
	public static List<Object> getGeneratedValues(PreparedStatement preparedStatement)throws SQLException {
		ResultSet generatedValues = preparedStatement.getGeneratedKeys();
		List<Object> generatedKeys = new LinkedList<Object>();
		if (generatedValues != null) {
			while (generatedValues.next()) {
				ResultSetMetaData meta = generatedValues.getMetaData(); 
				int count = meta.getColumnCount();
				for (int i = 1; i <= count; i++) {
					generatedKeys.add(RelationalSqlDatabase.getResultSetValue(generatedValues, i));
				}
			}
			generatedValues.close();
		}
		return generatedKeys;
	}

	private static long getGeneratedResult(List<Object> values) {
		Object generated;
		if(values != null && !values.isEmpty() && (generated = values.get(0)) instanceof Number){
			return ((Number)generated).longValue();
		}
		return -1;
	}
	
	public static void setStatmentParam(PreparedStatement preparedStatement,Object o, int i) throws SQLException {
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
		} else{
			preparedStatement.setObject(i, o);
		}
	}
	
	public static Object getResultSetValue(ResultSet rs, int i) throws SQLException {
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
	
	private static Object[] concat(Object[] A, Object[] B) {
		Object[] C = new Object[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}
	
	@Override
	protected void finalize() throws Throwable {
		closeConnection();
		super.finalize();
	}

	@Override
	public RelationalSqlDatabase initialize(DatabaseContext context) {
		this.schema = context.getProperty("schema");
		transactions = new LinkedList<SqlTransaction>();
		transactionsListeners = new LinkedList<TransactionListener>();
		return this;
	}
	
}