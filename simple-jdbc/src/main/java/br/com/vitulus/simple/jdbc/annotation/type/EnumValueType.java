package br.com.vitulus.simple.jdbc.annotation.type;

public enum EnumValueType {

    ORDINAL(int.class), NAME(String.class);

    public Class<?> valueClass;

	private EnumValueType(Class<?> valueClass) {
		this.valueClass = valueClass;
	}
    
}