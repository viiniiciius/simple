package br.com.vitulus.simple.jdbc;

import java.lang.reflect.Proxy;

import org.apache.commons.beanutils.PropertyUtilsBean;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class DynamicClass {

	private static final PropertyUtilsBean reflx = BeanProperty.instance();
	
	public static void main(String[] args) throws Exception{
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass("br.com.dataeasy.scratch.ApplianceModel.warnings.String");
		CtClass centity = pool.getOrNull("br.com.simple.jdbc.Entity");
		
		cc.addInterface(centity);
		CtField id = new CtField(CtClass.longType, "id", cc);
		cc.addMethod(CtNewMethod.getter("getId", id));
		cc.addMethod(CtNewMethod.setter("setId", id));
		
		cc.addField(id);
		
		Class<Entity> clazz = cc.toClass();
		
		System.out.println(Entity.class.isAssignableFrom(clazz));
		System.out.println(Proxy.isProxyClass(clazz));
		
		Entity e = clazz.newInstance();
		reflx.setNestedProperty(e, "id", 50l);
		
		System.out.println(reflx.getNestedProperty(e, "id"));
		
		System.out.println(e);
		
	}
	
}