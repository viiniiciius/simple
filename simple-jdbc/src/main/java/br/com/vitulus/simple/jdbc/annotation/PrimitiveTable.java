package br.com.vitulus.simple.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimitiveTable {
	Table table() default @Table(name = "");
	Column value() default @Column(name="value");	
	
}