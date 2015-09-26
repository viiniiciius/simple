package br.com.vitulus.simple.jdbc.setup;

import javax.naming.InitialContext;

public interface IDatasourceSetup {
	IDatasourceSetup setupDatasource(InitialContext ic);
	String getName();	
}