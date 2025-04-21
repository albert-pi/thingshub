package io.thingshub;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.thingshub.ioc.Value;

public class Configurer {

	private static final Pattern ANNOTATION_VALUE_PATTERN = Pattern.compile("\\$\\{(.+)\\}");

	public static void inject(Map<String, Object> configs, Object bean) throws IllegalAccessException {
		Class<?> clazz = bean.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Value theAnno = field.getAnnotation(Value.class);
			if (theAnno != null) {
				field.setAccessible(true);
				String configName = theAnno.value();
				Matcher matcher = ANNOTATION_VALUE_PATTERN.matcher(configName);
				while (matcher.find()) {
					String[] nameAndDefault = matcher.group(1).split(":");
					Object value = configs.get(nameAndDefault[0].trim());
					if (value != null) {
						field.set(bean, value);
					} else if (nameAndDefault.length > 1) {
						switch (field.getType().getTypeName()) {
						case "java.lang.String":
							field.set(bean, nameAndDefault[1].trim());
							break;
						case "long":
						case "java.lang.Long":
							field.set(bean, Long.valueOf(nameAndDefault[1].trim()));
							break;
						case "int":
						case "java.lang.Integer":
							field.set(bean, Integer.valueOf(nameAndDefault[1].trim()));
							break;
						case "short":
						case "java.lang.Short":
							field.set(bean, Short.valueOf(nameAndDefault[1].trim()));
							break;
						case "byte":
						case "java.lang.Byte":
							field.set(bean, Byte.valueOf(nameAndDefault[1].trim()));
							break;
						case "boolean":
						case "java.lang.Boolean":
							field.set(bean, Boolean.valueOf(nameAndDefault[1].trim()));
							break;
						case "float":
						case "java.lang.Float":
							field.set(bean, Float.valueOf(nameAndDefault[1].trim()));
							break;
						case "double":
						case "java.lang.Double":
							field.set(bean, Double.valueOf(nameAndDefault[1].trim()));
							break;
						case "java.math.BigDecimal":
							field.set(bean, new BigDecimal(nameAndDefault[1].trim()));
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}
}