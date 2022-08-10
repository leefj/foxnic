package com.github.foxnic.dao.relation;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.FieldsBuilder;
import com.github.foxnic.dao.entity.QuerySQLBuilder;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.PropertyRoute.DynamicValue;
import com.github.foxnic.dao.relation.PropertyRoute.OrderByInfo;
import com.github.foxnic.dao.relation.cache.PropertyCacheManager;
import com.github.foxnic.dao.relation.cache.PreBuildResult;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;

public class RelationSolver {
	public static final String PK_JOIN_FS = "pk_join_fs_";
	public static final String JOIN_FS = "join_fs_";
	public static final String JOIN_FS_RV = "_sf_nioj_";

//	private static InheritableThreadLocal<Integer> JOIN_COUNT=new InheritableThreadLocal(){
//		@Override
//		protected Integer initialValue() {
//			return new Integer(0);
//		}
//	};
//
//	public static Integer getJoinCount(){
//		return JOIN_COUNT.get();
//	}


	private static final ForkJoinPool JOIN_POOL= new ForkJoinPool(8);

    public static final String BR = "<$break.br/>";
	private DAO dao;
    private RelationManager relationManager;

    public RelationSolver(DAO dao)  {
        this.dao=dao;
        this.relationManager=dao.getRelationManager();
    }


    public <E extends Entity>  Map<String,JoinResult> join(Collection<E> pos, Class... targetType) {
    	if(pos==null || pos.isEmpty()) {
    		return new HashMap<>();
    	}
    	Map<String,JoinResult> result=null;
    	//同步执行
    	if(targetType.length==1 || pos.size()==1) {
	    	result=new HashMap<>();
			for (Class type : targetType) {
				Map<String,JoinResult> jr=this.join(DAO.DEFAULT_JOIN_TAG,pos,type,null);
				if(jr!=null) {
					result.putAll(jr);
				}
			}
    	} else {
	    	//异步执行
	    	PropertyTypeForkTask task = new PropertyTypeForkTask(dao.getDBTreaty().getLoginUserId(),this,pos,targetType);
	    	result = JOIN_POOL.invoke(task);
		}
    	return result;
    }


    public <S extends Entity,T extends Entity> Map<String,JoinResult<S,T>> join(String tag,Collection<S> pos, Class<T> targetType,Map<String, FieldsBuilder> fieldsBuilderMap) {
        if(pos==null || pos.isEmpty()) return null;
        Class<S> poType = getPoType(pos);
        if(poType==null) return null;

        List<PropertyRoute<S,T>> prs = this.relationManager.findProperties(poType,targetType);
        if(prs==null || prs.isEmpty()) {
			Class sourcePoType=poType;
        	if(EntityContext.isProxyType(poType)) {
				sourcePoType=sourcePoType.getSuperclass();
			}
        	throw new RuntimeException(sourcePoType.getName()+" 到 "+targetType.getName()+" 的关联关系未配置");
		}

        Map<String,JoinResult<S,T>> map=new HashMap<>();
        for (PropertyRoute<S,T> route : prs) {
        	JoinResult<S,T> jr=this.join(tag,poType,pos,route,targetType,fieldsBuilderMap);
        	map.put(route.getProperty(), jr);
		}
        return map;
    }




	@SuppressWarnings("unchecked")
	private <S extends Entity> Class<S> getPoType(Collection<S> pos) {
    	if(pos==null) return null;
		S sample=null;
        for (S e:pos) {
            if(e!=null) sample=e;
        }
        if(sample==null) return null;
        Class<S> poType=(Class<S>)sample.getClass();
		return poType;
	}





	private <S extends Entity,T extends Entity> JoinResult<S,T> join(String tag,Class<S> poType, Collection<S> pos,PropertyRoute<S,T> route,Class<T> targetType,Map<String, FieldsBuilder> fieldsBuilderMap) {

		if(route.getFork()<=0 || route.getFork()>pos.size()) {
			//同步执行
			return joinInFork(tag,poType, pos, route, targetType,fieldsBuilderMap);
		} else {
			Object loginUserId=JoinForkTask.getThreadLoginUserId();
			if(loginUserId==null) {
				loginUserId=dao.getDBTreaty().getLoginUserId();
			}
			RelationForkTask<S,T> recursiveTask = new RelationForkTask<>(tag,loginUserId,this,poType, pos, targetType,route,fieldsBuilderMap,Logger.getTID());
			JoinResult<S,T> result = JOIN_POOL.invoke(recursiveTask);
			return result;
		}

	}




