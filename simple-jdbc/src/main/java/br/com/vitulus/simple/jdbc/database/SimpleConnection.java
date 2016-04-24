package br.com.vitulus.simple.jdbc.database;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class SimpleConnection implements Connection {

	private Connection enclosing;
	private ConnectionFactory f;
	
	public SimpleConnection(Connection enclosing, ConnectionFactory f) {
		this.enclosing = enclosing;
		this.f = f;
	}
	
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return enclosing.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return enclosing.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		return enclosing.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return enclosing.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return enclosing.prepareCall(sql);
	}

	public String nativeSQL(String sql) throws SQLException {
		return enclosing.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		enclosing.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		return enclosing.getAutoCommit();
	}

	public void commit() throws SQLException {
		enclosing.commit();
	}

	public void rollback() throws SQLException {
		enclosing.rollback();
	}

	public void close() throws SQLException {
		try{
			f.onBeforeCloseConnecion(enclosing);
		}finally{
			try {
				enclosing.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isClosed() throws SQLException {
		return enclosing.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return enclosing.getMetaData();
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		enclosing.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		return enclosing.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		enclosing.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return enclosing.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		enclosing.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return enclosing.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return enclosing.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		enclosing.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return enclosing.createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return enclosing.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return enclosing.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return enclosing.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		enclosing.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		enclosing.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		return enclosing.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		return enclosing.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return enclosing.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		enclosing.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		enclosing.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return enclosing.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return enclosing.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return enclosing.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return enclosing.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return enclosing.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return enclosing.prepareStatement(sql, columnNames);
	}

	public Clob createClob() throws SQLException {
		return enclosing.createClob();
	}

	public Blob createBlob() throws SQLException {
		return enclosing.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return enclosing.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return enclosing.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return enclosing.isValid(timeout);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		enclosing.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		enclosing.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return enclosing.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return enclosing.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return enclosing.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return enclosing.createStruct(typeName, attributes);
	}

	public void setSchema(String schema) throws SQLException {
		enclosing.setSchema(schema);
	}

	public String getSchema() throws SQLException {
		return enclosing.getSchema();
	}

	public void abort(Executor executor) throws SQLException {
		enclosing.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		enclosing.setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException {
		return enclosing.getNetworkTimeout();
	}

}
