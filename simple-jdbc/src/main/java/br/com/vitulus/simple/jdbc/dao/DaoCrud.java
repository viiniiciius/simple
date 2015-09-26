package br.com.vitulus.simple.jdbc.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.PrimitiveEntity;
import br.com.vitulus.simple.jdbc.annotation.ForwardKey;
import br.com.vitulus.simple.jdbc.annotation.Table;
import br.com.vitulus.simple.jdbc.dao.DaoEntity.EntityReader;
import br.com.vitulus.simple.jdbc.database.ContentValues;
import br.com.vitulus.simple.jdbc.database.IDatabase;
import br.com.vitulus.simple.jdbc.orm.BeanFormat;
import br.com.vitulus.simple.jdbc.orm.ObjectMapping;
import br.com.vitulus.simple.jdbc.orm.OrmResolver;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.EntityFormater;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.EntityParser;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.QueryResultParser;
import br.com.vitulus.simple.jdbc.orm.ObjectMapping.ObjectField;
import br.com.vitulus.simple.jdbc.orm.RelationalMapping.RelationalField;
import br.com.vitulus.simple.jdbc.sql.SqlColumn;
import br.com.vitulus.simple.jdbc.sql.SqlTable;
import br.com.vitulus.simple.jdbc.sql.SqlJoin.JoinType;
import br.com.vitulus.simple.jdbc.sql.SqlOrderByClausule.ORDERING_MODE;
import br.com.vitulus.simple.jdbc.sql.expression.BinaryExpression;
import br.com.vitulus.simple.jdbc.sql.expression.ColumnExpression;
import br.com.vitulus.simple.jdbc.sql.expression.LiteralValueExpression;
import br.com.vitulus.simple.jdbc.sql.expression.SqlExpression.BinaryOperator;
import br.com.vitulus.simple.jdbc.template.DeleteTemplate;
import br.com.vitulus.simple.jdbc.template.SelectTemplate;
import br.com.vitulus.simple.jdbc.template.TemplateFactory;
import br.com.vitulus.simple.jdbc.template.UpdateTemplate;
import br.com.vitulus.simple.jdbc.template.AbstractTemplate.ExpressionBuilder;
import br.com.vitulus.simple.jdbc.template.AbstractTemplate.PreparedSql;

public class DaoCrud<T extends Entity> implements DaoEntity<T>, EntityReader<T> {

	public final TypeCursorParser 		TYPE_PARSER 		= new TypeCursorParser();
	public final UnTypeCursorParser 	UNTYPE_PARSER 		= new UnTypeCursorParser();
	public final ExistsParser 			EXISTS_PARSER 		= new ExistsParser();
	
	private BeanFormat<T> 				type;
	private IDatabase 					db;
	private List<DatabaseListener>		databaselisteners;
	private List<EntityParseListener>	entityParserlisteners;	
	
	public DaoCrud(BeanFormat<T> type) {
		this.type = type;
		this.databaselisteners = new ArrayList<DatabaseListener>();
		this.entityParserlisteners = new ArrayList<EntityParseListener>();
	}
	
	@Override
	public DaoCrud<T> whith(IDatabase db){
		this.db = db;
		return this;
	}
	
	@Override
	public boolean exists(long rowId) {
		SelectTemplate template = getBaseTemplateSelectionUnique(rowId,(String[])null);
		return getResult(template,EXISTS_PARSER);
	}
	
	public boolean exists(T example, boolean justKey) {
		SelectTemplate template = getBaseTemplateSelectionUnique(example,justKey,(String[])null);			
		return getResult(template,EXISTS_PARSER);
	}
	
	@Override
	public Map<String, Object> getUntyped(long rowId) {		
		return getUntyped(rowId, getAllFieldNames());
	}

	@Override
	public Map<String, Object> getUntyped(long rowId, String... projection) {
		SelectTemplate template = getBaseTemplateSelectionUnique(rowId,projection);
		return getResult(template,UNTYPE_PARSER);
	}

	@Override
	public Map<String, Object> getUntyped(T example, boolean justKey) {
		return getUntyped(example, justKey,getAllFieldNames());
	}
	
