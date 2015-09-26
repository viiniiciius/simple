package br.com.vitulus.simple.jdbc.sql;

import java.io.IOException;



public interface SqlElement {

	void append(Appendable p) throws IOException;
	
}