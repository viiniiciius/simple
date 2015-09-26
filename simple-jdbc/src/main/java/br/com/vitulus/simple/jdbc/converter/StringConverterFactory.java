package br.com.vitulus.simple.jdbc.converter;


public class StringConverterFactory {

	private static StringConverter<Integer>       integerConv;
	private static StringConverter<Long>          longConv;
	
	static{
		integerConv = new IntegerStringConverter();
		longConv = new LongStringConverter();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes" })
	public static <T> StringConverter<T> getConverter(Class<T> type){
		if(type.equals(Integer.class)){
			return (StringConverter<T>)integerConv;
		}else if(type.equals(Long.class)){
			return (StringConverter<T>) longConv;
		}else if(type.isEnum()){
			return new EnumStringConverter(type);
		}else{
			return null;
		}
	}
	
}