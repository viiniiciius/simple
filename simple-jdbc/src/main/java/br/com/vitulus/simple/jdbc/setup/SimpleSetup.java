package br.com.vitulus.simple.jdbc.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import br.com.vitulus.simple.jdbc.dao.DaoRegister;
import br.com.vitulus.simple.jdbc.database.ConnectionFactory;
import br.com.vitulus.simple.jdbc.setup.DbcpDatasourceSetup.DBCPPoolType;
import br.com.vitulus.simple.jdbc.setup.DbcpDatasourceSetup.DbcpProviderType;

public class SimpleSetup implements ContextSetup{
	
	public enum ConfigFileType{
		PROPERTIES {
			@Override
			public StartupConfig getConfig(InputStream in) {
				return StartupConfig.getConfigProperties(in);
			}
		},
		XML {
			@Override
			public StartupConfig getConfig(InputStream in) {
				return StartupConfig.getConfig(in);
			}
		};
		public abstract StartupConfig getConfig(InputStream in);
	}
	
	private StartupConfig config;
	
	public SimpleSetup(StartupConfig config) {
		this.config = config;
	}	
	
	public SimpleSetup(InputStream in,ConfigFileType type) {
		this(type.getConfig(in));
	}	
	
	public SimpleSetup(File file,ConfigFileType type) throws FileNotFoundException {
		this(new FileInputStream(file),type);
	}
	
	public SimpleSetup(String file,ConfigFileType type) throws FileNotFoundException {
		this(new File(file),type);		
	}
	
	public StartupConfig getConfig() {
		return config;
	}
	
	public void setup(String schema,String... packages){
		ConnectionFactory.registerSchema(schema, config);
		if(packages != null && packages.length > 0){
			DaoRegister daoSetup = new DaoRegister(schema,packages);
			daoSetup.registerEntityManagers();			
		}
	}

	@Override
	public void setupDefault(String... packages) {
		this.setup(ContextSetup.DEFAULT_SCHEMA, packages);
	}

	@Override
	public void setupDatasource(String name, String... packages) {
		this.setup(name, packages);
		DbcpDatasourceSetup.setup(config, DbcpProviderType.APACHE_COMMONS, DBCPPoolType.BASIC);
	}

	@Override
	public void setupDefaultDatasource(String... packages) {
		this.setupDatasource(ContextSetup.DEFAULT_SCHEMA,packages);
	}	
	
}