package br.com.vitulus.simple.jdbc.orm.old;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.com.vitulus.simple.jdbc.BeanProperty;
import br.com.vitulus.simple.jdbc.annotation.Enumerator;
import br.com.vitulus.simple.jdbc.annotation.ForwardKey;
import br.com.vitulus.simple.jdbc.annotation.Id;
import br.com.vitulus.simple.jdbc.annotation.Inheritance;
import br.com.vitulus.simple.jdbc.annotation.Relationship;
import br.com.vitulus.simple.jdbc.annotation.Table;
import br.com.vitulus.simple.jdbc.annotation.Transient;
import br.com.vitulus.simple.jdbc.annotation.type.EnumValueType;
import br.com.vitulus.simple.jdbc.converter.StringConverterFactory;

@Deprecated
public class OrmResolver {

	private Object                                                  target;
	private Map<Class<?>, Class<?>>                                 inheritanceMap;
	private static Map<Class<?>, List<Field>>                       fieldsCache;
	private static Map<Object, List<Class<? extends Annotation>>>   annotationCache;

	static {
		fieldsCache = new HashMap<Class<?>, List<Field>>();
		annotationCache = new HashMap<Object, List<Class<? extends Annotation>>>();
	}

	public static List<Class<? extends Annotation>> getAnnotations(Object o) {
		List<Class<? extends Annotation>> cache;
		if ((cache = annotationCache.get(o)) == null) {
			if (o instanceof Field) {
				Field field = (Field) o;
				cache = buildCache(field.getDeclaredAnnotations());
			} else if (o instanceof Class<?>) {
				Class<?> clazz = (Class<?>) o;
				cache = buildCache(clazz.getDeclaredAnnotations());
			}
			annotationCache.put(o, cache);
		}
		return cache;
	}
	
	public static List<Field> getFieldsCache(Class<?> key){
		return fieldsCache.get(key);
	}
	
	public static boolean hasAnnotation(Object o,Class<? extends Annotation> annotation) {
		List<Class<? extends Annotation>> cache = getAnnotations(o);
		return cache.contains(annotation);
	}

	private static List<Class<? extends Annotation>> buildCache(Annotation[] annotations) {
		List<Class<? extends Annotation>> cahce = new ArrayList<Class<? extends Annotation>>();
		for (Annotation ann : annotations) {
			cahce.add(ann.annotationType());
		}
		return cahce;
	}

	public OrmResolver(Object target) {
		this.target = target;
		inheritanceMap = new LinkedHashMap<Class<?>, Class<?>>();
		mapInheritance();
	}

	public OrmResolver(Class<?> classe) {
		inheritanceMap = new LinkedHashMap<Class<?>, Class<?>>();
		mapInheritance(classe);
	}

	private void mapInheritance(Class<?> classe) {
		while (classe.getSuperclass() != null
				&& (hasAnnotation(classe.getSuperclass(), Table.class) || classe
						.getSuperclass().equals(Object.class))) {
			inheritanceMap.put(classe, classe = classe.getSuperclass());
		}
	}

	private void mapInheritance() {
		Class<?> classe = target.getClass();
		mapInheritance(classe);
	}

	public Map<String, Object> format() {
		return format(target.getClass(), true);
	}

	public void parse(Map<String, Object> values) {
		parse(values, target.getClass());
	}

	public Map<String, Object> format(Class<?> classe, boolean deep) {
		List<Field> fields = getAllFields(new LinkedList<Field>(), classe, deep);
		Map<String, Object> map = formatBase(fields);
		return map;
	}
	
	public Map<String, Object> formatBase(List<Field> fields) {
		Map<String, Object> map = new HashMap<String, Object>();
		OrmTranslator translator = new OrmTranslator(fields);
		for (Field field : fields) {
			if(hasAnnotation(field, Transient.class)){
				continue;
			}else if (hasAnnotation(field, Relationship.class)) {
				// TODO tratar relacionamentos
			} else {
				String column = getSimpleColumnName(field,translator);
				if (column != null) {
					map.put(column, getBeanValue(field));
				}
			}
		}
		return map;
	}
	
	
	@SuppressWarnings("unused")
	private String getColumnName(Field field,OrmTranslator translator){
		if(!getTarget().getClass().equals(field.getDeclaringClass()) && hasAnnotation(field, Id.class)){
			Inheritance inheritance = getTarget().getClass().getAnnotation(Inheritance.class);
			Table table = getTarget().getClass().getAnnotation(Table.class);
			for(Iterator<ForwardKey> iterator = Arrays.asList(inheritance.joinFields()).iterator();iterator.hasNext();){
				ForwardKey fk = iterator.next();
				if(fk.foreginField().equals(translator.getColumn(field.getName()))){
					return table.name() + "." + fk.tableField();
				}
			}
		}else{
			return getSimpleColumnName(field,translator);
		}
		return null;
	}
	
	private String getSimpleColumnName(Field field,OrmTranslator translator){
		String column = translator.getColumn(field);
		if (column != null) {				
			Table table = field.getDeclaringClass().getAnnotation(Table.class);
			return table.name()+"."+column;
		}
		return null;
	}

	public Map<Class<?>, Map<String, Object>> formatDisjoin() {
		if (inheritanceMap.isEmpty()) {
			return null;
		}
		Map<Class<?>, Map<String, Object>> disjoinMap = new LinkedHashMap<Class<?>, Map<String, Object>>();
		Iterator<Class<?>> iterator = inheritanceMap.keySet().iterator();
		while (iterator.hasNext()) {
			Class<?> key = iterator.next();
			Map<String, Object> value = format(key, false);
			disjoinMap.put(key, value);
		}
		return disjoinMap;
	}

