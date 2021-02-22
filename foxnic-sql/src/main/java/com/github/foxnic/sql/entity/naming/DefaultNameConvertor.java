package com.github.foxnic.sql.entity.naming;

import java.util.ArrayList;

import com.github.foxnic.sql.meta.DBDataType;

/**
 * 命名转换器的默认实现
 * @author fangjieli
 *
 */
public class DefaultNameConvertor implements INameConvertor {
	
	private boolean isDBMode=true;
	
	public DefaultNameConvertor() {
		this.isDBMode=true;
	}
	
	public DefaultNameConvertor(boolean isDBMode) {
		this.isDBMode=isDBMode;
	}
	
	@Override
	public String getPackageName(String tableName)
	{
		int i=indexOfSplitChar(tableName);
		if(i==-1) {
			return "defaults";
		}
		return tableName.substring(0, i).toLowerCase();
	}
	
	@Override
	public String getClassName(String tableName,int startPartIndex)
	{
		if(startPartIndex<0) startPartIndex=0;
		String[] itms=split(tableName);
		if(startPartIndex>=itms.length) startPartIndex=0;
 
		String clsName="";
		String part="";
		for (int i = 0; i < itms.length; i++) {
			if(i<startPartIndex) continue;
			part=itms[i].toLowerCase();
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			clsName+=part;
		}
 
		return clsName;
	}
	
	@Override
	public String toClassNameStyle(String name)
	{
		String[] itms=this.split(name);
		String clsName="";
	 
		for (String part : itms) {
			part=part.toLowerCase();
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			clsName+=part;
		}
		return clsName;
	}
	
	@Override
	public String getPropertyName(String columnName)
	{
		
		String[] itms=split(columnName);
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
	
	@Override
	public String getGetMethodName(String columnName,DBDataType cata)
	{
		if("is_active".equals(columnName))
		{
			System.out.println();
		}
		String[] itms=split(columnName);
		String clsName="get";
		if(cata==DBDataType.BOOL) {
			clsName="is";
		}
 
		int i=0;
		for (String part : itms) {
			i++;
			part=part.toLowerCase();
			if(i==1 && cata==DBDataType.BOOL && "is".equals(part)) {
				continue;
			}
			
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			 
			clsName+=part;
		}
		return clsName;
	}
	
	
	@Override
	public String getSetMethodName(String columnName,DBDataType cata)
	{
		if("IS_ACTIVE".equalsIgnoreCase(columnName) || "DELETED".equalsIgnoreCase(columnName)) {
			System.out.println();
		}
		
		String[] itms=split(columnName);
		String clsName="set";
		int i=0;
		for (String part : itms) {
			i++;
			part=part.toLowerCase();
			if(i==1 && cata==DBDataType.BOOL && "is".equals(part))
			{
				continue;
			}
			part=part.substring(0, 1).toUpperCase()+part.substring(1);
			 
			clsName+=part;
		}
		return clsName;
	}

	@Override
	public String getRcdGetMethodName(DBDataType categery) {
		String type=categery.getType().getSimpleName();
		return "get"+type;
	}
	
	@Override
	public String getJoinId(String tableName,int startPartIndex,String[] fieldsA,String[] fieldsB,String[] relarionFields) {
		return getJoinId(getClassName(tableName,startPartIndex), fieldsA, fieldsB, relarionFields);
	}

	@Override
	public String getJoinId(String entityClassName,String[] fieldsA,String[] fieldsB,String[] relarionFields) {
		String key="join-"+entityClassName.toLowerCase()+"-on(";
		boolean hasRelarion=true;
		if(relarionFields!=null)
		{
			for (String string : relarionFields) {
				if(string==null) 
				{
					hasRelarion=false;
					break;
				}
			}
		}
		else
		{
			hasRelarion=false;
		}
		if(!hasRelarion)
		{
			for (int i = 0; i < fieldsB.length; i++) {
				key+="local."+fieldsA[i]+"=entity."+fieldsB[i]+" and ";
			}
		}
		else
		{
			for (int i = 0; i < fieldsB.length; i++) {
				if(fieldsA[i]!=null)
				{
					key+="local."+fieldsA[i]+"=rel."+relarionFields[i]+" and ";
				}
				if(fieldsB[i]!=null)
				{
					key+="entity."+fieldsB[i]+"=rel."+relarionFields[i]+" and ";
				}
			}
		}
		key=key.substring(0,key.length()-5)+")";
		return key;
	}
	
	private final char[] splitChars=new char[] {'_',' ','-','\t'};
	
	private boolean containSplitChar(String s) {
		if(isDBMode) {
			return s.contains("_");
		}
		for (char c : splitChars) {
			if(s.contains(c+"")) return true;
		}
		return false;
	}
	
	private int indexOfSplitChar(String s) {
		if(isDBMode) {
			return s.indexOf('_');
		}
		int z=Integer.MAX_VALUE;
		int i;
		for (char c : splitChars) {
			 i=s.indexOf(c);
			 if(z>i) {
				 z=i;
			 }
		}
		if(z==Integer.MAX_VALUE) {
			z=-1;
		}
		return z;
	}
	
	private boolean isSplitChar(char c) {
		if(isDBMode) {
			return '_'==c;
		}
		for (char s : splitChars) {
			if(s==c) return true;
		}
		return false;
	}
	
	public String[] split(String s) {
		if(isDBMode) {
			return s.split("_");
		}
		char c;
		String p;
		boolean up;
		ArrayList<String> list=new ArrayList<String>();
		int j=0;
		for (int i = 0; i < s.length(); i++) {
			c=s.charAt(i);
			up=Character.isUpperCase(c);
			if(c=='_' || c=='-' || c==' ' || c=='\t' || up) {
				p=s.substring(j,i);
				if(p.length()>0) {
					list.add(p.toLowerCase());
				}
				j=i+(up?0:1);
			}
		}
		p=s.substring(j);
		if(p.length()>0) {
			list.add(p.toLowerCase());
		}
		return list.toArray(new String[0]);
	}
	
	
}
