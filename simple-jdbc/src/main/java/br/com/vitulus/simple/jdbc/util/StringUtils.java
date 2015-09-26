package br.com.vitulus.simple.jdbc.util;

import java.io.UnsupportedEncodingException;

public class StringUtils {

	public static final String Empty 			= "";
	
	public static String getTrim(String value) {
		return value == null ? Empty : value.trim();
	}

	public static String getTrim(String value, String defaultIfNull) {
		if (value != null)
			return value.trim();
		return getTrim(defaultIfNull);		
	}
	
	public static String getTrim(String value, String defaultIfNull, String encoding) throws UnsupportedEncodingException{
		if (value != null)
			return new String(value.trim().getBytes(), "UTF-8");
		return new String(getTrim(defaultIfNull).getBytes(), "UTF-8");		
	}
	
	private static boolean isTrimmedEmpty(String value){
		int index = 0; final int len = value.length();
		while(index < len && value.charAt(index) == ' ') 
			index++;
		return index == len;
	}
	
	public static boolean isNullOrTrimEmpty(String value) {
		return value == null || isTrimmedEmpty(value);
	}
	
}