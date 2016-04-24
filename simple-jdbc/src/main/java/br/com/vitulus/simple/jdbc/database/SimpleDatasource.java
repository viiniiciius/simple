package br.com.vitulus.simple.jdbc.database;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class SimpleDatasource implements DataSource {

	private DataSource enclosing;
	private ConnectionFactory f;

	public SimpleDatasource(DataSource enclosing, ConnectionFactory f) {
		this.enclosing = enclosing;
		this.f = f;
	}
	
	public PrintWriter getLogWriter() throws SQLException {
		return enclosing.getLogWriter();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return enclosing.unwrap(iface);
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		enclosing.setLogWriter(out);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return enclosing.isWrapperFor(iface);
	}

	public Connection getConnection() throws SQLException {
		Connection c = enclosing.getConnection();
		try{
			f.onAfterCreateConnection(c);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return c;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		enclosing.setLoginTimeout(seconds);
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		return enclosing.getConnection(username, password);
	}

	public int getLoginTimeout() throws SQLException {
		return enclosing.getLoginTimeout();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return enclosing.getParentLogger();
	}

	

}
