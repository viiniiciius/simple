package br.com.vitulus.simple.jdbc.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;

import br.com.vitulus.simple.jdbc.dao.DaoRegister;
import br.com.vitulus.simple.jdbc.dao.SqliteDatabase;
import br.com.vitulus.simple.jdbc.database.ConnectionFactory;
import br.com.vitulus.simple.jdbc.database.IDatabase;
import br.com.vitulus.simple.jdbc.database.IDatabase.DatabaseContext;
import br.com.vitulus.simple.jdbc.setup.DbcpDatasourceSetup.DBCPPoolType;
import br.com.vitulus.simple.jdbc.setup.DbcpDatasourceSetup.DbcpProviderType;
import br.com.vitulus.simple.jdbc.setup.StartupConfig.PoolConfig;

public abstract class SqliteSetupHelper implements ContextSetup{	
	
	public static final String PREFIX = "jdbc:sqlite:";
	
	private StartupConfig 		config;
	private String 				databaseName;
	private int 				mNewVersion;
	
	
	public SqliteSetupHelper(String databaseName, int databaseVersion, StartupConfig config) {
		this.databaseName = databaseName;
		this.mNewVersion = databaseVersion;
		this.config = config;
	}
	
	public SqliteSetupHelper(String databaseName, int databaseVersion, Properties p) {
		this(databaseName,databaseVersion,StartupConfig.getConfigProperties(p));
	}	
	
	public SqliteSetupHelper(String databaseName, int databaseVersion) {
		this(databaseName,databaseVersion,getBaseConfigProperties(databaseName));
	}	
	
	public SqliteSetupHelper(String databaseName, int databaseVersion,PoolConfig poolConfig) {
		this(databaseName,databaseVersion,getBaseConfigProperties(databaseName));
		this.config.poolConfig = poolConfig;
	}
	
	public static Properties getBaseConfigProperties(String databaseName){
		Properties p = new Properties();
    	p.setProperty("conn.driver", "org.sqlite.JDBC");
    	p.setProperty("conn.url", PREFIX + databaseName);
    	return p;
	}
	
	@Override
	public void setupDatasource(String name, String... packages) {
		this.setupDbcpDatasource(name,DbcpProviderType.APACHE_COMMONS, DBCPPoolType.BASIC, packages);
	}
	
	public void setupDbcpDatasource(String name,DbcpProviderType providerType,DBCPPoolType poolType,String... packages) {
		this.setup(name, packages);
		DbcpDatasourceSetup.setup(config, providerType, poolType);
	}

	@Override
	public void setup(String schema, String... packages) {		
		ConnectionFactory factory = ConnectionFactory.create(schema, config);
		if(packages != null && packages.length > 0){
			DaoRegister daoSetup = new DaoRegister(schema,packages);
			daoSetup.registerEntityManagers();			
		}
		factory.addConnectionListener(new SqliteConnectionListener());
		DatabaseContext context = new DatabaseContext();
		context.setProperty("class", SqliteDatabase.class);
		context.setProperty("schema", schema);
		context.setProperty("path", new File(databaseName));
		IDatabase.DatabaseFactory.registerSchema(schema, context);
        SqliteDatabase db = null;
        try {            
            db = IDatabase.DatabaseFactory.getDatabase(schema);
            onOpen(db);
            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > mNewVersion) {
                            System.out.println("Can't downgrade database from version " +
                                    version + " to " + mNewVersion + ": " + db.getPath().getAbsolutePath());
                        }
                        onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }            
        } finally {
        	try { db.close(); } catch (Exception e) { }
        }		
	}

	@Override
	public void setupDefaultDatasource(String... packages) {
		this.setupDatasource(ContextSetup.DEFAULT_SCHEMA,packages);
	}


	@Override
	public void setupDefault(String... packages) {
		this.setup(ContextSetup.DEFAULT_SCHEMA, packages);
	}

	public StartupConfig getConfig() {
		return config;
	}
	
	protected void createFromDDL(IDatabase db,String ddl) {
		db.beginTransaction();
		try {			
			String[] ddls  = ddl.split(";");
			for(String sql : ddls){
				if(sql.trim().length() > 0){
					db.execSQL(sql);
				}				
			}
			buildPrimaryData(db);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.endTransaction();
		}		
	}
	
	protected void createFromDDL(IDatabase db,InputStream in) {
		String ddl;
		try {
			ddl = readInputStreamToString(in);
			createFromDDL(db,ddl);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	protected void buildPrimaryData(IDatabase db) {
		
	}
	
	public static String readInputStreamToString(InputStream in) throws IOException{
		StringBuilder stb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line = reader.readLine()) != null){
			stb.append(line).append("\r\n");
		}
		return stb.toString();
	}
	
	public abstract void onCreate(IDatabase database);
	public abstract void onUpgrade(IDatabase database,int oldVersion, int newVersion);
	public abstract void onOpen(IDatabase database);
		
	public class SqliteDatasourceSetup implements IDatasourceSetup{
		@Override
		public IDatasourceSetup setupDatasource(InitialContext ic) {
			return null;
		}
		@Override
		public String getName() {
			return null;
		}		
	}
	
	private static class SqliteConnectionListener implements ConnectionFactory.ConnectionListener{
		@Override
		public void onAfterCreateOpenConnection(Connection c, Object source) {
			try {
				c.prepareStatement("PRAGMA foreign_keys=true").executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void onBeforeCloseOpenConnection(Connection c, Object source) {			
		}		
	}
	

}