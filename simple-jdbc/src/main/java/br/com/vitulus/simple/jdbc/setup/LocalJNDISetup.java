package br.com.vitulus.simple.jdbc.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class LocalJNDISetup extends SimpleSetup{
	
	private IDBCPDatasourceSetup 	datasourceSetup;
	
	public LocalJNDISetup(InputStream in, ConfigFileType type) {
		this(type.getConfig(in));
	}
	
	public LocalJNDISetup(File file, ConfigFileType type) throws FileNotFoundException {
		this(new FileInputStream(file), type);
	}
	
	public LocalJNDISetup(String file, ConfigFileType type)	throws FileNotFoundException {
		this(new File(file), type);
	}

	public LocalJNDISetup(StartupConfig config,IDBCPDatasourceSetup datasourceSetup) {
		super(config);
		this.datasourceSetup = datasourceSetup;
	}
	
	public LocalJNDISetup(StartupConfig config) {
		super(config);
		if(config.usePool()){
			datasourceSetup = new DbcpDatasourceSetup(config);
		}
	}
	
	@Override
	public void setup(String schema,String... packages) {
		super.setup(schema,packages);
		if(datasourceSetup != null){
			setupJndi();
		}		
	}
	
	private void setupJndi(){		
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
	    System.setProperty(Context.PROVIDER_URL, "file:///.");
	    try {
			InitialContext ic = new InitialContext();
			datasourceSetup.setupDriver(ic);
			datasourceSetup.setupDatasource(ic);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}	
}