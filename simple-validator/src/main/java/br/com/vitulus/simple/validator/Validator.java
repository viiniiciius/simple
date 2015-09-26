package br.com.vitulus.simple.validator;

public interface Validator {

	boolean isValid();
	String getErrorMessage();
	Validator getSource();
	Object getValue();
	void setValue(Object value);
	Validator concatBegin(Validator next);
	Validator concat(Validator next);
}