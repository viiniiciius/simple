package br.com.vitulus.simple.jdbc.converter;


public class IntegerStringConverter implements StringConverter<Integer> {

	@Override
	public String getAsString(Integer value) {
		return value.toString();
	}

	@Override
	public Integer getAsObject(String value) {
		return Integer.valueOf(value);
	}

}