	@Override
	public Map<String, Object> getUntyped(T example, boolean justKey, String... projection) {
		SelectTemplate template = getBaseTemplateSelectionUnique(example,justKey,projection);
		return getResult(template,UNTYPE_PARSER);
	}

	@Override
	public T get(long rowId) {
		return get(rowId,getAllFieldNames());
	}

	@Override
	public T get(long rowId, String... projection) {
		SelectTemplate template = getBaseTemplateSelectionUnique(rowId,projection);		
		return getResult(template,TYPE_PARSER);
	}
	
	@Override
	public T get(T example, boolean justKey) {
		return get(example,justKey,getAllFieldNames());
	}	

	@Override
	public T get(T example, boolean justKey, String... projection) {
		SelectTemplate template = getBaseTemplateSelectionUnique(example,justKey,projection);		
		return getResult(template,TYPE_PARSER);
	}

	public SelectTemplate getBaseTemplateSelectionUnique(long rowId, String... projection) {		
		SelectTemplate template = getBaseTemplateSelection(0,0,null,null,projection);
		template.getWhereBuilder().appendEqual(getFullSupposedIdSqlColumn(), rowId);
		return template;
	}
	
	public SelectTemplate getBaseTemplateSelectionUnique(T example, boolean justKey,String... projection) {
		SelectTemplate template = getBaseTemplateSelection(0,0,null,null,projection);
		buildWhereByExample(example, justKey, template.getWhereBuilder());
		return template;
	}
	
	public SelectTemplate getBaseTemplateSelection(int offset, int limit, String orderBy,ORDERING_MODE mode,String... projection) {
		SelectTemplate template = TemplateFactory.getSelectTemplate(getTable().name());
		buildListDetails(offset, limit, orderBy , mode, template);
		buildProjection(template, projection);
		return template;
	}
	
	public ResultSet getRaw(SelectTemplate template) throws SQLException {
		PreparedSql query = template.buildSql();
		String sql = query.getSql().toString();
		Object[] params = query.getValues().toArray();
		return db.rawQuery(sql, params);
	}
	
	public void buildProjection(SelectTemplate template, String... projection) {
		buildProjection(this.type,template,projection);
	}
	
	public static void buildProjection(BeanFormat<?> type,SelectTemplate template, String... projection) {
		if(projection != null && projection.length > 0){
			for(String field : projection){
				if(field.equals("*")){
					buildProjection(type ,template, getAllFieldNames(type));
				}else{
					template.addProjection(getFullSqlColumn(type,field));
				}				
			}
		}else{
			template.addProjection(new LiteralValueExpression(1, false));
		}
	}
	
	public void buildWhereByExample(T example, boolean justKey, ExpressionBuilder whereBuilder) {
		BeanFormater f = new BeanFormater();
		if(justKey){
			type.formatKey(example, f);
		}else{
			type.formatEscapeDefaults(example, f);
		}
		Iterator<Entry<String, Object>> i =	f.initialValues.get(example.getClass()).valueSet().iterator(); //TODO Tratar herannça
		while(i.hasNext()){
			Entry<String, Object> e = i.next();
			SqlTable owner = getSqlTable();
			SqlColumn column = new SqlColumn(owner, e.getKey());
			whereBuilder.appendEqual(column , e.getValue());
			if(i.hasNext()){
				whereBuilder.AND();
			}
		}
	}	
	
	public <E extends Entity> E loadBeanRelationship(T o,String name,String... projection){		
		BeanSubTypeRelationship<E> relationship = new BeanSubTypeRelationship<E>(name);
		SelectTemplate template = getBaseTemplateSelectionRelationship(o,relationship, projection);
		return getResult(template,new SubTypeCursorParser<E>(relationship.getRelationshipType()));
	}

	public Collection<?> loadCollectionRelationship(T o,String name,String... projection){		
		BeanSubTypeRelationship<Entity> relationship = new BeanSubTypeRelationship<Entity>(name);
		SelectTemplate template = getBaseTemplateSelectionRelationship(o,relationship, projection);
		return getResultAsList(template,new SubTypeCursorParser<Entity>(relationship.getRelationshipType()));
	}	
	
