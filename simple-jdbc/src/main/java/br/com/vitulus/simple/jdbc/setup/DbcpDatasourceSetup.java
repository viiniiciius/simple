package br.com.vitulus.simple.jdbc.setup;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import br.com.vitulus.simple.jdbc.util.StringUtils;

public class DbcpDatasourceSetup implements IDBCPDatasourceSetup{

	public enum DBCPDriverAdapterCPDS implements DbcpObjectProvider{
		APACHE_COMMONS(
			"org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS",
			"org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS"
		),
		TOMCAT(
			"org.apache.tomcat.dbcp.dbcp.cpdsadapter.DriverAdapterCPDS",
			"org.apache.tomcat.dbcp.dbcp.cpdsadapter.DriverAdapterCPDS"
		);		
		private String className;
		private String factory;		
		private DBCPDriverAdapterCPDS(String className, String factory) {
			this.className = className;
			this.factory = factory;
		}		
		@Override
		public String getClassName() {
			return className;
		}
		@Override
		public String getClassFactory() {
			return factory;
		}
	}
	
	/**
	 * @see http://commons.apache.org/dbcp/api-1.4/org/apache/commons/dbcp/datasources/SharedPoolDataSource.html
	 *
	 */
	public enum DBCPSharedPool implements DbcpObjectProvider{
		APACHE_COMMONS(
			"org.apache.commons.dbcp.datasources.SharedPoolDataSource",
			"org.apache.commons.dbcp.datasources.SharedPoolDataSourceFactory"
		),
		TOMCAT(
			"org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSource",
			"org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSourceFactory"
		);		
		private String className;
		private String factory;		
		private DBCPSharedPool(String className, String factory) {
			this.className = className;
			this.factory = factory;
		}		
		@Override
		public String getClassName() {
			return className;
		}
		@Override
		public String getClassFactory() {
			return factory;
		}
	}
	
	/**
	 * @see http://commons.apache.org/dbcp/api-1.4/org/apache/commons/dbcp/datasources/PerUserPoolDataSource.html 
	 *
	 */
	public enum DBCPPerUserPool implements DbcpObjectProvider{
		APACHE_COMMONS(
			"org.apache.commons.dbcp.datasources.PerUserPoolDataSource",
			"org.apache.commons.dbcp.datasources.PerUserPoolDataSourceFactory"
		),
		TOMCAT(
			"org.apache.tomcat.dbcp.dbcp.datasources.PerUserPoolDataSource",
			"org.apache.tomcat.dbcp.dbcp.datasources.PerUserPoolDataSourceFactory"
		);		
		private String className;
		private String factory;		
		private DBCPPerUserPool(String className, String factory) {
			this.className = className;
			this.factory = factory;
		}		
		@Override
		public String getClassName() {
			return className;
		}
		@Override
		public String getClassFactory() {
			return factory;
		}
	}
	
	/**
	 * @see http://commons.apache.org/dbcp/api-1.4/org/apache/commons/dbcp/BasicDataSource.html
	 *
	 */
	public enum DBCPBasic implements DbcpObjectProvider{
		APACHE_COMMONS(
			"org.apache.commons.dbcp.BasicDataSource",
			"org.apache.commons.dbcp.BasicDataSourceFactory"
		),
		TOMCAT(
			"org.apache.tomcat.dbcp.dbcp.BasicDataSource",
			"org.apache.tomcat.dbcp.dbcp.datasources.BasicDataSourceFactory"
		);		
		private String className;
		private String factory;		
		private DBCPBasic(String className, String factory) {
			this.className = className;
			this.factory = factory;
		}		
		@Override
		public String getClassName() {
			return className;
		}
		@Override
		public String getClassFactory() {
			return factory;
		}
	}
	
	public enum DBCPPoolType implements DbcpDataSource{
		SHARED_POOL(){
			@Override
			public DbcpObjectProvider from(DbcpProviderType type) {
				return DBCPSharedPool.valueOf(type.toString());
			}
		},
		PER_USER_POOL(){
			@Override
			public DbcpObjectProvider from(DbcpProviderType type) {
				return DBCPPerUserPool.valueOf(type.toString());
			}			
		},
		BASIC(){
			@Override
			public DbcpObjectProvider from(DbcpProviderType type) {
				return DBCPBasic.valueOf(type.toString());
			}			
		};
		