	<S extends Entity,T extends Entity> JoinResult<S,T> joinInFork(String tag,Class<S> poType, Collection<S> pos,PropertyRoute<S,T> route,Class<T> targetType,Map<String, FieldsBuilder> fieldsBuilderMap) {

		if(route.isIgnoreJoin()) {
			return null;
		}

		PropertyCacheManager cacheMetaManager = PropertyCacheManager.instance();


		PreBuildResult preBuildResult= cacheMetaManager.preBuild(tag,dao,pos,route);


		JoinResult<S,T> jr=new JoinResult<>();

		QueryBuildResult result=buildJoinStatement(jr,poType,pos,preBuildResult.getBuilds(),route,targetType,fieldsBuilderMap,true);
		if(result==null) return jr;

		Expr expr=result.getExpr();
		String[] groupFields=result.getGroupFields();
		Join lastJoin=result.getLastJoin();
		String[] grpFields=result.getGroupJoinFields();
		String[] catalogFields=result.getCatalogFields();

//		JoinCacheType cacheMode =result.getCacheType();
//		String cachedTargetPoPKField=result.getTargetTableSimplePrimaryField();
//		Map<Object,Object> cachedTargetPoMap=result.getCachedTargetPoMap();
//		RelationCacheSolver cacheSolver= result.getCacheSolver();


		RcdSet targets=null;
		if(expr!=null) {
			targets=dao.query(expr);
//			cacheSolver.saveToCache(targets);
//			cacheSolver.appendRecords(targets);
//			JOIN_COUNT.set(JOIN_COUNT.get()+1);
		}
		else {
//			targets=cacheSolver.buildRcdSet();
			jr.setTargetList((List)preBuildResult.getTargets());
			return jr;
		}

		jr.setTargetRecords(targets);


		//记录集分组
		String[] keyParts=new String[groupFields.length];
		Map<Object,List<Rcd>> gs= CollectorUtil.groupBy(targets.getRcdList(), r -> {
			for (int j = 0; j < keyParts.length; j++) {
				String f = groupFields[j];
				keyParts[j]=r.getString(f);
			}
			return StringUtil.join(keyParts);
		});



		List<T> allTargets=new ArrayList<>();
		//需要保存到缓存的数据
//		Map<Object, Object> entitiesToSave=new HashMap<>();
//		Map<Object, Object> recordsToSave=new HashMap<>();

		//填充关联数据
		pos.forEach(p->{
			if(p==null) return;
			if(preBuildResult.getBuilds().contains(p)) return;
			String propertyKey=null;
//			String name=BeanUtil.getFieldValue(p,"label",String.class);
//			if("账户管理".equals(name)) {
//				System.out.println();
//			}
			for (int j = 0; j < keyParts.length; j++) {
				String f = lastJoin.getMasterFields()[j].name();
				keyParts[j]=BeanUtil.getFieldValue(p,f,String.class);
			}
			propertyKey=StringUtil.join(keyParts);
			List<Rcd> rcds=gs.get(propertyKey);

//			String pk=null;
//			DBTableMeta tm=dao.getTableMeta(route.getTargetTable().name());
//			if(tm.getPKColumnCount()==1) {
//				pk=tm.getPKColumns().get(0).getColumn();
//			}
//			DBTableMeta tm=dao.getTableMeta(route.getTargetTable().name());

 			@SuppressWarnings("rawtypes")
			List list=new ArrayList();
 			Map<Object, ExprRcd> map=new HashMap<>();


 			Entity entity=null;
			if(rcds!=null) {
				if(Catalog.class.equals(route.getType())) {
					Catalog cata=new Catalog();
					for (Rcd r : rcds) {
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
					JSONObject rcd=null;
					for (Rcd r : rcds) {

						//如果属性类型是实体
						if(ReflectUtil.isSubType(Entity.class, route.getType())) {
							if(route.getGroupFor()==null) {
								if(r==null){
									System.out.printf("");
								}
								entity=r.toEntity(targetType);
								if(entity==null){
									System.out.printf("");
									if(r!=null && !r.getFields().isEmpty()) {
										entity=r.toEntity(targetType);
									}
								}
//								rcd=r.toJSONObject();
								list.add(entity);
								map.put(entity,r);

							} else {
								list.add(r.getValue("gfor"));
							}
						}
						//如果属性类型是非实体
						else {
							list.add(r.getValue("gfor"));
						}
					}
				}
				//获取数据后的处理逻辑
				if(route.getAfter()!=null) {
					try {
						list=route.getAfter().process(tag,p,list,map);
					} catch (Exception e) {
						 throw new RuntimeException(route.getMasterPoType().getName()+"."+route.getProperty()+" 的 after 方法异常",e);
					}
				}
				allTargets.addAll(list);
			}

			//区别是集合还是单个实体
			if(route.isList()) {
				BeanUtil.setFieldValue(p, route.getProperty(), list);
//				entitiesToSave.put(propertyKey,list);
//				recordsToSave.put(propertyKey,map.values());
			} else {
				if(list!=null && !list.isEmpty()) {
					BeanUtil.setFieldValue(p, route.getProperty(), list.get(0));
//					entitiesToSave.put(propertyKey,list.get(0));
//					if(result.getCacheType()==JoinCacheType.SINGLE_PRIMARY_KEY) {
//						recordsToSave.put(propertyKey, map.get(list.get(0)));
//					} else if(result.getCacheType()==JoinCacheType.SINGLE_PRIMARY_KEY) {
//						recordsToSave.put(propertyKey, map.values());
//					}
				}
			}
			cacheMetaManager.save(dao,p,route,list,rcds);

			// 缓存后再设置 owner
			for (Object e : list) {
				if(e instanceof  Entity) {
					BeanUtil.setFieldValue(e,"$owner",p);
				}
			}

		});


		allTargets.addAll((List)preBuildResult.getTargets());

		jr.setTargetList(allTargets);

		return jr;
	}

	public <S extends Entity,T extends Entity> QueryBuildResult buildJoinStatement(JoinResult<S,T> jr, Class<S> poType, Collection<S> pos, Collection<? extends Entity> built, PropertyRoute<S,T> route, Class<T> targetType, Map<String, FieldsBuilder> fieldsBuilderMap, boolean forJoin) {

		String groupFor=route.getGroupFor();

		//返回的结果
		QueryBuildResult result=new QueryBuildResult();
		result.setForJoin(forJoin);

		jr.setSourceList(pos);
		jr.setPropertyRoute(route);
		jr.setSourceType(targetType);
		jr.setTargetType(targetType);

		if(forJoin) {
			if (pos == null || pos.size() == 0) {
				return null;
			}
		} else {
			System.out.println();
		}

		//获得源表与目标表
		DBTable poTable=route.getMasterTable();
		DBTable targetTable=route.getSlaveTable();
		//
		jr.setSourceTable(poTable.name());
		jr.setTargetTable(targetTable.name());

		//用于关联的数据字段属性清单
		DBField[] usingProps=route.getUsingProperties();

		//最后一个 Join 的 target 与 targetTable 一致
		List<Join> joinPath = route.getJoins();// this.dao.getRelationManager().findJoinPath(route,poTable,targetTable,usingProps);
//		printJoinPath(route,poTable,joinPath,targetTable,forJoin);
		jr.setJoinPath(joinPath.toArray(new Join[0]));

		if(joinPath.isEmpty()) {
			throw new RuntimeException("未配置关联关系");
		}

		Join firstJoin=joinPath.get(0);
		Join lastJoin=joinPath.remove(joinPath.size()-1);

		int i=0;

		String sourceAliasName=null;
		String targetAliasName="t_"+i;
		Select select=new Select();
		if(forJoin && route.isDistinct()) {
			select.distinct();
		}


		Map<String,String> alias=new HashMap<>();
		// 确定基表是否使用子查询，并设置
		Expr subQuery=null;
		List<ConditionExpr> ces=firstJoin.getSlavePoint().getConditions();
		ces=appendTreatyCondition(firstJoin.getSlaveTable(),ces);
		Map<String,DynamicValue> dyces=route.getDynamicConditions(firstJoin);

		FieldsBuilder firstFieldsBuilder=null;
		if(fieldsBuilderMap!=null) {
			firstFieldsBuilder=fieldsBuilderMap.get(firstJoin.getSlaveTable().toLowerCase());
		}

		if((ces==null || ces.isEmpty()) && (dyces==null || dyces.isEmpty())) {
			subQuery=new Expr(firstJoin.getSlaveTable());
		} else {
			String fieldsSQL="*";
			if(firstFieldsBuilder!=null) {
				fieldsSQL=firstFieldsBuilder.getFieldsSQL();
			}
			// 为子查询附加条件
			subQuery=new Expr("(select "+fieldsSQL+" from "+firstJoin.getSlaveTable());
			Where wh=new Where();
			if(ces!=null) {
				for (ConditionExpr ce : ces) {
					wh.and(ce);
				}
			}
			if(dyces!=null) {
				for (Entry<String,DynamicValue> e : dyces.entrySet()) {
					wh.andIf(e.getKey()+" = ?", getDynamicValue(dao,e.getValue()));
				}
			}
			subQuery.append(wh);
			subQuery.append(")");
		}


		select.from(subQuery,targetAliasName);
		if(StringUtil.isBlank(groupFor)) {
			if(!forJoin || route.getFields()==null || route.getFields().length==0) {
				String fieldsSQL=targetAliasName+".*";
				if(firstFieldsBuilder!=null) {
					fieldsSQL=firstFieldsBuilder.getFieldsSQL(targetAliasName);
				}
				select.select(fieldsSQL);
			} else {
				for (DBField field : route.getFields()) {
					select.select(targetAliasName + "."+field.name());
				}
			}
		} else {
			select.select(groupFor,"gfor");
		}
		//搜集别名
		alias.put(firstJoin.getSlaveTable(), targetAliasName);


		//拼接 Join 语句
		Expr expr=new Expr();
		for (Join join : joinPath) {
			i++;
			sourceAliasName="t_"+i;
			ces=join.getMasterPoint().getConditions();
			ces=appendTreatyCondition(join.getMasterTable(),ces);
			dyces=route.getDynamicConditions(join);
			// 确定是否使用子查询
			if((ces==null || ces.isEmpty()) && (dyces==null || dyces.isEmpty())) {
				subQuery=new Expr(join.getMasterTable());
			} else {
				// 为子查询附加条件
				subQuery=new Expr("(select * from "+join.getMasterTable());
				Where wh=new Where();
				if(ces!=null) {
					for (ConditionExpr ce : ces) {
						wh.and(ce);
					}
				}
				if(dyces!=null) {
					for (Entry<String,DynamicValue> e : dyces.entrySet()) {
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
			for (int j = 0; j < join.getMasterFields().length; j++) {
				String cdr=sourceAliasName+"."+join.getMasterFields()[j]+" = "+targetAliasName+"."+join.getSlaveFields()[j];
				joinConditions.add(cdr);
			}

			//搜集别名
			alias.put(join.getMasterTable(), sourceAliasName);
			alias.put(join.getSlaveTable(), targetAliasName);

			joinExpr.append(StringUtil.join(joinConditions," and "));
			expr.append(joinExpr);
			targetAliasName="t_"+i;
		}

		//设置用于分组关联的字段
		ArrayList<String> groupByFields=new ArrayList<>();
		i=0;
		String[] groupFields=new String[lastJoin.getSlaveFields().length];
		String[] groupColumn=new String[lastJoin.getSlaveFields().length];
		for (DBField f : lastJoin.getSlaveFields()) {
			groupFields[i]="join_f"+i;
			groupColumn[i]=f.name();
			select.select(targetAliasName+"."+f,groupFields[i]);
			groupByFields.add(targetAliasName+"."+f);
			i++;
		}
		String[] groupJoinFields=route.getGroupFields();
		final String[] catalogFields=new String[groupJoinFields.length];

		if(groupJoinFields!=null) {
			i=0;
			for (String f : groupJoinFields) {
				catalogFields[i]="join_c"+i;
				select.select(targetAliasName+"."+f, catalogFields[i]);
				i++;
			}
		}


		List<JoinPoint.SelelctFieldPair> fs=lastJoin.getSlavePoint().getSelectFields();
		for (JoinPoint.SelelctFieldPair f : fs) {
			select.select(alias.get(lastJoin.getSlaveTable())+"."+f.getField().name(), f.getAlias());
		}

		for (Join join : joinPath) {
			if(join==firstJoin) continue;
			fs=join.getSlavePoint().getSelectFields();
			if(fs.isEmpty()) continue;
			for (JoinPoint.SelelctFieldPair f : fs) {
				select.select(alias.get(join.getSlaveTable())+"."+f.getField().name(), f.getAlias());
			}
		}

//		RelationCacheSolver cacheSolver=new RelationCacheSolver(result,dao,route,forJoin);
//		result.setCacheSolver(cacheSolver);

		// 加入参与 join 的字段
		List<Join> joins = route.getJoins();
		String aliasName=null;
		for (Join join : joins) {
			//
			DBField[]  fields=join.getSlaveFields();
			aliasName=alias.get(join.getSlaveTable());
			for (DBField field : fields) {
				select.select(aliasName+"."+field.name(), JOIN_FS+join.getSlaveTable()+JOIN_FS_RV+field.name());
			}

			DBTableMeta tm=dao.getTableMeta(join.getSlaveTable());
			List<DBColumnMeta> pks=tm.getPKColumns();
			for (DBColumnMeta pk : pks) {
				select.select(aliasName+"."+pk.getColumn(), PK_JOIN_FS+join.getSlaveTable()+JOIN_FS_RV+pk.getColumn());
			}

			//
			fields=join.getMasterFields();
			aliasName=alias.get(join.getMasterTable());
			if(!StringUtil.isBlank(aliasName)) {
				for (DBField field : fields) {
					select.select(aliasName + "." + field.name(), JOIN_FS + join.getMasterTable() + JOIN_FS_RV + field.name());
				}
			}



		}


		//Select 语句转 Expr
		Expr selcctExpr=new Expr(select.getListParameterSQL(),select.getListParameters());
		expr=selcctExpr.append(expr);
		Map<Object,Object> cachedTargetPo=null;
		boolean hasIns=true;
		int elsCount=-1;
		if(forJoin) {
			In in = null;
			// 排除已经 build 的那些实体
			Collection<S> forIdsPos=new ArrayList();
			forIdsPos.addAll(pos);
			forIdsPos.removeAll(built);
			// 单字段的In语句
			if (usingProps.length == 1) {
				Set<Object> values = BeanUtil.getFieldValueSet(forIdsPos, usingProps[0].name(), Object.class,false);
//				cacheSolver.handleForIn(groupFields,groupColumn,values);
				in = new In(alias.get(lastJoin.getSlaveTable()) + "." + lastJoin.getSlaveFields()[0], values);
				elsCount=values.size();
			} else {
				List<Object[]> values=new ArrayList<>();
				for (S po : forIdsPos) {
					Object[] item=new Object[usingProps.length];
					for (int j = 0; j < usingProps.length; j++) {
						item[j]=BeanUtil.getFieldValue(po,usingProps[j].name());
					}
					values.add(item);
				}
				in=new In(lastJoin.getSlaveFields(),values);
				elsCount=values.size();
			}
			if(in==null || in.isEmpty()) {
				hasIns=false;
			}
			Where wh=new Where();
			wh.and(in);

			DBTableMeta tm=dao.getTableMeta(lastJoin.getSlaveTable());

			//加入逻辑删除条件
			DBColumnMeta delColumn=tm.getColumn(dao.getDBTreaty().getDeletedField());
			if(delColumn!=null) {
				wh.and(alias.get(lastJoin.getSlaveTable())+"."+delColumn.getColumn()+"=?",dao.getDBTreaty().getFalseValue());
			}
			//加入租户条件
			DBColumnMeta tenantColumn=tm.getColumn(dao.getDBTreaty().getTenantIdField());
			Object tenantId=dao.getDBTreaty().getActivedTenantId();
			if(tenantColumn!=null && tenantId!=null) {
				wh.and(alias.get(lastJoin.getSlaveTable())+"."+tenantColumn.getColumn()+"=?",tenantId);
			}

			expr.append(BR);
			expr.append(wh);

		}

		expr=new Expr(expr.getListParameterSQL().replace(BR, "\n"),expr.getListParameters());

		if(!StringUtil.isBlank(groupFor)) {
			GroupBy groupBy=new GroupBy();
			groupBy.bys(groupByFields.toArray(new String[] {}));
			if(groupJoinFields!=null) {
				for (String f : groupJoinFields) {
					groupBy.bys(targetAliasName+"."+f);
				}
				i++;
			}
			expr.append(groupBy);
		}

		//构建并附加排序
		List<OrderByInfo> orderByInfos=route.getOrderByInfos();

		if(forJoin) {
			@SuppressWarnings("rawtypes")
			OrderBy orderBy = null;

			aliasName = null;
			for (OrderByInfo info : orderByInfos) {
				aliasName = alias.get(info.getTableName());
				if (info.isAsc() && info.isNullsLast()) {
					orderBy = OrderBy.byAscNullsLast(aliasName + "." + info.getField());
				} else if (!info.isAsc() && info.isNullsLast()) {
					orderBy = OrderBy.byDescNullsLast(aliasName + "." + info.getField());
				} else if (info.isAsc() && !info.isNullsLast()) {
					orderBy = OrderBy.byAsc(aliasName + "." + info.getField());
				} else if (!info.isAsc() && !info.isNullsLast()) {
					orderBy = OrderBy.byDesc(aliasName + "." + info.getField());
				}
			}
			expr = expr.append(orderBy);
		}

		jr.addStatement(expr);

//		result.put("expr",expr);
//		result.put("groupFields",groupFields);
//		result.put("lastJoin",lastJoin);
//		result.put("grpFields",groupJoinFields);
//		result.put("catalogFields",catalogFields);
//		result.put("tableAlias",alias);
		if(!hasIns) {
			expr = null;
		} else {
			printJoinPath(route,poTable,joinPath,targetTable,forJoin,elsCount);
		}


		result.setExpr(expr);
		result.setGroupFields(groupFields);
		result.setLastJoin(lastJoin);
		result.setGroupJoinFields(groupJoinFields);
		result.setCatalogFields(catalogFields);
		result.setTableAlias(alias);

		return result;

	}

	private List<ConditionExpr> appendTreatyCondition(String table,List<ConditionExpr> ces) {
		DBTableMeta tm=dao.getTableMeta(table);
    	String deletedField=dao.getDBTreaty().getDeletedField();
		if(tm.isColumnExists(deletedField)) {
			ces.add(new ConditionExpr(deletedField+"=?",dao.getDBTreaty().getFalseValue()));
		}
		String tenantField=dao.getDBTreaty().getTenantIdField();
		Object tenantId=dao.getDBTreaty().getActivedTenantId();
		if(tm.isColumnExists(tenantField) && tenantId!=null) {
			ces.add(new ConditionExpr(tenantField+"=?",tenantId));
		}
		return  ces;
	}




	public static Object getDynamicValue(DAO dao, DynamicValue value) {
		if(value==null) return null;
		if(value==DynamicValue.LOGIN_USER_ID) {
			return JoinForkTask.getThreadLoginUserId();
			//return dao.getDBTreaty().getLoginUserId();
		}
		return null;
	}


	private void printJoinPath(PropertyRoute route,DBTable sourceTable, List<Join> joinPath,DBTable targetTable,boolean forJoin,int elsCount) {

		if(!this.dao.isPrintSQL()) {
			return;
		}

		List<Join> joinPathR=new ArrayList<>();
		joinPathR.addAll(joinPath);
		Collections.reverse(joinPathR);
		String thread=Thread.currentThread().getId()+"";
		DBField[] usingProps=route.getUsingProperties();
		String type=(route.isList()?"List<":"")+route.getType().getSimpleName()+(route.isList()?">":"");
		String path="JOIN("+(forJoin?"DATA":"SEARCH")+") FORK("+thread+"):"+route.getFork()+" , el="+elsCount+" >>> \n"+route.getMasterPoType().getSimpleName()+" :: "+type+" "+route.getProperty()+" , properties : "+StringUtil.join(usingProps)+" , route "+sourceTable.name()+" to "+targetTable.name()+"\n";

		for (Join join : joinPathR) {
			List<String> conditions=new ArrayList<>();
			for (ConditionExpr condition : join.getSlavePoint().getConditions()) {
				conditions.add(condition.getSQL());
			}
			path+="\t"+ join.getMasterTable()+"( "+StringUtil.join(join.getMasterFields())+" ) = "+ join.getSlaveTable()+"( "+StringUtil.join(join.getSlaveFields())+" )" +(conditions.isEmpty()?"":" , conditions : "+StringUtil.join(conditions," and ").trim())+"\n";
		}

		System.err.println("\n"+path);
	}

	public <S extends Entity,T extends Entity> JoinResult<S,T> join(String tag,Collection<S> pos, String property) {
		return join(tag,pos,null,property);
	}

	public <S extends Entity,T extends Entity> JoinResult<S,T> join(String tag, Collection<S> pos, Map<String, FieldsBuilder> fieldsBuilderMap , String property) {
		if(pos==null || pos.isEmpty()) return null;
		Class<S> poType = getPoType(pos);
		if(poType==null) {
			return null;
		}
		PropertyRoute<S, T> pr=dao.getRelationManager().findProperties(poType,property);
		if(pr==null) {
			Class realPoType=EntityContext.getPoType(poType);
			IllegalArgumentException exp=new IllegalArgumentException(realPoType.getName()+"."+property+" 关联关系未配置");
			Logger.exception(exp);
			throw exp;
		}
		JoinResult jr=this.join(tag,poType,pos,pr,pr.getSlavePoType(),fieldsBuilderMap);
		return jr;
	}

	public <E extends Entity,T extends Entity> Map<String,JoinResult> join(String tag,Collection<E> pos, String[] properties) {
		return join(tag,pos,null,properties);
	}

	@SuppressWarnings("unchecked")
	public <E extends Entity,T extends Entity> Map<String,JoinResult> join(String tag, Collection<E> pos, Map<String, FieldsBuilder> fieldsBuilderMap, String[] properties) {
		if(pos==null || pos.isEmpty()) return null;
		Map<String,JoinResult> map=new HashMap<>();
		//如果只有一个属性
		if(properties.length==1) {
			JoinResult result=this.join(tag,pos,fieldsBuilderMap,properties[0]);
			map.put(properties[0],result);
			return  map;
		}

		//如果多个属性
//		PropertyNameForkTask task=new PropertyNameForkTask();
		PropertyNameForkTask task = new PropertyNameForkTask(tag,dao.getDBTreaty().getLoginUserId(),this,pos,properties,fieldsBuilderMap);
		map = JOIN_POOL.invoke(task);
//		Object rrr=JOIN_POOL.submit(task);
//		JOIN_POOL.execute(task);
		return  map;

//		for (String prop : properties) {
//			PropertyRoute<S,T> pr=dao.getRelationManager().findProperties(poType,prop);
//			if(pr==null) {
//				throw new IllegalArgumentException(poType.getSimpleName()+"."+prop+" 关联关系未配置");
//			}
//			JoinResult jr=this.join(poType,pos,pr,pr.getTargetPoType());
//			map.put(pr.getProperty(), jr);
//		}
//		return map;
	}

}
