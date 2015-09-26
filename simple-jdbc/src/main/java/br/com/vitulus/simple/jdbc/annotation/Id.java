package br.com.vitulus.simple.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.vitulus.simple.jdbc.annotation.type.IdIncrementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {

    IdIncrementType autoIncrement() default IdIncrementType.NONE;

}