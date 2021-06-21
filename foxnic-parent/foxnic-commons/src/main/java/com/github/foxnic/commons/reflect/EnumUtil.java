package com.github.foxnic.commons.reflect;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.cache.LocalCache;

import java.lang.reflect.Method;

public class EnumUtil {

	public static CodeTextEnum parseByCode(String enumTypeName, String code) {
		Class<? extends CodeTextEnum> enumType=ReflectUtil.forName(enumTypeName);
		if(enumType==null) return null;
		return parseByCode(enumType,code);
	}

	public static CodeTextEnum parseByCode(Class<? extends CodeTextEnum> enumType, String code) {
		CodeTextEnum[] values=getValues(enumType);
		if(values==null) return null;
		for (CodeTextEnum value : values) {
			if(value.code().equals(code)) return value;
		}
		return null;
	}

	public static CodeTextEnum parseByName(String enumTypeName, String name) {
		Class<? extends CodeTextEnum> enumType=ReflectUtil.forName(enumTypeName);
		if(enumType==null) return null;
		return parseByName(enumType,name);
	}

	public static CodeTextEnum parseByName(Class<? extends CodeTextEnum> enumType, String name) {
		CodeTextEnum[] values=getValues(enumType);
		if(values==null) return null;
		for (CodeTextEnum value : values) {
			if(value.name().equals(name)) return value;
		}
		return null;
	}

	/***
	 * 获得指定枚举的所有实例
	 * */
	public static CodeTextEnum[] getValues(String enumTypeName) {
		CodeTextEnum[] values=VALUE_CACHE.get(enumTypeName);
		if(values!=null) return values;
		Class<? extends CodeTextEnum> enumType=ReflectUtil.forName(enumTypeName);
		return getValues(enumType);
	}


	private static LocalCache<String, CodeTextEnum[]> VALUE_CACHE=new LocalCache<String, CodeTextEnum[]>();
	/***
	 * 获得指定枚举的所有实例
	 * */
	public static CodeTextEnum[] getValues(Class<? extends CodeTextEnum> enumType) {
		CodeTextEnum[] values=VALUE_CACHE.get(enumType.getName());
		if(values!=null) return values;
		try {
			Method m = enumType.getDeclaredMethod("values");
			values = (CodeTextEnum[]) m.invoke(m, null);
			VALUE_CACHE.put(enumType.getName(),values);
			return  values;
		} catch (Exception e) {
			return null;
		}
	}


}
