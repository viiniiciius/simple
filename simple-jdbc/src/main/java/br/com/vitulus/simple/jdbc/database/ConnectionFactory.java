package br.com.vitulus.simple.jdbc.database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import br.com.vitulus.simple.jdbc.setup.StartupConfig;

public class ConnectionFactory implements Serializable {

	private static final long serialVersionUID = 1566977400293412673L;

	private  static Map<String,ConnectionFactory>	instances;
	private  		StartupConfig              		config;
	private 		String 							jndiPath;
	private         boolean							containerManaged;
	private         List<ConnectionListener>        connectionListeners;
	
	static {
		instances = new HashMap<String, ConnectionFactory>();		
	}

	static {
		instances = new HashMap<String, ConnectionFactory>();		
	}

	private ConnectionFactory() {		
		connectionListeners = new ArrayList<ConnectionFactory.ConnectionListener>();
	}

	public static ConnectionFactory create(String schema, String jndiPath){
		ConnectionFactory instance = getInstance(schema);
		instance.containerManaged = true;
		instance.jndiPath = jndiPath;
		return instance;
	}
	
	public static ConnectionFactory create(String schema, StartupConfig config){
		ConnectionFactory instance = getInstance(schema);
		instance.config = config;
		instance.jndiPath = config.getPoolConfig().getName() + "/ds";
		return instance;
	}

	public static void clearAll(){
		instances.clear();
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
	 * Recupera/Cria uma nova Conexao a critério do pool de conexões.
	 * 
	 * @return Um objeto Connection
	 * @throws Exception
	 */
	public Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}	

	public DataSource getDataSource() throws SQLException{		
		DataSource datasource;
		try {
			datasource = (DataSource)
				new InitialContext().lookup(jndiPath);
		} catch (NamingException e) {
			throw new SQLException("Não foi possível encontrar o datasource", e);
		}
		if (datasource == null)
			throw new SQLException("Não foi possível encontrar o datasource (datasource null)");
		return new SimpleDatasource(datasource, this);
	}	

	public StartupConfig getConfig() {
		return config;
	}
	
	public boolean isContainerManaged() {
		return containerManaged;
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
	
	void onBeforeCloseConnecion(Connection c){
		for(ConnectionListener l : connectionListeners){
			l.onBeforeCloseOpenConnection(c, this);
		}
	}

	void onAfterCreateConnection(Connection c){
		for(ConnectionListener l : connectionListeners){
			l.onAfterCreateOpenConnection(c, this);
		}
	}
	
	public static interface ConnectionListener{
		public void onAfterCreateOpenConnection(Connection c,Object source);
		public void onBeforeCloseOpenConnection(Connection c,Object source);
	}
	
}