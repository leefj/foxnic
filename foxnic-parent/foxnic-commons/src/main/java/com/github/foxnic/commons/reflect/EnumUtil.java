package com.github.foxnic.commons.reflect;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.lang.StringUtil;

import java.lang.reflect.Method;

public class EnumUtil {

	public static CodeTextEnum parseByCode(String enumTypeName, String code) {
		Class<? extends CodeTextEnum> enumType=ReflectUtil.forName(enumTypeName);
		if(enumType==null) return null;
		return parseByCode(enumType,code);
	}

	public static <M extends CodeTextEnum> M parseByCode(Class<M> enumType, String code) {
		CodeTextEnum[] values=getValues(enumType);
		if(values==null) return null;
		for (CodeTextEnum value : values) {
			if(value.code().equals(code)) return (M)value;
		}
		return null;
	}

	public static CodeTextEnum parseByCode(CodeTextEnum[] values, String code) {
		if(values==null) return null;
		if(StringUtil.isBlank(code)) return null;
		for (CodeTextEnum value : values) {
			if(value.code().equals(code)) return value;
		}
		for (CodeTextEnum value : values) {
			if(value.name().equals(code)) return value;
		}
		return null;
	}

	public static CodeTextEnum parseByName(String enumTypeName, String name) {
		if(StringUtil.isBlank(name)) return null;
		Class<? extends CodeTextEnum> enumType=ReflectUtil.forName(enumTypeName);
		if(enumType==null) return null;
		return parseByName(enumType,name);
	}

	public static CodeTextEnum parseByName(Class<? extends CodeTextEnum> enumType, String name) {
		if(StringUtil.isBlank(name)) return null;
		CodeTextEnum[] values=getValues(enumType);
		if(values==null) return null;
		for (CodeTextEnum value : values) {
			if(value.name().equals(name)) return value;
		}
		return null;
	}

	public static CodeTextEnum parseByText(Class<? extends CodeTextEnum> enumType, String text) {
		if(StringUtil.isBlank(text)) return null;
		CodeTextEnum[] values=getValues(enumType);
		if(values==null) return null;
		for (CodeTextEnum value : values) {
			if(value.text().equals(text)) return value;
		}
		return null;
	}

	/***
	 * 获得指定枚举的所有实例
	 * */
	public static CodeTextEnum[] getValues(String enumTypeName) {
		CodeTextEnum[] values= CODE_TEXT_ENUM_VALUE_CACHE.get(enumTypeName);
		if(values!=null) return values;
		Class<? extends CodeTextEnum> enumType=ReflectUtil.forName(enumTypeName);
		return getValues(enumType);
	}

	public static <T extends Enum>  T[] getEnumValues(Class<T> enumType) {
		try {
			T[] values = (T[])ENUM_VALUE_CACHE.get(enumType.getName());
			if(values!=null) return values;
			Method valuesMethod = enumType.getDeclaredMethod("values");
			values = (T[]) valuesMethod.invoke(null);
			ENUM_VALUE_CACHE.put(enumType.getName(),values);
			return  values;
		} catch (Exception e) {
			return null;
		}
	}


	private static LocalCache<String, Enum[]> ENUM_VALUE_CACHE =new LocalCache<String, Enum[]>();

	private static LocalCache<String, CodeTextEnum[]> CODE_TEXT_ENUM_VALUE_CACHE =new LocalCache<String, CodeTextEnum[]>();
	/***
	 * 获得指定枚举的所有实例
	 * */
	public static CodeTextEnum[] getValues(Class<? extends CodeTextEnum> enumType) {
		CodeTextEnum[] values= CODE_TEXT_ENUM_VALUE_CACHE.get(enumType.getName());
		if(values!=null) return values;
		try {
			Method m = enumType.getDeclaredMethod("values");
			values = (CodeTextEnum[]) m.invoke(m, null);
			CODE_TEXT_ENUM_VALUE_CACHE.put(enumType.getName(),values);
			return  values;
		} catch (Exception e) {
			return null;
		}
	}


}
