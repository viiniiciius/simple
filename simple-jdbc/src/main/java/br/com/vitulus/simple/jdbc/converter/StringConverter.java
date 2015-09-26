package br.com.vitulus.simple.jdbc.converter;

public interface StringConverter<T> {

	public T getAsObject(String value);
	public String getAsString(T value);
	
}