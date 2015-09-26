package br.com.vitulus.simple.jdbc.converter;


public class EnumStringConverter<T extends Enum<T>> implements StringConverter<T> {

	private Class<T> classe;
	
	public EnumStringConverter(Class<T> classe){
		this.classe = classe;
	}
	
	@Override
	public T getAsObject(String value) {
		try{
		    return Enum.valueOf(classe, value);
		}catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getAsString(T value) {
		return value.name();
	}
	

}