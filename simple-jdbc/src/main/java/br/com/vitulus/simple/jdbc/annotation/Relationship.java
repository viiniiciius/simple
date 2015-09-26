package br.com.vitulus.simple.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.vitulus.simple.jdbc.annotation.type.Cardinality;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Relationship {
	
	ForwardKey[] joinFields() default {};
	Cardinality cardinality();
}