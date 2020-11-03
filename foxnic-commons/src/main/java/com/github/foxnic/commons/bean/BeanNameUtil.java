package com.github.foxnic.commons.bean;

import java.util.HashMap;

import com.github.foxnic.commons.lang.StringUtil;

/**
 * 命名转换器的默认实现
 * @author fangjieli
 */
public class BeanNameUtil  {
 
	private final  static String UNDERLINE="_";
	private final  static String IS="is";
	private final  static String SET="set";
	private final  static String GET="get";
	
	/**
	 * 把带下划线的名称转成Java命名风格的类名
	 * @param name 带下划线的命名
	 * @return Java命名风格的类名
	 * */
	public String getClassName(String name)
	{
		String[] itms=name.split(UNDERLINE);
		String clsName="";
	 
		for (String part : itms) {
			part=part.toLowerCase();
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			clsName+=part;
		}
		return clsName;
	}
	
	/**
	 * 转属性名
	 * @param columnName 带下划线的命名
	 * @return Java命名风格的类名
	 * */
	public String getPropertyName(String columnName)
	{
		String[] itms=columnName.split(UNDERLINE);
		String clsName="";
		int i=0;
		for (String part : itms) {
			i++;
			part=part.toLowerCase();
			if(i>1)
			{
				part=part.substring(0, 1).toUpperCase()+part.substring(1);
			}
			clsName+=part;
		}
		return clsName;
	}
 
	/**
	 * 转get方法名
	 * 
	 * @param columnName 带下划线的命名
	 * @param isBooleanType 是否逻辑类型
	 * @return Java命名风格的类名
	 * */
	public String getGetMethodName(String columnName,boolean isBooleanType)
	{
		String[] itms=columnName.split(UNDERLINE);
		String clsName=GET;
		if(isBooleanType)
		{
			clsName=IS;
		}
		int i=0;
		for (String part : itms) {
			i++;
			part=part.toLowerCase();
			if(i==1 && isBooleanType && IS.equals(part))
			{
				continue;
			}
			
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			 
			clsName+=part;
		}
		return clsName;
	}
	
	private static HashMap<String, String> SIMPLE_SET_METHOD_NAMES=new HashMap<String, String>();
	
	/**
	 * 转简易的set方法名
	 * 
	 * @param columnName 带下划线的命名
	 * @return Java命名风格的类名
	 * */
	public String getSimpleSetMethodName(String columnName)
	{
		String r=SIMPLE_SET_METHOD_NAMES.get(columnName);
		if(r!=null) {
			return r;
		}
		if(columnName.length()==0) {
			return columnName;
		}
		if(columnName.length()==1) {
			return columnName.toUpperCase();
		}
		r=SET+columnName.substring(0, 1).toUpperCase()+columnName.substring(1);
		SIMPLE_SET_METHOD_NAMES.put(columnName, r);
		return r;
	}
	
	
	private static HashMap<String, String> SIMPLE_GET_METHOD_NAMES=new HashMap<String, String>();
	
	/**
	 * 转简易get方法名
	 * 
	 * @param columnName 带下划线的命名
	 * @return Java命名风格的类名
	 * */
	public String getSimpleGetMethodName(String columnName)
	{
		String r=SIMPLE_GET_METHOD_NAMES.get(columnName);
		if(r!=null) {
			return r;
		}
		if(columnName.length()==0) {
			return columnName;
		}
		if(columnName.length()==1) {
			return columnName.toUpperCase();
		}
		r=GET+columnName.substring(0, 1).toUpperCase()+columnName.substring(1);
		SIMPLE_GET_METHOD_NAMES.put(columnName, r);
		return r;
	}
	
	
	private static HashMap<String, String> SET_METHOD_NAMES=new HashMap<String, String>();
	
	/**
	 * 转set方法名
	 * 
	 * @param columnName 带下划线的命名
	 * @param isBooleanType 是否逻辑类型
	 * @return Java命名风格的类名
	 * */
	public String getSetMethodName(String columnName,boolean isBooleanType)
	{
		String clsName=SET_METHOD_NAMES.get(columnName+isBooleanType);
		if(clsName!=null) {
			return clsName;
		}
		String[] itms=columnName.split("_");
		clsName=SET;
		int i=0;
		for (String part : itms) {
			i++;
			part=part.toLowerCase();
			if(i==1 && isBooleanType && IS.equals(part))
			{
				continue;
			}
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			 
			clsName+=part;
		}
		SIMPLE_SET_METHOD_NAMES.put(columnName, clsName);
		return clsName;
	}
	
	/**
	 * 把JAVA命名转换成下划线分隔的样式
	 * @param name java风格命名
	 * @return 按大小写拆分后的命名
	 * */
	public String depart(String name)
	{
		String[] ns=name.split(UNDERLINE);
		StringBuilder builder=new StringBuilder();
		for (String n : ns) {
			if(n.isEmpty()) {
				continue;
			}
			if(builder.length()==0){
				builder.append(departInternal(n));
			}
			else {
				builder.append(UNDERLINE+departInternal(n));
			}
		}
		return builder.toString();
	}
	
	private String departInternal(String name)
	{
		if(name==null) {
			return null;
		}
		name=name.trim();
		if(name.length()==0) {
			return name;
		}
		
		name=StringUtil.removeFirst(name, UNDERLINE);
		name=StringUtil.removeLast(name, UNDERLINE);
		
		Character c;
		StringBuilder builder=new StringBuilder();
		String prevSeg=null;
		String upperSeg=null;
		int s=0;
		int e=-1;
		int p=-1;
		for (int i = 0; i < name.length(); i++) { 
			c=name.charAt(i);
			//忽略第一个字符
			if(i==0) {
				continue;
			}
			//
			if(Character.isUpperCase(c))
			{
				p=i;
				int j = i+1;
				for ( ; j < name.length(); j++) {
					c=name.charAt(j);
					if(Character.isLowerCase(c)) {
						e=j;
						break;
					}
				}
				
				i=j;
				
				prevSeg=name.substring(s, p);
				//后续字符全部为大写
				if(e==-1) {
					upperSeg=name.substring(p);
					s=-1;
				} 
				//后续有小写字符
				else {
					//相差一个字符
					if(e-p==1) {
						//检索结束
						if(e==name.length())
						{
							upperSeg=name.substring(p);
						}
						//检索未结束
						else
						{
							upperSeg=null;
						}
						s=p;
					}
					//相差多个字符，中间部分为全大写
					else if(e-p>1) {
						if(e==name.length())
						{
							upperSeg=name.substring(e);
							s=-1;
						}
						//检索未结束
						else
						{
							//提取中间大写补部分
							upperSeg=name.substring(p,e-1);
							s=e-1;
						}
						
					}
					 
					
				}
				
				if(builder.length()==0)
				{
					builder.append(prevSeg);
				}
				else
				{
					builder.append(UNDERLINE+prevSeg);
				}
				prevSeg=null;
				
				if(upperSeg!=null)
				{
					builder.append(UNDERLINE+upperSeg);
					upperSeg=null;
				}
				e=-1;
				p=-1;
				
			}
 
		}
		
		//没有找到任何一个大写的字符
		if(e==-1)
		{
			if(s==0)
			{
				return name.toLowerCase();
			}
			else if(s>0)
			{
				prevSeg=name.substring(s);
				builder.append(UNDERLINE+prevSeg);
			}
		}
	 
		return builder.toString().toLowerCase();
		 
		
	}
 
	
}
