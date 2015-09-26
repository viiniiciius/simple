package br.com.vitulus.simple.jdbc.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.PrimitiveEntity;
import br.com.vitulus.simple.jdbc.annotation.Column;
import br.com.vitulus.simple.jdbc.annotation.Enumerator;
import br.com.vitulus.simple.jdbc.annotation.ForwardKey;
import br.com.vitulus.simple.jdbc.annotation.Id;
import br.com.vitulus.simple.jdbc.annotation.Inheritance;
import br.com.vitulus.simple.jdbc.annotation.PrimitiveTable;
import br.com.vitulus.simple.jdbc.annotation.Relationship;
import br.com.vitulus.simple.jdbc.annotation.Table;
import br.com.vitulus.simple.jdbc.annotation.Transient;
import br.com.vitulus.simple.jdbc.orm.BeanFormat.OrmDescriptor;
import br.com.vitulus.simple.jdbc.orm.ObjectMapping.ObjectField;
import br.com.vitulus.simple.jdbc.orm.RelationalMapping.RelationalField;

public class OrmResolver {
	
	private static Map<Class<?>, EntityBeanFormat>						beanFormatIndex; 
	private static Map<Class<?>, OrmDescriptor>							descriptorsIndex;
	private static TwoWayHashmap<Class<? extends Entity>,Table> 		classTableIndex;
	private static TwoWayHashmap<Class<? extends Entity>,Inheritance> 	classInheritanceIndex;
	
