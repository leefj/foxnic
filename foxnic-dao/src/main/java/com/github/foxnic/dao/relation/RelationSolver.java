package com.github.foxnic.dao.relation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.entity.CollectorUtil;
import com.github.foxnic.dao.entity.Entity;
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
    	
//    	同步执行
//    	Map<String,JoinResult> jrs=new HashMap<>();
//		for (Class type : targetType) {
//			Map<String,JoinResult> jr=this.join(pos.getList(),type);
//			jrs.putAll(jr);
//		}
    	//异步执行
    	PropertyTypeForkTask task = new PropertyTypeForkTask(this,pos,targetType);
    	Map<String,JoinResult> result = JOIN_POOL.invoke(task);
    	
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
			RelationForkTask<S,T> recursiveTask = new RelationForkTask<>(this,poType, pos, targetType,route,Logger.getTID());
			
			JoinResult<S,T> result = JOIN_POOL.invoke(recursiveTask);
			return result;
		}
 
	}
	
	

    
	<S extends Entity,T extends Entity> JoinResult<S,T> joinInFork(Class<S> poType, Collection<S> pos,PropertyRoute<S,T> route,Class<T> targetType) {
 
		//获得字段注解
		Field field=ReflectUtil.getField(poType, route.getProperty());
		com.github.foxnic.dao.relation.annotations.Join joinAnn=field.getAnnotation(com.github.foxnic.dao.relation.annotations.Join.class);
		String groupFor=null;
		if(joinAnn!=null) {
			groupFor=joinAnn.groupFor();
		}
		
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
		String poTable=EntityUtil.getAnnotationTable(poType);
		String targetTable=EntityUtil.getAnnotationTable(targetType);
		//
		jr.setSourceTable(poTable);
		jr.setTargetTable(targetTable);
		
		
		//用于关联的数据字段属性清单
		String[] usingProps=route.getUsingProperties();
		//校验关联字段
		if(usingProps==null || usingProps.length==0) {
			throw new RuntimeException(route.getPoType().getName()+"."+route.getProperty()+" 未明确使用的数据字段 , 请使用 using 方法指定");
		}
		
		//最后一个 Join 的 target 与 targetTable 一致
		List<Join> joinPath = this.dao.getRelationManager().findJoinPath(poTable,targetTable,usingProps);
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
		if(cdrs.isEmpty()) {
			subQuery=new Expr(firstJoin.getTargetTable());
		} else {
			// 为子查询附加条件
			subQuery=new Expr("(select * from "+firstJoin.getTargetTable());
			Where wh=new Where();
			for (ConditionExpr ce : cdrs) {
				wh.and(ce);
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
			// 确定是否使用子查询
			if(cdrs.isEmpty()) {
				subQuery=new Expr(join.getSourceTable());
			} else {
				// 为子查询附加条件
				subQuery=new Expr("(select * from "+join.getSourceTable());
				Where wh=new Where();
				for (ConditionExpr ce : cdrs) {
					wh.and(ce);
				}
				subQuery.append(wh);
				subQuery.append(")");
			}
			//join 一个表
			Expr joinExpr=new Expr(BR+join.getJoinType().getJoinSQL()+" "+subQuery.getListParameterSQL()+" "+sourceAliasName+" on ",subQuery.getListParameters());
			//循环拼接 Join 的条件
			List<String> joinConditions=new ArrayList<>();
			for (int j = 0; j < join.getSourceTableFields().size(); j++) {
				String cdr=sourceAliasName+"."+join.getSourceTableFields().get(j)+" = "+targetAliasName+"."+join.getTargetTableFields().get(j);
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
		String[] groupFields=new String[lastJoin.getTargetTableFields().size()];
		for (String f : lastJoin.getTargetTableFields()) {
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
		if(lastJoin.getTargetTableFields().size()==1) {
			Object[] values=BeanUtil.getFieldValueArray(pos, lastJoin.getSourceTableFields().get(0), Object.class);
			in=new In(lastJoin.getTargetTableFields().get(0), values);
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
				String f = lastJoin.getSourceTableFields().get(j);
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
