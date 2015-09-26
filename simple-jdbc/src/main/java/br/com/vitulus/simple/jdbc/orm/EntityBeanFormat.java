package br.com.vitulus.simple.jdbc.orm;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtilsBean;

import br.com.vitulus.simple.jdbc.BeanProperty;
import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.annotation.type.EnumValueType;
import br.com.vitulus.simple.jdbc.annotation.type.IdIncrementType;
import br.com.vitulus.simple.jdbc.orm.ObjectMapping.ObjectField;
import br.com.vitulus.simple.jdbc.orm.RelationalMapping.RelationalField;

public class EntityBeanFormat implements BeanFormat<Entity> {

	private static final PropertyUtilsBean reflx = BeanProperty.instance();
	
	private OrmDescriptor descriptor;
	
	EntityBeanFormat(OrmDescriptor descriptor){
		this.descriptor = descriptor;
	}
	
	@Override
	public void format(Entity o, EntityFormater f) {
		iterateFields(new FormatFieldIterator(o,f));
	}

	@Override
	public void formatKey(Entity o, EntityFormater f) {
		iterateFields(new FormatFieldWithKeyIterator(o,f));
	}
	
	@Override
	public void formatWithoutKey(Entity o,EntityFormater f) {
		iterateFields(new FormatFieldWithoutKeyIterator(o,f));
	}
	
	@Override
	public void formatEscapeAutoincrement(Entity o,EntityFormater f) {
		iterateFields(new FormatFieldEscapeDefaultValuesAutoIncrementIterator(o,f));
	}
	
	@Override
	public void formatEscapeDefaults(Entity o, EntityFormater f) {				
		iterateFields(new FormatFieldIterator(o,new DefaultValueAvoidFormaterWrapper(f)));		
	}
	
	@Override
	public Entity parse(EntityParser p) {		
		return parse(p,newInstance());
	}
	
	@Override
	public Entity parse(EntityParser p, Entity instance) {
		iterateFields(new ParseFieldIterator(instance,p));
		return instance;
	}
	
	private void iterateFields(FieldProcessIterator i){
		OrmDescriptor descriptor = getDescriptor();
		Class<? extends Entity> stype = null;
		do{
			ObjectMapping<? extends Entity> om = descriptor.getObjectMapping();
			List<ObjectField> fields = descriptor.getObjectMapping().getFields();
			iterateFields(i,fields,om.getClasse());
			stype = om.getInheritance();
			if(stype != null){
				descriptor = OrmResolver.getOrmDescriptor(stype);
			}			
		}while(stype != null);
	}

	private void iterateFields(FieldProcessIterator i,List<ObjectField> fields, Class<? extends Entity> declaring) {
		for(final ObjectField field : fields){			
			FieldAspect aspect = new FieldAspect(){
				@Override
				public boolean isKey() {
					RelationalField rel = getDescriptor().getRelationalByPath(field.getPath());
					return rel.hasKey();
				}
				@Override
				public boolean isAutoIncrement() {
					RelationalField rel = getDescriptor().getRelationalByPath(field.getPath());
					return rel.getId() != null && rel.getId().autoIncrement() == IdIncrementType.IDENTITY;
				}
			};
			if(!i.skip(aspect)){
				i.onIterate(field,declaring,aspect);
			}
		}
	}
	
