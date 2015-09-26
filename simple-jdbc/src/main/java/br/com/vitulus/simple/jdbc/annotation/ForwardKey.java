package br.com.vitulus.simple.jdbc.annotation;

public @interface ForwardKey {
    String tableField();
    String foreginField();
}