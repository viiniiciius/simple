package br.com.vitulus.simple.jdbc.dao;

import br.com.vitulus.simple.jdbc.annotation.Table;
import br.com.vitulus.simple.jdbc.orm.BeanFormat;
import br.com.vitulus.simple.jdbc.orm.RelationalMapping.RelationalField;

interface RelationshipModel<T> {

	public Table getRelationshipTable();

	public String getRelationshipTableName();

	public BeanFormat<T> getRelationshipType();

	public RelationalField getRelationshipField();

}