package com.github.foxnic.sql.entity.naming;

import com.github.foxnic.sql.meta.DBDataType;

/**
 * 命名转换器的默认实现
 * @author fangjieli
 *
 */
public class DefaultNameConvertor implements INameConvertor {
	
	@Override
	public String getPackageName(String tableName)
	{
		int i=tableName.indexOf("_");
		if(i==-1)
		{
			return "defaults";
		}
		return tableName.substring(0, i).toLowerCase();
	}
	
	@Override
	public String getClassName(String tableName,int startPartIndex)
	{
		if(startPartIndex<0) startPartIndex=0;
		String[] itms=tableName.split("_");
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
		String[] itms=name.split("_");
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
		String[] itms=columnName.split("_");
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
		String[] itms=columnName.split("_");
		String clsName="get";
		if(cata==DBDataType.BOOL)
		{
			clsName="is";
		}
 
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
	public String getSetMethodName(String columnName,DBDataType cata)
	{
		if("IS_ACTIVE".equalsIgnoreCase(columnName) || "DELETED".equalsIgnoreCase(columnName)) {
			System.out.println();
		}
		
		String[] itms=columnName.split("_");
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
	
}
