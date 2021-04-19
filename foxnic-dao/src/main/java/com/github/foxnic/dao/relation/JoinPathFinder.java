package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.bean.BeanNameUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JoinPathFinder {
	
	private static final BeanNameUtil beanNameUtil=new BeanNameUtil();

	private final PropertyRoute prop;
	private final String poTable;
	private final String targetTable;
	private final String[] usingProps;
	private final List<String> routeTables;
	private final Map<String,String[]> routeFields;
	private  List<Join> allJoins=null;
	
	private static List<Join> ALL_JOINS=new ArrayList<>();

	static void clearCache() {
		ALL_JOINS.clear();
	}
	/**
	 * 初始化所有的 Join 关系
	 * */
	private void initTotalJoins(List<Join> joins) {
		if(ALL_JOINS.size()==0) {
	    	synchronized (ALL_JOINS) {
	    		if(ALL_JOINS.size()==0) {
	    	    	for (Join join : joins) {
	    	    		ALL_JOINS.add(join);
	    	    		ALL_JOINS.add(join.getRevertJoin());
	    			}
	        	}
			}
    	}
		allJoins=new ArrayList<>();
		allJoins.addAll(ALL_JOINS);
	}
	
	public JoinPathFinder(PropertyRoute prop, List<Join> joins,String poTable, String targetTable,String[] usingProps,List<String> routeTables,Map<String,String[]> routeFields) {
		this.prop=prop;
		this.poTable=poTable;
		this.targetTable=targetTable;
		this.usingProps=usingProps;
		this.routeTables=routeTables;
		this.routeFields=routeFields;
		
		//如果只有一个，就自动加入 targetTable 作为 route
		if(this.routeTables.size()==1) {
			this.routeTables.add(targetTable);
		}
		
		//如果有多个，并且最后一个不是 targetTable， 就自动加入 targetTable 作为 route
		if(this.routeTables.size()>1 && !this.routeTables.get(this.routeTables.size()-1).equalsIgnoreCase(targetTable)) {
			this.routeTables.add(targetTable);
		}
		
		if(usingProps==null || usingProps.length==0) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() + " usingProps 未指定");
		}
		//
    	initTotalJoins(joins);
	}

	private List<Join> paths=new ArrayList<>();

	public List<Join> find() {
		//寻找关联源
		List<Join> sourceJoin=findSourceJoin();
		if(sourceJoin.size()==0) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() +" 未发现匹配的 Join 关系，请使用 using 方法指定使用的关联字段");
		}
		if(sourceJoin.size()>1) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() +"发现多个匹配的 Join 关系，请指定精确的Join路径，并请使用 using 方法指定使用的关联字段");
		}
		
		Join firstJoin=sourceJoin.get(0);
 
		allJoins.remove(firstJoin);
		allJoins.remove(firstJoin.getRevertJoin());
		paths.add(firstJoin);

		//判断是否已经找到
		boolean findNext=true;
		if(firstJoin.getTargetTable().equalsIgnoreCase(this.targetTable)) {
			if(this.routeTables!=null && this.routeTables.size()>1) {
				if(2==this.routeTables.size()) {
					findNext=false;
				}
			}
		}

		if(findNext) {
			findNextJoin(2, firstJoin);
		}
		
		Collections.reverse(paths);
		
		return paths;
 
	}
	
	private void findNextJoin(int index, Join currJoin) {
		 
		String tempTable=null;
		if(this.routeTables!=null && index<this.routeTables.size()) {
			tempTable=this.routeTables.get(index);
		}
		
		String routeTable=tempTable;
		String[] routeFields=this.routeFields.get(routeTable);
		
		List<Join> sourceJoins=new ArrayList<>();
		for (Join join : allJoins) {
			System.out.println(currJoin.getTargetTable()+" -> "+join.getSourceTable());
			if( !currJoin.getTargetTable().equalsIgnoreCase(join.getSourceTable()) ) continue;
			if(currJoin.getTargetTableFields().size() != join.getSourceTableFields().size()) continue;
			//List<String> targetFields=currJoin.getTargetTableFields();
			List<String> sourceFields=join.getSourceTableFields();
			for (int i = 0; i < sourceFields.size()  ; i++) {
				//if(isFieldMatch(sourceFields.get(i),targetFields.get(i))) {
					if(isTargetMatch(join,routeTable,routeFields)) {
						sourceJoins.add(join);
					}
				//}
			}
		}
		
		//找不到，且表名匹配 , 就退出查找
		if(sourceJoins.size()==0  && paths.get(paths.size()-1).getTargetTable().equalsIgnoreCase(targetTable)) {
			return;
		}
		
		if(sourceJoins.size()==0) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() +" 未发现匹配的 Join 关系");
		}
				
		if(sourceJoins.size()>1) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() + " 发现多个匹配的 Join 关系，请指定精确的Join路径");
		}
		
		Join join=sourceJoins.get(0);
		 
		allJoins.remove(join);
		allJoins.remove(join.getRevertJoin());
		paths.add(join);
		
		boolean findNext=true;
		
		//是否符合退出条件
		if(join.getTargetTable().equalsIgnoreCase(this.targetTable)) {
			if(this.routeTables!=null && this.routeTables.size()>1) {
				if((index+1)>=this.routeTables.size()) {
					findNext=false;
				}
			}
		}
		
		if(findNext) {
			findNextJoin(index+1, join);
		}
 
		
	}

	
	
	
	/**
	 * 查找第一个Join
	 * */
	private List<Join> findSourceJoin() {
		
		String tempTable=null;
		if(this.routeTables!=null && 1<this.routeTables.size()) {
			tempTable=this.routeTables.get(1);
		}
		String routeTable=tempTable;
		String[] routeFields=this.routeFields.get(routeTable);
		List<Join> sourceJoins=new ArrayList<>();
		for (Join join : allJoins) {
			if(!join.getSourceTable().equalsIgnoreCase(this.poTable)) continue;
			if(join.getSourceTableFields().size()!=this.usingProps.length) continue;
			List<String> sourceFields=join.getSourceTableFields();
			for (int i = 0; i < this.usingProps.length; i++) {
				if(isFieldMatch(sourceFields.get(i),this.usingProps[i])) {
					if(isTargetMatch(join,routeTable,routeFields)) {
						sourceJoins.add(join);
					}
				}
			}
		}
		return sourceJoins;
	}

	private boolean isTargetMatch(Join join, String routeTable, String[] routeFields) {
		
		if(routeTable!=null && !routeTable.equalsIgnoreCase(join.getTargetTable())) return false;
		
		if(routeFields!=null && routeFields.length>0) {
			if(routeFields.length!=join.getTargetTableFields().size()) {
				throw new IllegalArgumentException("参数数量不一致");
			}
			for (int i = 0; i < join.getTargetTableFields().size(); i++) {
				if(! isFieldMatch(join.getTargetTableFields().get(i), routeFields[i])) {
					return false;
				}
			}	
		}
		return true;
	}
	
	
	private boolean isFieldMatch(String fieldInJoin,String prop) {
		return  fieldInJoin.equalsIgnoreCase(prop) ||
				fieldInJoin.equalsIgnoreCase(beanNameUtil.depart(prop)) ||
				fieldInJoin.equalsIgnoreCase(beanNameUtil.getPropertyName(prop));
	}
	
	

 
	
}