		public static DBCPPoolType from(String type) {
			try{
				return valueOf(type);
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public enum DBCPDriverType implements DbcpDriver{
		ADAPTER_CPDS(){
			@Override
			public DbcpObjectProvider from(DbcpProviderType type) {
				return DBCPDriverAdapterCPDS.valueOf(type.toString());
			}
		}
	}

	public static interface DbcpObjectProvider{
		String getClassName();
		String getClassFactory();
	}	
	
	public interface DbcpDataSource{
		DbcpObjectProvider from(DbcpProviderType type);
	}
	
	public interface DbcpDriver{
		DbcpObjectProvider from(DbcpProviderType type);
	}
	
	public enum DbcpProviderType{
		TOMCAT,
		APACHE_COMMONS;
		
		public static DbcpProviderType from(String type){
			try{
				return valueOf(type);
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private StartupConfig     			config;
	private DBCPDriverType 				driverType;
	private DBCPPoolType 				dataSourceType;
	private DbcpProviderType 			providerType;
	
	protected DbcpDatasourceSetup(StartupConfig config) {
		this(config, DBCPDriverType.ADAPTER_CPDS, DBCPPoolType.BASIC ,DbcpProviderType.APACHE_COMMONS);
	}
	
	protected DbcpDatasourceSetup(StartupConfig config, DBCPDriverType driverType, DBCPPoolType dataSourceType,DbcpProviderType providerType) {
		this.config = config;
		this.driverType = driverType;
		this.dataSourceType = dataSourceType;
		this.providerType = providerType;
	}

	public static IDatasourceSetup setup(StartupConfig config, DbcpProviderType provider, DBCPPoolType pool){
		if (provider == null)
			throw new RuntimeException("O provedor '" + provider + "' é invalido");
		if (pool == null)
			throw new RuntimeException("O tipo de pool '" + pool + "' é invalido como DBCPPoolType");
		return new DbcpDatasourceSetup(
				config,
				DBCPDriverType.ADAPTER_CPDS,
				pool,
				provider
			).setupDriver().setupDatasource();
	}
	
	public static IDatasourceSetup setup(StartupConfig config, String providerType, String poolType){
		DbcpProviderType provider = DbcpProviderType.from(providerType);
		if (provider == null)
			throw new RuntimeException("O provedor '" + provider + "' é invalido");
		DBCPPoolType pool = DBCPPoolType.from(poolType);		
		if (pool == null)
			throw new RuntimeException("O tipo de pool '" + pool + "' é invalido como DBCPPoolType");
		return setup(config, provider, pool);

	}
	
	public DbcpDatasourceSetup setupDatasource(){		
		try {
			return setupDatasource(new InitialContext());
		} catch (NamingException e) {
			throw new RuntimeException("Não foi possivel criar a instancia de InitialContext", e);
		}
	}
	
	public DbcpDatasourceSetup setupDriver() {
		try {
			return setupDriverAdapter(new InitialContext());
		} catch (NamingException e) {
			throw new RuntimeException("Não foi possivel criar a instancia de InitialContext", e);
		}
	}
	
	public DbcpDatasourceSetup setupDriverAdapter(InitialContext ic){
		try {
			DbcpObjectProvider driverProvider = driverType.from(providerType);
			bindDbcpDriver(ic, driverProvider, getName());
		} catch (NamingException e) {
			throw new RuntimeException("Não foi bindar o driver", e);
		}
		return this;
	}
	
	public DbcpDatasourceSetup setupDatasource(InitialContext ic){
		try {
			DbcpObjectProvider poolProvider = dataSourceType.from(providerType);
			bindDbcpPool(ic, poolProvider, getName());
		} catch (NamingException e) {
			throw new RuntimeException("Não foi bindar o datasource", e);
		}	
		return this;
	}	

	public StartupConfig getConfig() {
		return config;
	}

	public DBCPPoolType getDataSourceType() {
		return dataSourceType;
	}
	
	public DbcpProviderType getProviderType() {
		return providerType;
	}
	
	public DBCPDriverType getDriverType() {
		return driverType;
	}	

	@Override
	public String getName() {
		return getConfig().getPoolConfig().getName();
	}

	private void bindDbcpDriver(InitialContext ic, DbcpObjectProvider driverProvider, String jndiName) throws NamingException{
		Context context = setupRoot(ic, jndiName);
		Reference dbcpReference = new Reference(driverProvider.getClassName(), driverProvider.getClassFactory(), null);
		dbcpReference.add(new StringRefAddr("driver", getConfig().getConnConfig().getDriver()));
		dbcpReference.add(new StringRefAddr("url", getConfig().getConnConfig().getUrl()));
		dbcpReference.add(new StringRefAddr("user", getConfig().getConnConfig().getUser()));
		dbcpReference.add(new StringRefAddr("password", getConfig().getConnConfig().getPassword()));
		context.rebind("driver", dbcpReference);
	}
	
	private void bindDbcpPool(InitialContext ic, DbcpObjectProvider poolProvider, String jndiName) throws NamingException{
		Context context = setupRoot(ic, jndiName);
		String driver = jndiName + "/driver";
		Reference poolReference = new Reference(poolProvider.getClassName(), poolProvider.getClassFactory(), null);
		poolReference.add(new StringRefAddr("dataSourceName", driver));
		poolReference.add(new StringRefAddr("maxActive", getConfig().getPoolConfig().getMaxActive()));
		poolReference.add(new StringRefAddr("maxIdle", getConfig().getPoolConfig().getMaxIdle()));
		poolReference.add(new StringRefAddr("maxWait", getConfig().getPoolConfig().getMaxWait()));		
		Properties properties = getConfig().getPoolConfig().getProperties();
		Iterator<String> i = properties.stringPropertyNames().iterator();
		while(i.hasNext()){
			String name = StringUtils.getTrim(i.next());
			if (name.isEmpty())
				continue;
			String value = StringUtils.getTrim(properties.getProperty(name, StringUtils.Empty));
			if (value.isEmpty())
				continue;
			poolReference.add(new StringRefAddr(name, value));
		}
		context.rebind("ds", poolReference);
	}
	
	private Context setupRoot(InitialContext ic, String jndiName) throws NamingException{
		String path = jndiName;
		try {			
			return (Context)ic.lookup(path);
		} catch (NamingException e) {
				Iterator<String> i = Arrays.asList(path.split("/")).iterator();
				Context cc = ic;
				while(i.hasNext()){
					String node = i.next();
					try{
						ic.lookup(node);
					}catch (NamingException e2) {
						cc = cc.createSubcontext(node);
					}					
				}
				return cc;
		}
	}

	@Override
	public DbcpDatasourceSetup setupDriver(InitialContext ic) {
		// TODO Auto-generated method stub
		return null;
	}
	
}