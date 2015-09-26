package br.com.vitulus.simple.jdbc.database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import br.com.vitulus.simple.jdbc.setup.ContextSetup;
import br.com.vitulus.simple.jdbc.setup.StartupConfig;

public class ConnectionFactory implements Serializable {

	private static final long serialVersionUID = 1566977400293412673L;

	private  static Map<String,ConnectionFactory>	instances;
	private  		ThreadLocal<Connection>    		session;
	private  		StartupConfig              		config;
	private         List<ConnectionListener>        connectionListeners;
	
	static {
		instances = new HashMap<String, ConnectionFactory>();		
	}

	private ConnectionFactory() {
		connectionListeners = new ArrayList<ConnectionFactory.ConnectionListener>();
		session = new ThreadLocal<Connection>();
	}

	public static ConnectionFactory registerSchema(String schema,StartupConfig config){
		ConnectionFactory ins;
		(ins = getInstance(schema)).config = config;return ins;		
	}
	
	public static ConnectionFactory getInstance() {
		return getInstance(ContextSetup.DEFAULT_SCHEMA);
	}
	
	public static ConnectionFactory getInstance(String schema) {
		ConnectionFactory instance;
		if((instance = instances.get(schema)) == null){
			instances.put(schema, instance = new ConnectionFactory());
		}
		return instance;
	}

	public static Collection<String> getSchemasName(){
		return instances.keySet();
	}
	
	/**
	 * Cria uma nova Conexao caso o objeto connection seja null ou esteja
	 * fechado.
	 * 
	 * @return Um objeto Connection
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception {
		Connection connection = session.get();
		if (!isConnectionActive()) {
			connection = buildConnection();
			session.set(connection);
		}
		return connection;
	}

	private Connection buildConnection() throws SQLException {
		Connection c = null;
		try{
			if(config.getConnConfig().isUsePool()){
				return c = buildPoolConnection();
			}else{
				return c = buildLocalConnection();
			}
		}finally{
			onAfterCreateConnection(c);
		}
	}

	private Connection buildPoolConnection(){
		try {
			Context initCtx = new InitialContext();
			String ctxName = config.getPoolConfig().getName() + "/ds";
			Connection connection = ((DataSource) initCtx.lookup(ctxName)).getConnection();
			return connection;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private Connection buildLocalConnection(){
        try {
    		Class.forName(config.getConnConfig().getDriver());
    		String url = config.getConnConfig().getUrl();
    		String user = config.getConnConfig().getUser();
    		String pwd = config.getConnConfig().getPassword();
            Connection connection = DriverManager.getConnection(url,user,pwd);
            return connection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Fecha a conexao atual.
	 */
	public void closeConnection() {
		Connection connection = session.get();
		try {
			if (isConnectionActive()) {
				if (!connection.getAutoCommit()) {
					connection.commit();
				}
				onBeforeCloseConnecion(connection);
				connection.close();
			}
			session.set(null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnectionActive() {
		Connection connection = session.get();
		try {
			return (connection != null && !connection.isClosed());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public StartupConfig getConfig() {
		return config;
	}
	
	public void addConnectionListener(ConnectionListener listener){
		connectionListeners.add(listener);
	}
	
	public void removeConnectionListener(ConnectionListener listener){
		connectionListeners.remove(listener);
	}
	
	public Collection<ConnectionListener> getConnectionListeners(){
		return Collections.unmodifiableList(connectionListeners);
	}
	
	public void onBeforeCloseConnecion(Connection c){
		for(ConnectionListener l : connectionListeners){
			l.onBeforeCloseOpenConnection(c, this);
		}
	}

	public void onAfterCreateConnection(Connection c){
		for(ConnectionListener l : connectionListeners){
			l.onAfterCreateOpenConnection(c, this);
		}
	}
	
	public static interface ConnectionListener{
		public void onAfterCreateOpenConnection(Connection c,Object source);
		public void onBeforeCloseOpenConnection(Connection c,Object source);
	}

	
}