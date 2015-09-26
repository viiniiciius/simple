package br.com.vitulus.simple.jdbc.orm.old;

import java.lang.reflect.Field;
import java.util.List;
@Deprecated
public interface ProjectionResult {

    List<Field> getProjectionFields();

    List<Field> getProjectionFields(Class<?> classe);

}