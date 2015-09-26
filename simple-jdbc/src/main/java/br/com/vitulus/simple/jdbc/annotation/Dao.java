package br.com.vitulus.simple.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.vitulus.simple.jdbc.Entity;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dao {

	Class<? extends Entity> classe();
	String   name() default "";
	
}