	@SuppressWarnings("unchecked")
	public <E> Collection<E> loadPrimitiveCollectionRelationship(T o,String name){
		BeanSubTypeRelationship<PrimitiveEntity> relationship = new BeanSubTypeRelationship<PrimitiveEntity>(name);
		String projection = relationship.targetRelField.getPrimitiveTable().value().name();
		SelectTemplate template = getBaseTemplateSelectionRelationship(o,relationship, projection);
		return (Collection<E>)getResultAsPrimitiveList(template,new SubTypeCursorParser<PrimitiveEntity>(relationship.getRelationshipType()));
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Entity> BeanFormat<E> resolveSubTypeBeanFormat(String name) {		
		ObjectField objTarget =  type.getDescriptor().getObjectByPath(name);
		if(objTarget == null){
			throw new IllegalStateException("O atributo '" + name + "' Não existe em : " + this.type.getDescriptor().getObjectMapping().getClasse());
		}
		if(objTarget.isCollection()){
			try {
				return OrmResolver.getRelationshipBeanFormat((Class<E>)type.getDescriptor().getObjectMapping().getClasse(),name);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}				
		if(!Entity.class.isAssignableFrom(objTarget.getType())){
			throw new IllegalStateException("'" + name + "' Não é um relacionamento válido.");
		}
		return OrmResolver.getBeanFormat((Class<E>)objTarget.getType());
	}	
	
	public SelectTemplate getBaseTemplateSelectionJoinedRelationship(String name, String... projection) {		
		return getBaseTemplateSelectionJoinedRelationship(
				new BeanSubTypeRelationship<Entity>(name),
				projection);
	}

	public SelectTemplate getBaseTemplateSelectionRelationship(T o,String name, String... projection) {		
		return getBaseTemplateSelectionRelationship(
				o,
				new BeanSubTypeRelationship<Entity>(name),
				projection);
	}
	
	public SelectTemplate getBaseTemplateSelectionJoinedRelationship(RelationshipModel<?> relationship, String... projection) {		
		SelectTemplate template = TemplateFactory.getSelectTemplate(relationship.getRelationshipTableName());
		buildProjection(template, projection);
		buildJoinByForeginKey(relationship.getRelationshipField(), relationship.getRelationshipTableName(), template);
		return template;
	}

	public SelectTemplate getBaseTemplateSelectionRelationship(T o,RelationshipModel<?> relationship, String... projection) {		
		SelectTemplate template = TemplateFactory.getSelectTemplate(relationship.getRelationshipTableName());
		buildProjection(relationship.getRelationshipType(),template, projection);
		buildWhereByForeginKey(o,relationship.getRelationshipField(), template);
		return template;
	}
	
	public void buildWhereByForeginKey(T o,RelationalField relTarget,SelectTemplate template){
		ForwardKey[] keys = relTarget.getRelationship().joinFields();
		buildWhereKeyExpression(o,keys,template.getWhereBuilder());
	}
	
	public void buildJoinByForeginKey(RelationalField relTarget, String targetTable, SelectTemplate template) {
		ForwardKey[] keys = relTarget.getRelationship().joinFields();
		ExpressionBuilder exprBuilder = template.newExpressionBuilder();
		buildJoinKeyExpression(keys, exprBuilder);
		template.addJoin(new SqlTable(targetTable), JoinType.LEFT, exprBuilder.getExpression());
	}	
	
	public void buildWhereByForeginKey(T o,String name,SelectTemplate template){
		BeanSubTypeRelationship<Entity> relationship = new BeanSubTypeRelationship<Entity>(name);
		buildWhereByForeginKey(o,relationship, template);
	}

	public void buildWhereByForeginKey(T o,BeanSubTypeRelationship<Entity> relationship,SelectTemplate template) {
		buildWhereByForeginKey(o,relationship.targetRelField,template);
	}
	
	public void buildJoinByForeginKey(String name, SelectTemplate template) {
		BeanSubTypeRelationship<Entity> relationship = new BeanSubTypeRelationship<Entity>(name);
		buildJoinByForeginKey(relationship, template);
	}

	private void buildJoinByForeginKey(BeanSubTypeRelationship<Entity> relationship, SelectTemplate template) {
		buildJoinByForeginKey(relationship.getRelationshipField(),relationship.getRelationshipTableName(),template);
	}	
	
	protected void buildJoinKeyExpression(ForwardKey[] keys,ExpressionBuilder joinBuilder){
		Iterator<ForwardKey> ijoin = Arrays.asList(keys).iterator();
		while(ijoin.hasNext()){
			ForwardKey fk = ijoin.next();
			ColumnExpression fromExpr = new ColumnExpression(fk.tableField());
			ColumnExpression toExpr = new ColumnExpression(fk.foreginField());
			BinaryExpression expr = new BinaryExpression(fromExpr, BinaryOperator.EQ, toExpr);
			joinBuilder.append(expr);
			if(ijoin.hasNext()){
				joinBuilder.AND();
			}
		}
	}
	
	protected void buildWhereKeyExpression(T o,ForwardKey[] keys,ExpressionBuilder exprBuilder){
		BeanFormater f;
		type.format(o, f = new BeanFormater());
		ContentValues values =	f.initialValues.get(o.getClass()); //TODO Tratar herannça
		Iterator<ForwardKey> ijoin = Arrays.asList(keys).iterator();
		while(ijoin.hasNext()){
			ForwardKey fk = ijoin.next();
			ColumnExpression fromExpr = new ColumnExpression(fk.foreginField());
			Object value = values.get(fk.tableField());
			LiteralValueExpression toExpr = exprBuilder.getLiteral(value) ;
			BinaryExpression expr = new BinaryExpression(fromExpr, BinaryOperator.EQ, toExpr);
			exprBuilder.append(expr);
			if(ijoin.hasNext()){
				exprBuilder.AND();
			}
		}
	}
	

	@Override
	public List<T> listAll() {
		return list(0,0,null,null);
	}
	
	@Override
	public List<T> list(int offset, int limit,String orderBy,ORDERING_MODE mode) {
		return list(offset,limit,orderBy,mode,getAllFieldNames());
	}

	@Override
	public List<T> list(int offset, int limit, String orderBy,ORDERING_MODE mode, String... projection) {
		SelectTemplate template = getBaseTemplateSelection(offset,limit,orderBy,mode,projection);
		return getResultAsList(template,TYPE_PARSER);
	}

	@Override
	public List<Map<String, Object>> listUntypedAll() {
		return listUntyped(0, 0, null, null);
	}
	
	@Override
	public List<Map<String, Object>> listUntyped(int offset, int limit,	String orderBy,ORDERING_MODE mode) {
		return listUntyped(offset, limit, orderBy,mode, getAllFieldNames());
	}

	@Override
	public List<Map<String, Object>> listUntyped(int offset, int limit,	String orderBy,ORDERING_MODE mode, String... projection) {
		SelectTemplate template = getBaseTemplateSelection(offset,limit,orderBy,mode);
		return getResultAsList(template,UNTYPE_PARSER);
	}

	public void buildListDetails(int offset, int limit, String orderBy, ORDERING_MODE mode, SelectTemplate template) {
		if(limit != 0){
			template.setLimit(offset, limit);
		}
		if(orderBy != null && !orderBy.isEmpty()){
			SqlColumn orderByColumn = getFullSqlColumn(orderBy);
			template.setOrderBy(orderByColumn,mode);
		}
	}
	
	public <E> List<E> getResultAsList(SelectTemplate template,CursorParser<E> parser){
		CursorListResult<E> i = new CursorListResult<E>(parser);
		return getQueryResult(template, i);
	}
	
	public List<Object> getResultAsPrimitiveList(SelectTemplate template,CursorParser<PrimitiveEntity> parser){
		CursorPrimitiveListResult i = new CursorPrimitiveListResult(parser);
		return getQueryResult(template, i);
	}
	
	public <E> E getResult(SelectTemplate template,CursorParser<E> parser){
		CursorResult<E> i = new CursorResult<E>(parser);
		return getQueryResult(template, i);
	}
	
	public <E,R> R getQueryResult(SelectTemplate template,CursorIterate<E,R> i) {
		try{
			ResultSet mCursor = getRaw(template);	        
	        if (mCursor != null) {
	            iterateQueryResult(i, mCursor);
	        }
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return i.getResult();
	}

	public <E,R> void iterateQueryResult(CursorIterate<E,R> i, ResultSet mCursor)	throws SQLException {
		while(i.more(mCursor)){
			i.onIterate(mCursor);
		}
		mCursor.close();
	}
	
	@Override
	public QueryResultParser query(String sql, Object[] args) {		
		try {
			ResultSet mCursor = db.rawQuery(sql,args);
			return new BeanResultParser(mCursor);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<Map<String, Object>> queryUntype(String sql, Object[] args) {
		try {
			ResultSet mCursor = db.rawQuery(sql,args);
			iterateQueryResult(new CursorListResult<Map<String, Object>>(UNTYPE_PARSER), mCursor);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public long insert(T o) {
		BeanFormater f;
		type.formatEscapeAutoincrement(o, f = new BeanFormater());
		long generated = 0;
		try{
			beforeInsert(o);
			return generated = db.insert(getTable().name(), "id", f.initialValues.get(o.getClass())); //TODO Tratar herança
		}finally{
			afterInsert(o,generated);
		}		
	}

	@Override
	public int update(T o) {
		BeanFormater f;
		type.formatWithoutKey(o, f = new BeanFormater());
		UpdateTemplate template = TemplateFactory.getUpdateTemplate(getTable().name());
		RelationalSqlDatabase.applyContentValues(f.initialValues.get(o.getClass()), template); //TODO Tratar herança
		buildWhereByExample(o, true, template.getWhere());
		PreparedSql query = template.buildSql();
		int modified = 0;
		try {
			beforeUpdate(o);
			return modified = db.execSQL(query.getSql().toString(), query.getValues().toArray());
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			afterUpdate(o,modified);
		}
		return -1;
	}

	@Override
	public int deleteById(int rowId) {
		DeleteTemplate template = TemplateFactory.getDeleteTemplate(getTable().name());
		template.getWhereBuilder().appendEqual(getFullSupposedIdSqlColumn(), rowId);
		return delete(template);
	}
	
	@Override
	public int delete(T o) {
		DeleteTemplate template = TemplateFactory.getDeleteTemplate(getTable().name());
		buildWhereByExample(o, true, template.getWhereBuilder());
		return delete(template);
	}
	
	@Override
	public int deleteAll() {
		DeleteTemplate template = TemplateFactory.getDeleteTemplate(getTable().name());
		return delete(template);
	}
	
	private int delete(DeleteTemplate template){
		PreparedSql query = template.buildSql();
		int modified = 0;
		try {
			beforeDelete(null);
			return modified = db.execSQL(query.getSql().toString(), query.getValues().toArray());
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			afterDelete(null,modified);
		}
		return -1;		
	}
	
	@Override
	public void close() {
		getDatabase().close();
	}

	public <E extends DatabaseListener & EntityParseListener> void addRelationListener(E listener){
		addDatabaseListener(listener);
		addEntityParserListener(listener);
	}
	
	@Override
	public void addDatabaseListener(DatabaseListener listener) {
		databaselisteners.add(listener);
	}

	@Override
	public void removeDatabaseListener(DatabaseListener listener) {
		databaselisteners.remove(listener);
	}

	@Override
	public List<DatabaseListener> getAllDatabaseListeners() {
		return Collections.unmodifiableList(databaselisteners);
	}

	@Override
	public void addEntityParserListener(EntityParseListener listener) {
		entityParserlisteners.add(listener);
	}
	
	@Override
	public void removeEntityParserListener(EntityParseListener listener) {
		entityParserlisteners.remove(listener);
	}
	
	@Override
	public List<EntityParseListener> getAllEntityParserListeners() {
		return Collections.unmodifiableList(entityParserlisteners);
	}
	
	@Override
	public void beginTransaction() {
		getDatabase().beginTransaction();
	}

	@Override
	public void setTransactionSuccessful() {
		getDatabase().setTransactionSuccessful();
	}

	@Override
	public void endTransaction() {
		getDatabase().endTransaction();
	}

	@Override
	public EntityReader<T> getReader() {
		return this;
	}

	@Override
	public IDatabase getDatabase() {
		return db;
	}

	public SqlColumn getFullSupposedIdSqlColumn(){
		String columnName = "id"; //Isso é uma suposição
		RelationalField rel = type.getDescriptor().getRelationalByColumn(columnName);
		return new SqlColumn(getSqlTable(),rel.getColumn().name());
	}
	
	public static SqlColumn getFullSqlColumn(BeanFormat<?> type,String fieldPath){
		RelationalField rel = type.getDescriptor().getRelationalByPath(fieldPath);
		return new SqlColumn(getSqlTable(type),rel.getColumn().name());
	}	
	
	public SqlColumn getFullSqlColumn(String fieldPath){
		return getFullSqlColumn(this.type,fieldPath);
	}	

	public static SqlTable getSqlTable(BeanFormat<?> type) {
		return new SqlTable(getTable(type).name());
	}
	
	public SqlTable getSqlTable() {
		return new SqlTable(getTable().name());
	}
	
	public static String[] getAllFieldNames(BeanFormat<?> type) {			
		ObjectMapping<?> map = type.getDescriptor().getObjectMapping();
		return map.getFieldsName();
	}
	
	public String[] getAllFieldNames() {
		return getAllFieldNames(this.type);
	}
	
	public static Table getTable(BeanFormat<?> type){
		return type.getDescriptor().getRelationaMapping().getTable();
	}
	
	public Table getTable(){
		return getTable(this.type);
	}
	
	public T typeParse(EntityParser parser){
		T instance = type.newInstance();
		try{
			beforeParse(instance,parser);
			return type.parse(parser,instance);
		}finally{
			afterParse(instance,parser);
		}		
	}
	
	@Override
	public BeanFormat<T> getType() {
		return type;
	}
	
	public Map<String,Object> untypeParse(ResultSet mCursor) throws SQLException{
		Map<String,Object> untype = new HashMap<String, Object>();		
		try{
			beforeUntypedParse(untype,mCursor);
		    ResultSetMetaData rsMetaData = mCursor.getMetaData();
		    int numberOfColumns = rsMetaData.getColumnCount();
		    for (int i = 1; i < numberOfColumns + 1; i++) {
		      String name = rsMetaData.getColumnName(i);
		      Object value = RelationalSqlDatabase.getResultSetValue(mCursor, i);
		      untype.put(name, value);
		    }
			return untype;
		}finally{
			afterUntypedParse(untype,mCursor);
		}
	}

	protected void beforeParse(final T o, EntityParser parser){
		for(EntityParseListener listener : entityParserlisteners){
			listener.onBeforeParse(o,parser,DaoCrud.this);
		}
	}
	
	protected void afterParse(final T o, EntityParser parser){
		for(EntityParseListener listener : entityParserlisteners){
			listener.onAfterParse(o, parser, DaoCrud.this);
		}
	}
	
	protected void beforeUntypedParse(final Map<String,Object> untype, ResultSet mCursor){
		for(EntityParseListener listener : entityParserlisteners){
			listener.onBeforeUntypedParse(untype,mCursor,DaoCrud.this);
		}
	}
	
	protected void afterUntypedParse(final Map<String,Object> untype, ResultSet mCursor){
		for(EntityParseListener listener : entityParserlisteners){
			listener.onAfterUntypedParse(untype,mCursor,DaoCrud.this);
		}
	}
	
	protected void beforeInsert(final T o){
		for(DatabaseListener listener : databaselisteners){
			listener.onBeforeInsert(o, DaoCrud.this);
		}
	}
	
	protected void beforeUpdate(final T o){
		for(DatabaseListener listener : databaselisteners){
			listener.onBeforeUpdate(o, DaoCrud.this);
		}
	}
	
	protected void beforeDelete(final T o){
		for(DatabaseListener listener : databaselisteners){
			listener.onBeforeDelete(o, DaoCrud.this);
		}
	}
	
	protected void afterInsert(final T o,long generated){
		for(DatabaseListener listener : databaselisteners){
			listener.onAfterInsert(o,generated, DaoCrud.this);
		}
	}
	
	protected void afterUpdate(final T o,int modified){
		for(DatabaseListener listener : databaselisteners){
			listener.onAfterUpdate(o, modified, DaoCrud.this);
		}
	}
	
	protected void afterDelete(final T o,int modified){
		for(DatabaseListener listener : databaselisteners){
			listener.onAfterDelete(o, modified, DaoCrud.this);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	private interface CursorIterate<E,R>{
		boolean more(ResultSet mCursor) throws SQLException;
		E onIterate(ResultSet mCursor) throws SQLException;
		R getResult();
	}
	
	private interface CursorParser<E>{
		E getElement(ResultSet mCursor) throws SQLException;
	}
	
	private class BeanSubTypeRelationship<E extends Entity> implements RelationshipModel<E>{		
		@SuppressWarnings("unused")
		private String 			name;
		private BeanFormat<E> 	targetType;
		private RelationalField targetRelField;
		
		public BeanSubTypeRelationship(String name) {
			this.name = name;
			this.targetType = resolveSubTypeBeanFormat(name);
			this.targetRelField = type.getDescriptor().getRelationalByPath(name);
		}		
		@Override
		public Table getRelationshipTable(){
			return targetType.getDescriptor().getRelationaMapping().getTable();
		}
		@Override
		public String getRelationshipTableName(){
			return getRelationshipTable().name();
		}
		@Override
		public BeanFormat<E> getRelationshipType() {
			return targetType;
		}
		@Override
		public RelationalField getRelationshipField() {
			return targetRelField;
		}
	}
	
	private class TypeCursorParser implements CursorParser<T>{
		@Override
		public T getElement(ResultSet mCursor) throws SQLException {
			BeanParser parser = new BeanParser(mCursor);
			return typeParse(parser);
		}
	}
	
	private class SubTypeCursorParser<E extends Entity> implements CursorParser<E>{
		private BeanFormat<E> subType;
		public SubTypeCursorParser(BeanFormat<E> subType) {
			this.subType = subType;
		}
		@Override
		public E getElement(ResultSet mCursor) throws SQLException {
			BeanParser parser = new BeanParser(mCursor);
			E instance = subType.newInstance();
			return subType.parse(parser,instance);
		}		
	}
	
	private class UnTypeCursorParser implements CursorParser<Map<String,Object>>{
		@Override
		public Map<String, Object> getElement(ResultSet mCursor) throws SQLException {
			return untypeParse(mCursor);
		}
	}
	
	private class ExistsParser implements CursorParser<Boolean>{
		@Override
		public Boolean getElement(ResultSet mCursor) throws SQLException {
			return mCursor.next();
		}		
	}
	
	private class CursorResult<E> implements CursorIterate<E,E>{
		private CursorParser<E>		parser;
		private Boolean 			more;
		private E 					e;		
		public CursorResult(CursorParser<E> parser) {
			this.parser = parser;
		}
		@Override
		public boolean more(ResultSet mCursor) throws SQLException {
			return more == null ? more = mCursor.next() : more;
		}
		@Override
		public E onIterate(ResultSet mCursor) throws SQLException{
			e = parser.getElement(mCursor);
			more = false;
			return e;
		}
		@Override
		public E getResult() {
			return e;
		}
	}
	
	private class CursorListResult<E> implements CursorIterate<E,List<E>>{
		private List<E> 			list;
		private CursorParser<E>		parser;		
		public CursorListResult(CursorParser<E> parser) {
			this.parser = parser;
			this.list = new ArrayList<E>();
		}
		@Override
		public boolean more(ResultSet mCursor) throws SQLException {
			return mCursor.next();
		}
		@Override
		public E onIterate(ResultSet mCursor) throws SQLException{
			E e = parser.getElement(mCursor);
			list.add(e);
			return e;
		}
		@Override
		public List<E> getResult() {
			return list;
		}
	}
	
	private class CursorPrimitiveListResult implements CursorIterate<PrimitiveEntity,List<Object>>{
		private List<Object> 					list;
		private CursorParser<PrimitiveEntity>	parser;		
		public CursorPrimitiveListResult(CursorParser<PrimitiveEntity> parser) {
			this.parser = parser;
			this.list = new ArrayList<Object>();
		}
		@Override
		public boolean more(ResultSet mCursor) throws SQLException {
			return mCursor.next();
		}
		@Override
		public PrimitiveEntity onIterate(ResultSet mCursor) throws SQLException{
			PrimitiveEntity e = parser.getElement(mCursor);
			list.add(e.getRawValue());
			return e;
		}
		@Override
		public List<Object> getResult() {
			return list;
		}
	}
	
	class BeanFormater implements EntityFormater{
		Map<Class<?>,ContentValues> initialValues = new HashMap<Class<?>, ContentValues>();
		@Override
		public void setInt(Class<?> stype,String name, int value) {
			get(stype).put(name, value);
		}
		@Override
		public void setLong(Class<?> stype,String name, long value) {
			get(stype).put(name, value);
		}
		@Override
		public void setDouble(Class<?> stype,String name, double value) {
			get(stype).put(name, value);
		}
		@Override
		public void setString(Class<?> stype,String name, String value) {
			get(stype).put(name, value);
		}
		@Override
		public void setBoolean(Class<?> stype,String name, boolean value) {
			get(stype).put(name, value);
		}
		@Override
		public void setDate(Class<?> stype,String name, Date date) {
			if(date != null){
				get(stype).put(name, date);
			}			
		}
		private ContentValues get(Class<?> stype){
			ContentValues values;
			if((values = initialValues.get(stype)) == null)
				initialValues.put(stype, values = new ContentValues());
			return values;
		}
	}
	
	class BeanParser implements EntityParser{
		ResultSet mCursor;
		public BeanParser(ResultSet mCursor) {
			this.mCursor = mCursor;
		}
		@Override
		public int getInt(String name) {
			if(find(name) >= 0)
			try {
				return mCursor.getInt(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}
		@Override
		public long getLong(String name) {
			if(find(name) >= 0)
			try {
				return mCursor.getLong(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}
		@Override
		public double getDouble(String name) {
			if(find(name) >= 0)
			try {
				return mCursor.getDouble(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}
		@Override
		public String getString(String name) {
			if(find(name) >= 0)
			try {
				return mCursor.getString(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		public boolean getBoolean(String name) {
			if(find(name) >= 0)
			try {
				return mCursor.getShort(name) != 0;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;			
		}
		@Override
		public Date getDate(String name) {
			if(find(name) >= 0)
			try {
				return mCursor.getDate(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
		private int find(String columnLabel){
			try {
				return mCursor.findColumn(columnLabel);
			} catch (SQLException ignore) {
			}
			return -1;
		}
	}

	public class BeanResultParser implements QueryResultParser{
		private BeanParser 	parser;
		public BeanResultParser(ResultSet mCursor) {
        	parser = new BeanParser(mCursor);
		}		
		@Override
		public EntityParser next() {
			try {
				return parser.mCursor.next() ? parser : null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void finalize() throws Throwable {
			try{parser.mCursor.close();}catch (Exception e) {}
			super.finalize();
		}
	}
	
	public abstract class EntityRelation implements DatabaseListener,EntityParseListener{
		@Override
		public void onBeforeInsert(Entity o, Object source) {
		}
		@Override
		public void onAfterInsert(Entity o, long generated, Object source) {
		}
		@Override
		public void onBeforeUpdate(Entity o, Object source) {
		}
		@Override
		public void onAfterUpdate(Entity o, int modified, Object source) {
		}
		@Override
		public void onBeforeDelete(Entity o, Object source) {
		}
		@Override
		public void onAfterDelete(Entity o, int modified, Object source) {
		}
		@Override
		public void onBeforeParse(Entity o, EntityParser parser, Object source) {
		}
		@Override
		public void onAfterParse(Entity o, EntityParser parser, Object source) {			
		}
		@Override
		public void onBeforeUntypedParse(Map<String, Object> o, ResultSet mCursor, Object source) {			
		}
		@Override
		public void onAfterUntypedParse(Map<String, Object> o, ResultSet mCursor, Object source) {			
		}		
	}
	
}