	@Override
	public Entity newInstance() {
		try {			
			return getClasse().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Object getParsedValue(EntityParser p, ObjectField field) {
		String columnName = getColumnName(field);
		Class<?> type = field.getType();		
		return getTypedParsedValue(p, columnName, type);
	}

	private Object getTypedParsedValue(EntityParser p,String columnName, Class<?> type) {
		Object value = null;
		if(type.isAssignableFrom(String.class)){
			value = p.getString(columnName);
		}else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)){
			value = p.getInt(columnName);
		}else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)){
			value = p.getLong(columnName);
		}else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)){
			value = p.getDouble(columnName);
		}else if(type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)){
			value = p.getBoolean(columnName);
		}else if(type.isAssignableFrom(Date.class)){
			value = p.getDate(columnName);
		}else if(Enum.class.isAssignableFrom(type)){
			RelationalField rel = getDescriptor().getRelationalByColumn(columnName);
			EnumValueType valueType = rel.getEnumerator().type();
			value = getTypedParsedValue(p,columnName,valueType.valueClass);
		}
		return value;
	}	
	
	private void setFormatedValue(EntityFormater f, ObjectField field, Object value,Class<? extends Entity> declaring) {
		String columnName = getColumnName(field);
		Class<?> type = field.getType();
		setTypedFormatedValue(f, value, columnName, type,declaring);
	}

	private void setTypedFormatedValue(EntityFormater f, Object value, String columnName, Class<?> type, Class<? extends Entity> declaring) {
		if(type.isAssignableFrom(String.class)){			
			f.setString(declaring,columnName, (String) value);
		}else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)){
			int ivalue = value == null ? 0 : (Integer) value; 
			f.setInt(declaring,columnName, ivalue);
		}else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)){
			long lvalue = value == null ? 0 : (Long) value; 
			f.setLong(declaring,columnName, lvalue);
		}else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)){
			double dvalue = value == null ? 0 : (Double) value; 
			f.setDouble(declaring,columnName, dvalue);
		}else if(type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)){
			boolean bvalue = value == null ? false : (Boolean) value; 
			f.setBoolean(declaring,columnName, bvalue);
		}else if(type.isAssignableFrom(Date.class)){
			f.setDate(declaring,columnName, (Date)value);
		}else if(Enum.class.isAssignableFrom(type)){
			if(value != null){
				setTypedFormatedValue(f,value,columnName,value.getClass(),declaring);
			}			
		}else{
			throw new IllegalArgumentException("Column : '" + columnName +"'; Of java type : " + type + "; Declared at : " + declaring + "; With value : " + value + ". Is not a supported Type ");
		}
	}
	
	private Object getBeanValue(Entity o,ObjectField field) {
		try {
			String fieldPath = field.getPath();
			Object value = reflx.getNestedProperty(o, fieldPath);
			return formatSpecialTypeCases(field, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object formatSpecialTypeCases(ObjectField field, Object value) {
		String fieldPath = field.getPath();
		if (field.getType().isEnum()) {				
			RelationalField rel = getDescriptor().getRelationalByPath(fieldPath);
			if(rel.hasEnum()){
				value = formatEnumValue(value, rel.getEnumerator().type());
			}				
		}
		return value;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object parseSpecialTypeCases(ObjectField field, Object value){
		String fieldPath = field.getPath();
		if (field.getType().isEnum()) {				
			RelationalField rel = getDescriptor().getRelationalByPath(fieldPath);
			if(rel.hasEnum()){
				Class type = field.getType();
				value = parseEnumValue(value,type ,rel.getEnumerator().type());
			}				
		}
		return value;
	}
	
	private void setBeanValue(Entity o,ObjectField field,Object value){
		try {
			reflx.setNestedProperty(o, field.getPath(), value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	private <E extends Enum<E>> Enum<E> parseEnumValue(Object value, Class<E> type, EnumValueType valueType){		
		if (valueType == EnumValueType.ORDINAL) {
			E[] values = type.getEnumConstants();
			Number index = (Number) value;
			if (index.intValue() >= 0 || index.intValue() < values.length) {
				return values[index.intValue()];
			}
		} else if (valueType == EnumValueType.NAME) {
			return Enum.valueOf(type, String.valueOf(value));
		}
		return null;
	}
	
	private Object formatEnumValue(Object value, EnumValueType type) {
		if (value instanceof Enum<?>) {
			Enum<?> enumBean = (Enum<?>) value;
			if (type == EnumValueType.ORDINAL) {
				return enumBean.ordinal();
			} else if (type == EnumValueType.NAME) {
				return enumBean.name();
			} else {
				return null;
			}
		}
		return null;
	}
	
	private Class<? extends Entity> getClasse(){
		return getDescriptor().getObjectMapping().getClasse();
	}	
	
	private String getColumnName(ObjectField field) {
		RelationalField relField = getDescriptor().getRelationalByPath(field.getPath());
		return relField.getColumn().name();
	}

	public OrmDescriptor getDescriptor() {
		return descriptor;
	}
	
	private class FormatFieldEscapeDefaultValuesAutoIncrementIterator extends FormatFieldIterator{
		EntityFormater wrapper;
		public FormatFieldEscapeDefaultValuesAutoIncrementIterator(Entity o, EntityFormater f) {
			super(o, f);
			wrapper = new DefaultValueAvoidFormaterWrapper(f);
		}
		@Override
		public void onIterate(ObjectField field,Class<? extends Entity> declaring,FieldAspect aspect) {
			Object value = getBeanValue(o, field);
			setFormatedValue(aspect.isAutoIncrement() ? wrapper : f, field, value, declaring);		
		}		
	}

	private class DefaultValueAvoidFormaterWrapper implements EntityFormater{
		EntityFormater f;
		public DefaultValueAvoidFormaterWrapper(EntityFormater f) {
			this.f = f;
		}		
		@Override
		public void setInt(Class<?> stype,String name, int value) {
			if(value > 0){
				f.setInt(stype,name, value);
			}			
		}
		@Override
		public void setLong(Class<?> stype,String name, long value) {
			if(value > 0){
				f.setLong(stype,name, value);
			}
		}
		@Override
		public void setDouble(Class<?> stype,String name, double value) {
			if(value > 0){
				f.setDouble(stype,name, value);
			}
		}
		@Override
		public void setString(Class<?> stype,String name, String value) {
			f.setString(stype,name, value);			
		}
		@Override
		public void setBoolean(Class<?> stype,String name, boolean value) {
			f.setBoolean(stype,name, value);
		}
		@Override
		public void setDate(Class<?> stype,String name, Date date) {
			f.setDate(stype,name, date);
		}		
	}
	
	private class FormatFieldWithoutKeyIterator extends FormatFieldIterator{
		public FormatFieldWithoutKeyIterator(Entity o, EntityFormater f) {
			super(o, f);
		}
		@Override
		public boolean skip(FieldAspect aspect) {
			return aspect.isKey();
		}
	}
	
	private class FormatFieldWithKeyIterator extends FormatFieldIterator{
		public FormatFieldWithKeyIterator(Entity o, EntityFormater f) {
			super(o, f);
		}
		@Override
		public boolean skip(FieldAspect aspect) {
			return !aspect.isKey();
		}
	}
	
	private class FormatFieldIterator implements FieldProcessIterator{
		Entity 			o;
		EntityFormater 	f;		
		public FormatFieldIterator(Entity o, EntityFormater f) {
			this.o = o;
			this.f = f;
		}
		@Override
		public boolean skip(FieldAspect aspect) {
			return false;
		}
		@Override
		public void onIterate(ObjectField field,Class<? extends Entity> declaring,FieldAspect aspect) {
			Object value = getBeanValue(o, field);
			setFormatedValue(f, field, value, declaring);		
		}		
	}

	private class ParseFieldIterator implements FieldProcessIterator{
		Entity 		  o;
		EntityParser  p;
		public ParseFieldIterator(Entity o, EntityParser p) {
			this.o = o;
			this.p = p;
		}
		@Override
		public boolean skip(FieldAspect aspect) {
			return false;
		}
		@Override
		public void onIterate(ObjectField field,Class<? extends Entity> declaring,FieldAspect aspect) {
			Object value = getParsedValue(p, field);
			if(value != null){
				value = parseSpecialTypeCases(field,value);
			}			
			setBeanValue(o,field,value);
		}		
	}
	
	private interface FieldProcessIterator{
		boolean skip(FieldAspect aspect);
		void 	onIterate(ObjectField field, Class<? extends Entity> declaring, FieldAspect aspect);
	}
	
	private interface FieldAspect{
		boolean isKey();
		boolean isAutoIncrement();
	}
	
}