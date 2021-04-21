package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.entity.CollectorUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.relation.PropertyRoute.DynamicValue;
import com.github.foxnic.dao.relation.PropertyRoute.OrderByInfo;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.GroupBy;
import com.github.foxnic.sql.expr.In;
import com.github.foxnic.sql.expr.OrderBy;
import com.github.foxnic.sql.expr.Select;
import com.github.foxnic.sql.expr.Where;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class RelationSolver {
	
	private static final ForkJoinPool JOIN_POOL= new ForkJoinPool();

    private static final String BR = "<$break.br/>";
	private DAO dao;
    private RelationManager relationManager;
 
    public RelationSolver(DAO dao)  {
        this.dao=dao;
        this.relationManager=dao.getRelationManager();
    }
    
    
    public <E extends Entity>  Map<String,JoinResult> join(Collection pos, Class... targetType) {
    	if(pos.isEmpty()) {
    		return new HashMap<>();
    	}
    	Map<String,JoinResult> result=null;
    	//同步执行
    	if(targetType.length==1 || pos.size()==1) {
	    	result=new HashMap<>();
			for (Class type : targetType) {
				Map<String,JoinResult> jr=this.join(pos,type);
				result.putAll(jr);
			}
    	} else {
	    	//异步执行
	    	PropertyTypeForkTask task = new PropertyTypeForkTask(dao.getDBTreaty().getLoginUserId(),this,pos,targetType);
	    	result = JOIN_POOL.invoke(task);
    	}
    	return result;
    }

 
    public <S extends Entity,T extends Entity> Map<String,JoinResult<S,T>> join(Collection<S> pos, Class<T> targetType) {
        if(pos==null || pos.isEmpty()) return null;
        Class<S> poType = getPoType(pos);
        List<PropertyRoute<S,T>> prs = this.relationManager.findProperties(poType,targetType);
        if(prs==null || prs.isEmpty()) return null;

        Map<String,JoinResult<S,T>> map=new HashMap<>();
        for (PropertyRoute<S,T> route : prs) {
        	JoinResult<S,T> jr=this.join(poType,pos,route,targetType);
        	map.put(route.getProperty(), jr);
		}
        return map;
    }




	@SuppressWarnings("unchecked")
	private <S extends Entity> Class<S> getPoType(Collection<S> pos) {
		S sample=null;
        for (S e:pos) {
            if(e!=null) sample=e;
        }
        if(sample==null) return null;
        Class<S> poType=(Class<S>)sample.getClass();
		return poType;
	}
	
	
	
	
	
	private <S extends Entity,T extends Entity> JoinResult<S,T> join(Class<S> poType, Collection<S> pos,PropertyRoute<S,T> route,Class<T> targetType) {
		
		if(route.getFork()<=0 || route.getFork()>pos.size()) {
			return joinInFork(poType, pos, route, targetType);
		} else {
			Object loginUserId=JoinForkTask.getThreadLoginUserId();
			if(loginUserId==null) {
				loginUserId=dao.getDBTreaty().getLoginUserId();
			}
			RelationForkTask<S,T> recursiveTask = new RelationForkTask<>(loginUserId,this,poType, pos, targetType,route,Logger.getTID());
			JoinResult<S,T> result = JOIN_POOL.invoke(recursiveTask);
			return result;
		}
 
	}
 
    
	<S extends Entity,T extends Entity> JoinResult<S,T> joinInFork(Class<S> poType, Collection<S> pos,PropertyRoute<S,T> route,Class<T> targetType) {
 
		if(route.isIgnoreJoin()) {
			return null;
		}
		//获得字段注解
//		Field field=ReflectUtil.getField(poType, route.getProperty());
//		com.github.foxnic.dao.relation.annotations.Join joinAnn=field.getAnnotation(com.github.foxnic.dao.relation.annotations.Join.class);
		String groupFor=route.getGroupFor();
//		if(joinAnn!=null) {
//			groupFor=joinAnn.groupFor();
//		}
		
		//返回的结果
		JoinResult<S,T> jr=new JoinResult<>();
		jr.setSourceList(pos);
		jr.setPropertyRoute(route);
		jr.setSourceType(targetType);
		jr.setTargetType(targetType);
 
		if(pos==null || pos.size()==0) {
			return jr;
		}
		
		//获得源表与目标表
		DBTable poTable=route.getSourceTable();
		DBTable targetTable=route.getTargetTable();
		//
		jr.setSourceTable(poTable.name());
		jr.setTargetTable(targetTable.name());
		
		
		//用于关联的数据字段属性清单
		DBField[] usingProps=route.getUsingProperties();
		//校验关联字段
		if(usingProps==null || usingProps.length==0) {
			usingProps = getDefaultUsingProps(route, poTable, usingProps);
		}
		
		//最后一个 Join 的 target 与 targetTable 一致
		List<Join> joinPath = this.dao.getRelationManager().findJoinPath(route,poTable,targetTable,usingProps,route.getRouteTables(),route.getRouteFields());
		printJoinPath(route,poTable,joinPath,targetTable);
		jr.setJoinPath(joinPath.toArray(new Join[0]));
		
		if(joinPath.isEmpty()) {
			throw new RuntimeException("未配置关联关系");
		}
		
		Join firstJoin=joinPath.get(0);
		Join lastJoin=joinPath.remove(joinPath.size()-1);
		
		//检查重复表
		Boolean hasRepeatTable=null;
		Set<String> tables=new HashSet<>(); 
		for (int i = joinPath.size()-1; i >=0; i--) {
			Join join=joinPath.get(i);
			if(i == joinPath.size()-1) {
				hasRepeatTable=!tables.add(join.getSourceTable());
				if(hasRepeatTable) break;
			}
			hasRepeatTable=!tables.add(join.getTargetTable());
			if(hasRepeatTable) break;
		}
		if(hasRepeatTable==null) hasRepeatTable=false;
		
		if(hasRepeatTable && route.getTableConditions().size()>0) {
			throw new RuntimeException("数据表重复的情况下，无法应用查询条件,请使用 sql 方法指定查询语句");
		}
		
		if(hasRepeatTable && route.getOrderByInfos().size()>0) {
			throw new RuntimeException("数据表重复的情况下，无法应用排序,请使用 sql 方法指定查询语句");
		}
		
 
		int i=0;
		
		String sourceAliasName=null;
		String targetAliasName="t_"+i;
		Select select=new Select();
		
		Map<String,String> alias=new HashMap<>();
		// 确定基表是否使用子查询，并设置
		Expr subQuery=null;
		List<ConditionExpr>  cdrs=route.getTableConditions(firstJoin.getTargetTable());
		Map<String,DynamicValue> dycdrs=route.getDynamicConditions(firstJoin.getTargetTable());
		if((cdrs==null || cdrs.isEmpty()) && (dycdrs==null || dycdrs.isEmpty())) {
			subQuery=new Expr(firstJoin.getTargetTable());
		} else {
			// 为子查询附加条件
			subQuery=new Expr("(select * from "+firstJoin.getTargetTable());
			Where wh=new Where();
			if(cdrs!=null) {
				for (ConditionExpr ce : cdrs) {
					wh.and(ce);
				}
			}
			if(dycdrs!=null) {
				for (Entry<String,DynamicValue> e : dycdrs.entrySet()) {
					wh.andIf(e.getKey()+" = ?", getDynamicValue(dao,e.getValue()));
				}
			}
			subQuery.append(wh);
			subQuery.append(")");
		}
		
		
		select.from(subQuery,targetAliasName);
		if(StringUtil.isBlank(groupFor)) {
			select.select(targetAliasName+".*");
		} else {
			select.select(groupFor,"gfor");
		}
		//搜集别名
		alias.put(firstJoin.getTargetTable(), targetAliasName);
		
		
		//拼接 Join 语句
		Expr expr=new Expr();
		for (Join join : joinPath) {
			i++;
			sourceAliasName="t_"+i;
			cdrs=route.getTableConditions(join.getSourceTable());
			dycdrs=route.getDynamicConditions(join.getTargetTable());
			// 确定是否使用子查询
			if(cdrs.isEmpty()) {
				subQuery=new Expr(join.getSourceTable());
			} else {
				// 为子查询附加条件
				subQuery=new Expr("(select * from "+join.getSourceTable());
				Where wh=new Where();
				if(cdrs!=null) {
					for (ConditionExpr ce : cdrs) {
						wh.and(ce);
					}
				}
				if(dycdrs!=null) {
					for (Entry<String,DynamicValue> e : dycdrs.entrySet()) {
						wh.andIf(e.getKey()+" = ?", getDynamicValue(dao,e.getValue()));
					}
				}
				subQuery.append(wh);
				subQuery.append(")");
			}
			//join 一个表
			Expr joinExpr=new Expr(BR+join.getJoinType().getJoinSQL()+" "+subQuery.getListParameterSQL()+" "+sourceAliasName+" on ",subQuery.getListParameters());
			//循环拼接 Join 的条件
			List<String> joinConditions=new ArrayList<>();
			for (int j = 0; j < join.getSourceFields().length; j++) {
				String cdr=sourceAliasName+"."+join.getSourceFields()[j]+" = "+targetAliasName+"."+join.getTargetFields()[j];
				joinConditions.add(cdr);
			}
			 
			//搜集别名
			alias.put(join.getSourceTable(), sourceAliasName);
			alias.put(join.getTargetTable(), targetAliasName);
			
			joinExpr.append(StringUtil.join(joinConditions," and "));
			expr.append(joinExpr);
			targetAliasName="t_"+i;
		}
		
		//设置用于分组关联的字段
		ArrayList<String> groupByFields=new ArrayList<>();
		i=0;
		String[] groupFields=new String[lastJoin.getTargetFields().length];
		for (String f : lastJoin.getTargetFields()) {
			groupFields[i]="join_f"+i;
			select.select(targetAliasName+"."+f,groupFields[i]);
			groupByFields.add(targetAliasName+"."+f);
			i++;
		}
		String[] grpFields=route.getGroupFields();
		final String[] catalogFields=new String[grpFields.length];
		
		if(grpFields!=null) {
			i=0;
			for (String f : grpFields) {
				catalogFields[i]="join_c"+i;
				select.select(targetAliasName+"."+f, catalogFields[i]);
				i++;
			}
			
		}
		
		//Select 语句转 Expr
		Expr selcctExpr=new Expr(select.getListParameterSQL(),select.getListParameters());
		expr=selcctExpr.append(expr);
		
 
		In in=null;
		// 单字段的In语句
		if(usingProps.length==1) {
//		if(lastJoin.getTargetFields().length==1) {
//			Object[] values=BeanUtil.getFieldValueArray(pos, lastJoin.getTargetFields()[0], Object.class);
			Object[] values=BeanUtil.getFieldValueArray(pos,usingProps[0].name(), Object.class);
			in=new In(alias.get(lastJoin.getTargetTable())+"."+lastJoin.getTargetFields()[0], values);
		} else {
			//多字段的In语句
		}
				
		Where wh=new Where();
		wh.and(in);
		expr.append(BR);
		expr.append(wh);
		
		expr=new Expr(expr.getListParameterSQL().replace(BR, "\n"),expr.getListParameters());
		
		
		if(!StringUtil.isBlank(groupFor)) {
			GroupBy groupBy=new GroupBy();
			groupBy.bys(groupByFields.toArray(new String[] {}));
			if(grpFields!=null) {
				for (String f : grpFields) {
					groupBy.bys(targetAliasName+"."+f);
				}
				i++;
			}
			expr.append(groupBy);
		}
		
		//构建并附加排序
		List<OrderByInfo> orderByInfos=route.getOrderByInfos();
		
		@SuppressWarnings("rawtypes")
		OrderBy orderBy=null;
		
		String aliasName=null;
		for (OrderByInfo info : orderByInfos) {
			aliasName=alias.get(info.getTableName());
			if(info.isAsc() && info.isNullsLast()) {
				orderBy=OrderBy.byAscNullsLast(aliasName+"."+info.getField());
			} else if(! info.isAsc() && info.isNullsLast()) {
				orderBy=OrderBy.byDescNullsLast(aliasName+"."+info.getField());
			} else if(info.isAsc() && !info.isNullsLast()) {
				orderBy=OrderBy.byAsc(aliasName+"."+info.getField());
			} else if(!info.isAsc() && !info.isNullsLast()) {
				orderBy=OrderBy.byDesc(aliasName+"."+info.getField());
			}
		}
		expr=expr.append(orderBy);
		jr.addStatement(expr);
 
		RcdSet targets=dao.query(expr);
		jr.setTargetRecords(targets);
		
		//记录集分组
		String[] keyParts=new String[groupFields.length];
		Map<Object,List<Rcd>> gs=CollectorUtil.groupBy(targets.getRcdList(), r -> {
			for (int j = 0; j < keyParts.length; j++) {
				String f = groupFields[j];
				keyParts[j]=r.getString(f);
			}
			return StringUtil.join(keyParts);
		});
		
		List<T> allTargets=new ArrayList<>();
		//填充关联数据
		pos.forEach(p->{
			for (int j = 0; j < keyParts.length; j++) {
				String f = lastJoin.getSourceFields()[j];
				keyParts[j]=BeanUtil.getFieldValue(p,f,String.class);
			}
			List<Rcd> tcds=gs.get(StringUtil.join(keyParts));
		
 			@SuppressWarnings("rawtypes")
			List list=new ArrayList();
			if(tcds!=null) {
				if(Catalog.class.equals(route.getType())) {
					Catalog cata=new Catalog();
					for (Rcd r : tcds) {
						String[] key=new String[grpFields.length];
						int j=0;
						for (String f : catalogFields) {
							key[j]=r.getString(f);
							j++;
						}
						cata.put(StringUtil.join(key,"_"), r.getDouble("gfor"));
					}
					list.add(cata);
				} else {
					for (Rcd r : tcds) {
						//如果属性类型是实体
						if(ReflectUtil.isSubType(Entity.class, route.getType())) {
							list.add(r.toEntity(targetType));
						}
						//如果属性类型是非实体
						else {
							list.add(r.getValue("gfor"));
						}
					}
				}
				//获取数据后的处理逻辑
				if(route.getAfter()!=null) {
					list=route.getAfter().process(p,list);
				}
				allTargets.addAll(list);
			}
			
			//区别是集合还是单个实体
			if(route.isMulti()) {
				BeanUtil.setFieldValue(p, route.getProperty(), list);
			} else {
				if(list!=null && !list.isEmpty()) { 
					BeanUtil.setFieldValue(p, route.getProperty(), list.get(0));
				}
			}
		});
 
		jr.setTargetList(allTargets);
		
		return jr;
	}


	/**
	 * 为 route 填充默认的 usingProps
	 * */
	private <S extends Entity, T extends Entity> DBField[] getDefaultUsingProps(PropertyRoute<S, T> route, DBTable poTable,
			DBField[] usingProps) {
		//用主键补齐
		List<DBColumnMeta> pks= dao.getTableMeta(poTable.name()).getPKColumns();
		if(pks.size()>0) {
			usingProps=new DBField[pks.size()];
			for (int i = 0; i < usingProps.length; i++) {
				usingProps[i]=poTable.getField(pks.get(i).getColumn());
			}
			route.using(usingProps);
		}
		if(usingProps==null || usingProps.length==0) {
			throw new RuntimeException(route.getSourcePoType().getName()+"."+route.getProperty()+" 未明确使用的数据字段 , 请使用 using 方法指定，请指定；或设置表 "+route.getSourceTable()+" 的主键为默认的数据字段");
		}
		return usingProps;
	}

 
	private Object getDynamicValue(DAO dao, DynamicValue value) {
		if(value==null) return null;
		if(value==DynamicValue.LOGIN_USER_ID) {
			return JoinForkTask.getThreadLoginUserId();
			//return dao.getDBTreaty().getLoginUserId();
		}
		return null;
	}


	private void printJoinPath(PropertyRoute route,DBTable sourceTable, List<Join> joinPath,DBTable targetTable) {
		
		List<Join> joinPathR=new ArrayList<>();
		joinPathR.addAll(joinPath);
		Collections.reverse(joinPathR);
		
		DBField[] usingProps=route.getUsingProperties();
		String type=(route.isMulti()?"List<":"")+route.getType().getSimpleName()+(route.isMulti()?">":"");
		String path=route.getSourcePoType().getSimpleName()+" :: "+type+" "+route.getProperty()+" , using : "+StringUtil.join(usingProps)+" , route "+sourceTable.name()+" to "+targetTable.name()+"\n";
		
		for (Join join : joinPathR) {
			path+="\t"+ join.getSourceTable()+"( "+StringUtil.join(join.getSourceFields())+" ) = "+ join.getTargetTable()+"( "+StringUtil.join(join.getTargetFields())+" )"+"\n";
		}
 
		System.err.println("\n"+path);
	}


	@SuppressWarnings("unchecked")
	public <S extends Entity,T extends Entity> Map<String, JoinResult<S,T>> join(Collection<S> pos, String[] properties) {
		if(pos==null || pos.isEmpty()) return null;
		Class<S> poType = getPoType(pos);
		
		Map<String,JoinResult<S,T>> map=new HashMap<>();
 
		for (String prop : properties) {
			PropertyRoute<S,T> pr=dao.getRelationManager().findProperties(poType,prop);
			if(pr==null) {
				throw new IllegalArgumentException(poType.getSimpleName()+"."+prop+" 未配置");
			}
			JoinResult jr=this.join(poType,pos,pr,pr.getTargetPoType());
			map.put(pr.getProperty(), jr);
		}
		return map;
	}

}
