package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class JoinPathFinder {
	
	private static final BeanNameUtil beanNameUtil=new BeanNameUtil();

	private final PropertyRoute prop;
	private final DBTable poTable;
	private final DBTable targetTable;
	private final DBField[] usingProps;
	private final List<DBTable> routeTables;
	private final Map<String,DBField[]> routeFields;
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
	    			//初始化全集，并去重
	    	    	Set<String> keys=new HashSet<>();
	    			for (Join join : joins) {
	    				if(!keys.contains(join.toString())) {
	    					ALL_JOINS.add(join);
	    					keys.add(join.toString());
	    				}
	    				if(!keys.contains(join.getRevertJoin().toString())) {
	    					ALL_JOINS.add(join.getRevertJoin());
	    					keys.add(join.getRevertJoin().toString());
	    				}
	    			}
	        	}
			}
    	}
		
		allJoins=new ArrayList<>();
		for (Join join : ALL_JOINS) {
			if(!isInRoute(join)) continue;
			allJoins.add(join);
		}
		
	}
	
	private boolean isInRoute(Join join) {
		for (DBTable table : this.routeTables) {
			if( table.name().equalsIgnoreCase(join.getSourceTable())   || table.name().equalsIgnoreCase(join.getTargetTable())  ) {
				return true;
			}
		}
		return false;
	}
	
	public JoinPathFinder(PropertyRoute prop, List<Join> joins,DBTable poTable, DBTable targetTable,DBField[] usingProps,List<DBTable> routeTables,Map<String,DBField[]> routeFields) {
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
		if(this.routeTables.size()>1 && !this.routeTables.get(this.routeTables.size()-1).name().equalsIgnoreCase(targetTable.name())) {
			this.routeTables.add(targetTable);
		}
		
		if(usingProps==null || usingProps.length==0) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() + " usingProps 未指定");
		}
		//
    	initTotalJoins(joins);
	}

	/**
	 * 有效部分
	 * */
	private List<Join> paths=new ArrayList<>();
	/**
	 * 反向反转的部分
	 * */
	private List<Join> reverseIgnors=new ArrayList<>();
	
	private void push(Join join) {
		paths.add(join);
		reverseIgnors.add(join.getRevertJoin());
	}
	
	private void pop() {
		paths.remove(paths.size()-1);
		reverseIgnors.remove(reverseIgnors.size()-1);
	}
	
	private boolean isInStack(Join join) {
		return paths.contains(join) || reverseIgnors.contains(join.getRevertJoin());
	}
	

	public List<Join> find() {
		//寻找关联源
		List<Join> sourceJoin=findSourceJoin();
		if(sourceJoin.size()==0) {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() +" 未发现匹配的 Join 关系，请使用 using 方法指定使用的关联字段");
		}

		Join matchdJoin=null;
		for (Join join : sourceJoin) {
			//判断是否已经找到
			if(join.getTargetTable().equalsIgnoreCase(this.targetTable.name())) {
				if(this.routeTables!=null && this.routeTables.size()>1) {
					if(2==this.routeTables.size()) {
						matchdJoin=join;
					}
				}
			}
		}

		if(matchdJoin!=null) {
			 this.push(matchdJoin);
			return paths;
		}

		
		//先序遍历 Join 图
		boolean matched=false;
		for (Join join : sourceJoin) {
			this.push(join);
			matched=findNextJoin(2, join);
			if(matched) break;
		}
		
		if(matched) {
			Collections.reverse(paths);
			return paths;
		} else {
			throw new IllegalArgumentException(prop.getSourcePoType().getSimpleName()+"."+ prop.getProperty() +" 未发现 Join 关系");
		}
 
	}
	
	private boolean findNextJoin(int index, Join currJoin) {
		 
		DBTable routeTable=routeTable=this.routeTables.get(index);
		DBField[] routeFields=this.routeFields.get(routeTable);
 
		List<Join> sourceJoins=new ArrayList<>();
		for (Join join : allJoins) {
			//已经处理过的不再处理
			if(isInStack(join)) continue;
			if(!currJoin.getTargetTable().equalsIgnoreCase(join.getSourceTable()) ) continue;
			if(currJoin.getTargetFields().length != join.getSourceFields().length) continue;
			String[] sourceFields=join.getSourceFields();
			for (int i = 0; i < sourceFields.length  ; i++) {
				if(isTargetMatch(join,routeTable,routeFields)) {
					sourceJoins.add(join);
				}
			}
		}
		
		//未找到，弹出退栈
		if(sourceJoins.size()==0) {
			this.pop();
			return false; 
		}

		Join matchedJoin=null;
		for (Join join : sourceJoins) {
			//是否符合退出条件
			if(join.getTargetTable().equalsIgnoreCase(this.targetTable.name())) {
				if(this.routeTables!=null && this.routeTables.size()>1) {
					if((index+1)>=this.routeTables.size()) {
						matchedJoin=join;
					}
				}
			}
		}
 
		if(matchedJoin!=null) {
			this.push(matchedJoin);
			return true;
		}
		
		boolean matched=false;
		//继续先序查找
		for (Join join : sourceJoins) {
			this.push(join);
			matched=findNextJoin(index+1, join);
			if(matched) break;
		}
		
		return matched;
 
	}


	/**
	 * 查找第一个 Join 关系
	 * */
	private List<Join> findSourceJoin() {
		List<Join> sourceJoins=new ArrayList<>();
		JoinPoint usingJoinPoint=new JoinPoint(this.usingProps);
		for (Join join : allJoins) {
			if(join.getSourceJoinPoint().match(usingJoinPoint)) {
				sourceJoins.add(join);
			}
		}
		return sourceJoins;
	}

	private boolean isTargetMatch(Join join, DBTable routeTable, DBField[] routeFields) {
		
		if(routeTable!=null && !routeTable.name().equalsIgnoreCase(join.getTargetTable())) return false;
		
		if(routeFields!=null && routeFields.length>0) {
			if(routeFields.length!=join.getTargetFields().length) {
				throw new IllegalArgumentException("参数数量不一致");
			}
			for (int i = 0; i < join.getTargetFields().length; i++) {
				if(join.getTargetFields()[i].equals(routeFields[i])) {
					return false;
				}
			}	
		}
		return true;
	}

}
