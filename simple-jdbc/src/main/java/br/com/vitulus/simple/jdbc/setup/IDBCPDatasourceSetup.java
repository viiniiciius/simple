package br.com.vitulus.simple.jdbc.setup;

import javax.naming.InitialContext;

public interface IDBCPDatasourceSetup extends IDatasourceSetup{
	DbcpDatasourceSetup setupDriver(InitialContext ic);
}