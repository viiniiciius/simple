package br.com.vitulus.simple.jdbc.converter;


public class LongStringConverter implements StringConverter<Long> {

	@Override
	public String getAsString(Long value) {
		return value.toString();
	}

	@Override
	public Long getAsObject(String value) {
		return Long.parseLong(value);
	}

}