	static{
		beanFormatIndex = new HashMap<Class<?>, EntityBeanFormat>();
		descriptorsIndex = new HashMap<Class<?>, EntityBeanFormat.OrmDescriptor>();
		classTableIndex = new TwoWayHashmap<Class<? extends Entity>, Table>();
		classInheritanceIndex = new TwoWayHashmap<Class<? extends Entity>, Inheritance>();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> BeanFormat<T> getRelationshipBeanFormat(Class<T> classe,final String fieldPath) throws Exception{
		final OrmDescriptor pDescriptor = getOrmDescriptor(classe);
		RelationalField relationField =  pDescriptor.getRelationalByPath(fieldPath);
		ObjectField objectField =  pDescriptor.getObjectByPath(fieldPath);
		
		PrimitiveTable ptable = relationField.getPrimitiveTable();
		Table table = ptable.table();		
		if (table.name() == null || table.name().isEmpty()) {
			/*Se não descreve a tabela do relacionamento, faz uma suposição*/
			table = new Table() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return Table.class;
				}
				@Override
				public String name() {
					return fieldPath;
				}
			};
		}		
		ForwardKey[] fks = relationField.getRelationship().joinFields();
		if (fks == null || fks.length == 0) {
			/*Se não tem nenhuma informação sobre os campos para junção, faz uma suposição */
			fks = new ForwardKey[] { new ForwardKey() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return ForwardKey.class;
				}
				@Override
				public String tableField() {
					return "id";
				}
				@Override
				public String foreginField() {
					return "fk_"+pDescriptor.getRelationaMapping().getTable().name();
				}
			} };
		}
		
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass(classe.getName() + "." + fieldPath + "." + objectField.getType().getSimpleName());
		CtClass centity = pool.getOrNull(PrimitiveEntity.class.getName());		
		cc.addInterface(centity);
		
		Class<?> argumentType = objectField.getGenericTypes().get(0);
		CtClass valueType = pool.getOrNull(argumentType.getName());			
		CtField valueField = new CtField(valueType, "value", cc);
		cc.addField(valueField);
		cc.addMethod(CtNewMethod.getter("getValue", valueField));
		cc.addMethod(CtNewMethod.setter("setValue", valueField));
		cc.addMethod(CtNewMethod.make("public Object getRawValue(){return getValue();}", cc));
		ObjectField valueObjectfield = new ObjectField("value", argumentType);
		RelationalField valueRelationalfield = new RelationalField(ptable.value());	
			
		FieldColumnAssociation a = new FieldColumnAssociation();		
		a.add(valueObjectfield, valueRelationalfield);		
		int fkCount = 0;
		for(ForwardKey fk : fks){			
			ObjectField localField =  pDescriptor.getObjectByColumn(fk.tableField());			
			CtClass dynFkType = pool.getOrNull(localField.getType().getName());
			String dynFieldName = "Field" + fkCount++;			
			CtField dynFkField = new CtField(dynFkType, "fk"+dynFieldName, cc);
			cc.addField(dynFkField);
			cc.addMethod(CtNewMethod.getter("getFk"+dynFieldName, dynFkField));
			cc.addMethod(CtNewMethod.setter("setFk"+dynFieldName, dynFkField));
			ObjectField fkObjectfield = new ObjectField("fk"+dynFieldName, localField.getType());
			RelationalField fkRelationalfield = new RelationalField(fk.foreginField());
			a.add(fkObjectfield, fkRelationalfield);
		}		
		Class<T> relationalClasse = cc.toClass();
		classTableIndex.add(relationalClasse, table);
		RelationalMapping relationaMapping = new RelationalMapping(
				table,
				a.getRelFields(),
				a.getRelRelantionshipFields());
		ObjectMapping<T> objectMapping = new ObjectMapping<T>(
				relationalClasse,
				a.getoFields(),
				a.getRelantionshipFields());
		OrmDescriptor descriptor = new OrmBean<T>(objectMapping,relationaMapping);
		return (BeanFormat<T>) new EntityBeanFormat(descriptor);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> BeanFormat<T> getBeanFormat(Class<T> classe){
		EntityBeanFormat beanFormat = beanFormatIndex.get(classe);
		if(beanFormat == null){
			beanFormat = new EntityBeanFormat(getOrmDescriptor(classe));
			beanFormatIndex.put(classe, beanFormat);
		}
		return (BeanFormat<T>)beanFormat;
	}
	
	public static Table getTable(Class<? extends Entity> classe){
		return classTableIndex.getForward(classe);
	}
	
	public static Class<? extends Entity> getClasse(Table table){
		return classTableIndex.getBackward(table);
	}
	
	public static Inheritance getInheritance(Class<? extends Entity> classe){
		return classInheritanceIndex.getForward(classe);
	}
	
	public static Class<? extends Entity> getClasse(Inheritance inheritance){
		return classInheritanceIndex.getBackward(inheritance);
	}
	
	public static <T extends Entity>OrmDescriptor getOrmDescriptor(Class<T> classe){
		OrmDescriptor descriptor;
		if((descriptor = descriptorsIndex.get(classe)) == null){
			descriptorsIndex.put(classe, descriptor = buildDescriptor(classe));
		}				
		return descriptor;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Entity> OrmDescriptor buildDescriptor(Class<T> classe) {
		Table table = classe.getAnnotation(Table.class);
		if(table == null){
			throw new IllegalStateException("A classe : " + classe + " precisa anotar @Table.");
		}
		classTableIndex.add(classe, table);
		FieldColumnAssociation a = getFieldColumnAssociation(classe.getDeclaredFields());
		RelationalMapping relationaMapping = new RelationalMapping(
				table,
				a.getRelFields(),
				a.getRelRelantionshipFields());
		ObjectMapping<T> objectMapping = new ObjectMapping<T>(
				classe,
				a.getoFields(),
				a.getRelantionshipFields());
		Class<? super T> superClasse = classe.getSuperclass();
		if(Entity.class.isAssignableFrom(superClasse)){
			Inheritance inheritance = classe.getAnnotation(Inheritance.class);
			classInheritanceIndex.add(classe, inheritance);
			objectMapping.setInheritance((Class<T>)superClasse);
		}
		OrmDescriptor descriptor = new OrmBean<T>(objectMapping,relationaMapping);
		return descriptor;
	}			
	
	private static FieldColumnAssociation getFieldColumnAssociation(Field[] fields){
		FieldColumnAssociation association = new FieldColumnAssociation();
		for(Field field : fields){
			if(!isIgnoreField(field)){
				Column 			column = field.getAnnotation(Column.class);
				Id				id = field.getAnnotation(Id.class);
				Enumerator 		enumerator = field.getAnnotation(Enumerator.class);				
				Relationship	relationship = field.getAnnotation(Relationship.class);
				PrimitiveTable  primitiveTable = field.getAnnotation(PrimitiveTable.class);
				Type[]          generics = {};				
				Type genericType = field.getGenericType();
				if(genericType instanceof ParameterizedType){
					ParameterizedType ptype  = (ParameterizedType) genericType;
					generics = ptype.getActualTypeArguments();
				}				
				ObjectField ofield = new ObjectField(field.getName(), field.getType(), generics);				
				if(relationship != null){
					if(primitiveTable != null){
						association.addRelationShip(
								ofield,
								new RelationalField(field.getName(),relationship,primitiveTable));
					}else{
					association.addRelationShip(
							ofield,
							new RelationalField(field.getName(),relationship));
					}
				}else{
					if(column == null){
						association.add(
								ofield,
								new RelationalField(field.getName(),enumerator,id));
					}else{
						association.add(
								ofield,
								new RelationalField(column,enumerator,id));						
					}
				}				
			}
		}
		return association;
	}
	
	private static boolean isIgnoreField(Field field){
		return field.getAnnotation(Transient.class) != null ||
				Modifier.isStatic(field.getModifiers());
	}
	
	public static class OrmBean<T extends Entity> implements OrmDescriptor{
		private Map<String,ObjectField> 		objectIndex;
		private Map<String,RelationalField> 	relationalIndex;
		private ObjectMapping<T>				objectMapping;
		private RelationalMapping				relationalMapping;		
		public OrmBean(ObjectMapping<T> objectMapping,	RelationalMapping relationalMapping) {
			this.objectMapping = objectMapping;
			this.relationalMapping = relationalMapping;
			this.objectIndex = new HashMap<String, ObjectField>();
			this.relationalIndex = new HashMap<String, RelationalField>();
			indexFields();
		}
		private void indexFields(){
			for(int i = 0;i < objectMapping.getFields().size(); i++ ){
				ObjectField objectField = objectMapping.getFields().get(i);
				RelationalMapping.RelationalField relationalField = relationalMapping.getFields().get(i);
				objectIndex.put(objectField.getPath(), objectField);
				relationalIndex.put(relationalField.getColumn().name(), relationalField);
			}
			/*Seria interessante construir um indice separado para os relacionamentos ?*/
			for(int i = 0;i < objectMapping.getRelationships().size(); i++ ){
				ObjectField objectField = objectMapping.getRelationships().get(i);
				RelationalMapping.RelationalField relationalField = relationalMapping.getRelationships().get(i);
				objectIndex.put(objectField.getPath(), objectField);
				relationalIndex.put(relationalField.getColumn().name(), relationalField);
			}
		}
		@Override
		public ObjectMapping<? extends Entity> getObjectMapping() {
			return objectMapping;
		}
		@Override
		public RelationalMapping getRelationaMapping() {
			return relationalMapping;
		}
		@Override
		public ObjectField getObjectByPath(String fieldPath) {
			return objectIndex.get(fieldPath);
		}
		@Override
		public RelationalField getRelationalByColumn(String columnName) {
			return relationalIndex.get(columnName);
		}		
		@Override
		public RelationalField getRelationalByPath(String fieldPath) {
			ObjectField objectField = getObjectByPath(fieldPath);
			int index = objectMapping.getFields().indexOf(objectField);
			if(index < 0){
				index = objectMapping.getRelationships().indexOf(objectField);
				if(index < 0){
					throw new IllegalArgumentException("O campo '" + fieldPath + "' não existe.");
				}
				return relationalMapping.getRelationships().get(index);
			}else{
				return relationalMapping.getFields().get(index);
			}			
		}
		@Override
		public ObjectField getObjectByColumn(String columnName) {
			RelationalField relationalField = getRelationalByColumn(columnName);
			int index = relationalMapping.getFields().indexOf(relationalField);
			if(index < 0){
				index = relationalMapping.getRelationships().indexOf(relationalField);
				if(index < 0){
					throw new IllegalArgumentException("A coluna '" + columnName + "' não existe.");
				}
				return objectMapping.getRelationships().get(index);
			}else{
				return objectMapping.getFields().get(index);
			}			
		}
	}
	
	private static class TwoWayHashmap<K,V> {
	  private Map<K,V> forward = new HashMap<K, V>();
	  private Map<V,K> backward = new HashMap<V, K>();
	  private void add(K key, V value) {
	    forward.put(key, value);
	    backward.put(value, key);
	  }
	  private V getForward(K key) {
	    return forward.get(key);
	  }
	  private K getBackward(V key) {
	    return backward.get(key);
	  }
	}
	
	private static class FieldColumnAssociation{
		List<RelationalField> 	relFields;
		List<RelationalField>	relRelantionshipFields;
		List<ObjectField>		oFields;
		List<ObjectField>		oRelantionshipFields;		
		public FieldColumnAssociation() {
			this.relFields = new LinkedList<RelationalField>();
			this.relRelantionshipFields = new LinkedList<RelationalField>();
			this.oFields = new LinkedList<ObjectField>();
			this.oRelantionshipFields = new LinkedList<ObjectField>();
		}		
		public void add(ObjectField o,RelationalField r){
			relFields.add(r);
			oFields.add(o);
		}		
		public void addRelationShip(ObjectField o,RelationalField r){
			relRelantionshipFields.add(r);
			oRelantionshipFields.add(o);
		}		
		public ObjectField[] getoFields() {
			return oFields.toArray(new ObjectField[oFields.size()]);
		}
		public ObjectField[] getRelantionshipFields() {
			return oRelantionshipFields.toArray(new ObjectField[oRelantionshipFields.size()]);
		}
		public RelationalField[] getRelFields() {
			return relFields.toArray(new RelationalField[relFields.size()]);
		}
		public RelationalField[] getRelRelantionshipFields() {
			return relRelantionshipFields.toArray(new RelationalField[relRelantionshipFields.size()]);
		}
	}
	
}