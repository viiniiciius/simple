package br.com.vitulus.simple.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import br.com.vitulus.simple.jdbc.dao.RelationalSqlDatabase;
import br.com.vitulus.simple.jdbc.template.InsertTemplate;
import br.com.vitulus.simple.jdbc.template.TemplateFactory;
import br.com.vitulus.simple.jdbc.template.AbstractTemplate.PreparedSql;

public class Main {

	private static Connection 					connection;
	
	public static void main(String[] args) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost:3306/jdbc";
		String user = "root";
		String pwd = "12345";
        connection = DriverManager.getConnection(url,user,pwd);
        
        InsertTemplate template = TemplateFactory.getInsertTemplate("destino");
        template.addData("title", "goiania");
        template.addData("descricao", "Eu vou para Goiânia!");
        PreparedSql query = template.buildSql();
        
        PreparedStatement preparedStatement = connection.prepareStatement(query.getSql(), Statement.RETURN_GENERATED_KEYS);        
        Object[] selectionArgs = query.getValues().toArray();
		for (int i = 1; i < selectionArgs.length + 1; i++) {
			RelationalSqlDatabase.setStatmentParam(preparedStatement, selectionArgs[i - 1],i);
		}        
		connection.setAutoCommit(false);
		int isrs = preparedStatement.executeUpdate();
		System.out.println(isrs);
		Long  key = getGeneratedAutoIncremetValue(preparedStatement);
		System.out.println(key);
		connection.setAutoCommit(true);
	}
	
	
	private static Long getGeneratedAutoIncremetValue(PreparedStatement ps) throws SQLException {
		List<Object> values = getGeneratedValues(ps);
		return getGeneratedResult(values);
	}	
	
	private static List<Object> getGeneratedValues(PreparedStatement preparedStatement)throws SQLException {
		ResultSet generatedValues = preparedStatement.getGeneratedKeys();
		List<Object> generatedKeys = new LinkedList<Object>();
		if (generatedValues != null) {
			while (generatedValues.next()) {
				ResultSetMetaData meta = generatedValues.getMetaData(); 
				int count = meta.getColumnCount();
				for (int i = 1; i <= count; i++) {
					generatedKeys.add(RelationalSqlDatabase.getResultSetValue(generatedValues, i));
				}
			}
			generatedValues.close();
		}
		return generatedKeys;
	}

	private static long getGeneratedResult(List<Object> values) {
		Object generated;
		if(values != null && !values.isEmpty() && (generated = values.get(0)) instanceof Number){
			return ((Number)generated).longValue();
		}
		return -1;
	}
	
}