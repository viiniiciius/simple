package br.com.vitulus.simple.jdbc;

import org.apache.commons.beanutils.PropertyUtilsBean;

public class BeanProperty {

	public static PropertyUtilsBean propertyUtilsBean;
	
	static{
	    propertyUtilsBean = new PropertyUtilsBean();
	}
	
	public static PropertyUtilsBean instance(){
		return propertyUtilsBean;
	}
	
}