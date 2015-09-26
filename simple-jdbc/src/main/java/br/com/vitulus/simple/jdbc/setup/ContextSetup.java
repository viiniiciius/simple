package br.com.vitulus.simple.jdbc.setup;


public interface ContextSetup {

	public static final String DEFAULT_SCHEMA = "default";
	
	void setupDatasource(String name,String... packages);
	void setup(String name,String... packages);
	void setupDefaultDatasource(String... packages);
	void setupDefault(String... packages);
	public StartupConfig getConfig();
	
}