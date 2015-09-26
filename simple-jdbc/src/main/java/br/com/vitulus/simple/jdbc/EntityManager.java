package br.com.vitulus.simple.jdbc;

import java.sql.SQLException;
import java.util.List;

/**
 * @author vinicius.rodrigues
 * 
 * @param <T>
 */
@Deprecated
public interface EntityManager<T extends Entity> {

    void inserir(T o) throws Exception;

    void alterar(T o) throws Exception;

    void remover(T o) throws Exception;

    void load(T o) throws Exception;

    boolean exists(T o)throws SQLException;
   
    List<T> listar();
    
    List<T> listar(boolean lazy,String... filds);    

    SqlExecutor<T> getSqlExecutor();

    void closeConnection();

}