	public List<Field> getNotNullFields(List<Field> fields, Class<?> type,boolean deep) {
		List<Field> all = getAllFields(fields, type, deep);
		List<Field> remove = new ArrayList<Field>();
		OrmTranslator translator = new OrmTranslator(all);
		for (Field field : all) {
			Object value = null;
			try {
				if (translator.getColumn(field) != null) {
					value = getBeanValue(field);
				}
			} catch (Exception ex) {
			}
			if (value == null) {
				remove.add(field);
			}
		}
		all.removeAll(remove);
		return all;
	}

	public static List<Field> getKeyFields(List<Field> fields, Class<?> type,boolean deep) {
		List<Field> all = getAllFields(fields, type, deep);
		List<Field> remove = new ArrayList<Field>();
		for (Field field : all) {
			if (!hasAnnotation(field, Id.class)) {
				remove.add(field);
			}
		}
		all.removeAll(remove);
		return all;
	}

	public static List<Field> getAllFields(List<Field> fields, Class<?> type, boolean deep) {
		if (fieldsCache.containsKey(type)) {
			fields.addAll(fieldsCache.get(type));
		} else {
			List<Field> parc = new ArrayList<Field>();
			for (Field field : type.getDeclaredFields()) {
				parc.add(field);
			}
			fields.addAll(parc);
			fieldsCache.put(type, parc);
		}
		Class<?> superType = type.getSuperclass();
		if (superType != null) {
			Table table = superType.getAnnotation(Table.class);
			if ((table != null && deep) || (table == null && !deep)) {
				if (fieldsCache.containsKey(superType)) {
					fields.addAll(fieldsCache.get(superType));
				} else {
					fields = getAllFields(fields, superType, deep);
				}
			}
		}
		return fields;
	}

	public void parse(Map<String, Object> values, Class<?> classe) {
		List<Field> fields = getAllFields(new LinkedList<Field>(), classe, true);
		OrmTranslator translator = new OrmTranslator(fields);
		for (String column : values.keySet()) {
			Field field = translator.getFieldByColumnName(column);
			if(field == null || hasAnnotation(field, Transient.class)){
				continue;
			}else if (hasAnnotation(field, Relationship.class)) {
				// TODO tratar relacionamentos
			} else {
				setBeanValue(field, values.get(column));
			}
		}
	}

	private void setBeanValue(Field field, Object value) {
		try {
			if((value instanceof String) && !field.getType().equals(String.class)){
					value = attemptSyncValueType(value,field.getType());
			}
			if(!alreadyTyped(value,field)){
				if (field.getType().isEnum() && hasAnnotation(field, Enumerator.class)) {
					value = reTypeSyncEnum(value, field, field.getAnnotation(Enumerator.class).type());
				} else if (Number.class.isAssignableFrom(field.getType())) {
					value = reTypeSyncNumber(value, field.getType());
				} else if (String.class.isAssignableFrom(field.getType())) {
					value = reTypeSyncString(value);
				}
			}

			BeanProperty.instance().setNestedProperty(target, field.getName(), value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private boolean alreadyTyped(Object value,Field field){
		if(value == null || field == null){
			return false;
		}
		return value.getClass().isAssignableFrom(field.getType());
	}
	
	private String reTypeSyncString(Object value) {
		if (value == null) {
			return null;
		}
		return String.valueOf(value);
	}

	public Number reTypeSyncNumber(Object value, Class<?> type) {
		Number num = (Number) value;
		if(value == null){
			return num;
		}
		if (type.isAssignableFrom(Integer.class)) {
			return num.intValue();
		} else if (type.isAssignableFrom(Long.class)) {
			return num.longValue();
		} else if (type.isAssignableFrom(Short.class)) {
			return num.shortValue();
		} else if (type.isAssignableFrom(Byte.class)) {
			return num.byteValue();
		} else if (type.isAssignableFrom(Double.class)) {
			return num.doubleValue();
		} else if (type.isAssignableFrom(Float.class)) {
			return num.floatValue();
		}
		return num;
	}

	private <T> T attemptSyncValueType(Object value,Class<T> type){
		return StringConverterFactory.getConverter(type).getAsObject(value.toString());
	}
	
	private Object reTypeSyncEnum(Object value, Field field, EnumValueType type) {
		if (type == null || value == null) {
			return null;
		}
		if ((type == EnumValueType.ORDINAL && value instanceof Number) || (type == EnumValueType.NAME && value instanceof String)) {
			if (type == EnumValueType.ORDINAL) {
				Object[] values = field.getType().getEnumConstants();
				Number index = (Number) value;
				if (index.intValue() >= 0 || index.intValue() < values.length) {
					return values[index.intValue()];
				}
			} else if (type == EnumValueType.NAME) {
				try {
					return field.getType().getDeclaredField(
							String.valueOf(value)).get(null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private Object getBeanValue(Field field) {
		if (target == null) {
			return null;
		}
		try {
			Object value = BeanProperty.instance().getNestedProperty(target,
					field.getName());
			if (Enum.class.isAssignableFrom(field.getType())
					&& hasAnnotation(field, Enumerator.class)) {
				return getEnumValue(value, field
						.getAnnotation(Enumerator.class).type());
			}
			return value;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object getEnumValue(Object value, EnumValueType type) {
		if (type == null) {
			return null;
		}
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

	public Object getTarget() {
		return target;
	}

	public Map<Class<?>, Class<?>> getInheritanceMap() {
		return inheritanceMap;
	}
}