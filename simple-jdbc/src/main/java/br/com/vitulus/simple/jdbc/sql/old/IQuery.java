package br.com.vitulus.simple.jdbc.sql.old;

import java.util.List;
@Deprecated
public interface IQuery {

    public abstract String getQuery();

    public abstract List<Object> getParams();

    public abstract String getTable();

}