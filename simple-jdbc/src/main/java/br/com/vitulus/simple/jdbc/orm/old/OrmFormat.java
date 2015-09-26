package br.com.vitulus.simple.jdbc.orm.old;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class OrmFormat {

	private OrmResolver ormResolver;

	public OrmFormat(Object target) {
		this.ormResolver = new OrmResolver(target);
	}

	public Map<String, Object> format() {
		return ormResolver.format();
	}

	public Map<Class<?>, Map<String, Object>> formatDisjoin() {
		return ormResolver.formatDisjoin();
	}

	public Map<String, Object> format(String... fields) {
		ProjectionResult mask = new ProjectionResolver(ormResolver.getTarget().getClass(), fields);
		return ormResolver.formatBase(mask.getProjectionFields());
	}

	public Map<String, Object> formatNotNull() {
		Class<?> type = getOrmResolver().getTarget().getClass();
		List<Field> fields = ormResolver.getNotNullFields(new ArrayList<Field>(), type, true);
		return ormResolver.formatBase(fields);
	}

	public Map<String, Object> formatKey() {
		Class<?> type = getOrmResolver().getTarget().getClass();
		List<Field> fields = OrmResolver.getKeyFields(new ArrayList<Field>(),type, true);
		return ormResolver.formatBase(fields);
	}

	public Map<Class<?>, Map<String, Object>> formatDisjoin(String... fields) {
		ProjectionResult mask = new ProjectionResolver(this.getClass(), fields);
		Map<Class<?>, Map<String, Object>> map = new LinkedHashMap<Class<?>, Map<String, Object>>();
		for (Class<?> classe : ormResolver.getInheritanceMap().keySet()) {
			map.put(classe, ormResolver.formatBase(mask.getProjectionFields(classe)));
		}
		return map;
	}
	
	public static Map<String,Object> getCleanFormat(Map<String, Object> formated){
		Map<String,Object> clean = new HashMap<String, Object>();
		for(Map.Entry<String, Object> entry : formated.entrySet()){
			String key = entry.getKey();
			if(entry.getKey().contains(".")){
				key = key.substring(key.lastIndexOf(".") + 1);
			}
			clean.put(key, entry.getValue());
		}
		return clean;
	}

	public void parse(Map<String, Object> values) {
		ormResolver.parse(values);
	}

	public OrmResolver getOrmResolver() {
		return ormResolver;
	}

}