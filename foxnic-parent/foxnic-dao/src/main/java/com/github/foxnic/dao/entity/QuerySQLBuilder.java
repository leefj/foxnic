package com.github.foxnic.dao.entity;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.api.model.CompositeItem;
import com.github.foxnic.api.model.CompositeParameter;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.Join;
import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.relation.RelationSolver;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class QuerySQLBuilder<E> {

    private SuperService service;

    QuerySQLBuilder(SuperService service) {
        this.service = service;
    }

    private CompositeParameter getSearchValue(E sample) {
        String searchValue = BeanUtil.getFieldValue(sample, "searchValue", String.class);
        if (searchValue != null) searchValue = searchValue.trim();
        CompositeParameter compositeParameter = new CompositeParameter(searchValue, BeanUtil.toMap(sample));
        return compositeParameter;
    }

    /**
     *  生成 Select 语句，join关系不变则表别名不变
     * */
    public Expr buildSelect(E sample, ConditionExpr customConditionExpr, OrderBy orderBy) {
        return buildSelect(sample,null,customConditionExpr,orderBy);
    }

    /**
     *  生成 Select 语句，join关系不变则表别名不变
     * */
    public Expr buildSelect(E sample, String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy) {

        List<RouteUnit> units = this.getSearchRoutes(sample);
        Set<String> handledKeys=new HashSet<>();

        String sourceAliasName;
        String targetAliasName;

        if(StringUtil.isBlank(tabAlias)) {
            tabAlias="t";
        }

        String firstTableAlias = tabAlias;
        Map<String, String> alias = new HashMap<>();
        alias.put(service.table().toLowerCase(), firstTableAlias);

        Set<String> joinedPoints=new HashSet<>();

        int aliasIndex = 0;
        Expr expr = new Expr("select " + firstTableAlias + ".* from " + this.service.table() + " " + firstTableAlias);
        //循环扩展的条件路由单元
        for (RouteUnit unit : units) {
            //获得关联条件
            List<Join> joins = unit.route.getJoins();
            //标记 key 已被处理，跳过在常规字段处理逻辑
            handledKeys.add(unit.item.getKey());
            for (Join join : joins) {
                //如果重复，那么加入查询条件
                if(joinedPoints.contains(join.getTargetJoinKey())) {
                    targetAliasName = alias.get(join.getTargetTable().toLowerCase());
                    ConditionExpr conditionExpr=this.buildSearchCondition(unit.searchField,unit.columnMeta,unit.item,null,targetAliasName);
                    expr.append(conditionExpr.startWithAnd());
                    continue;
                }
                //
                aliasIndex++;
                String tableAlias = "t_" + aliasIndex;
                if(alias.containsKey(join.getTargetTable().toLowerCase())) {
                    throw new IllegalArgumentException("不支持相同表 Join , table="+join.getTargetTable());
                }
                alias.put(join.getTargetTable().toLowerCase(), tableAlias);
                sourceAliasName = alias.get(join.getSourceTable().toLowerCase());
                targetAliasName = alias.get(join.getTargetTable().toLowerCase());

                List<ConditionExpr> conditions=join.getTargetPoint().getConditions();
                Map<String, PropertyRoute.DynamicValue> dynamicConditions=unit.route.getDynamicConditions(join);
                Expr joinExpr = null;
                if(conditions.isEmpty() && dynamicConditions.isEmpty()) {
                    joinExpr = new Expr(join.getJoinType().getJoinSQL() + " " + join.getTargetTable() + " " + tableAlias + " on ");
                } else {
                    Expr sub=new Expr("select * from "+ join.getTargetTable());
                    //附加在Join中配置的过滤条件
                    Where where=new Where();
                    if(!conditions.isEmpty()) {
                        for (ConditionExpr ce : conditions) {
                            where.and(ce);
                        }
                    }
                    // 附加在Join中配置的动态过滤条件
                    if(!dynamicConditions.isEmpty()) {
                        for (Map.Entry<String, PropertyRoute.DynamicValue> e : dynamicConditions.entrySet()) {
                            where.and(e.getKey()+" = ?", RelationSolver.getDynamicValue(service.dao(),e.getValue()));
                        }
                    }
                    sub.append(where);
                    joinExpr = new Expr(join.getJoinType().getJoinSQL() + " (" + sub.getListParameterSQL() + ") " + tableAlias + " on ",sub.getListParameters());
                }

                List<String> joinConditions = new ArrayList<>();
                for (int j = 0; j < join.getSourceFields().length; j++) {
                    String cdr = sourceAliasName + "." + join.getSourceFields()[j] + " = " + targetAliasName + "." + join.getTargetFields()[j];
                    joinConditions.add(cdr);
                }
                joinExpr.append(StringUtil.join(joinConditions, " and "));
                // 加入策略默认值
                ConditionExpr conditionExpr=this.buildDBTreatyCondition(targetAliasName);
                joinExpr.append(conditionExpr.startWithAnd());

                // 加入其它查询条件
                conditionExpr=this.buildSearchCondition(unit.searchField,unit.columnMeta,unit.item,null,targetAliasName);
                joinExpr.append(conditionExpr.startWithAnd());
                //
                expr.append(joinExpr);
                joinedPoints.add(join.getTargetJoinKey());
            }
        }


        DBTableMeta tm=service.getDBTableMeta();
        String searchFiledTable=null;


        //追加本表的查询条件
        Where where=new Where();

        ConditionExpr localConditionExpr=this.buildLocalCondition(sample,firstTableAlias);
        where.and(localConditionExpr);

        // 加入策略默认值
//        ConditionExpr conditionExpr=this.buildDBTreatyCondition(firstTableAlias);
//        where.and(conditionExpr);
//
//
        CompositeParameter parameter=this.getSearchValue(sample);
//        for (CompositeItem item : parameter) {
//            //排除已处理的 key
//            if(handledKeys.contains(item.getKey())) continue;
//            //没有搜索值，就不处理
//            Object searchValue=item.getValue();
//            if (StringUtil.isBlank(searchValue)) {
//                continue;
//            }
//            //优先使用明确指定的查询字段
//            String field = item.getField();
//            //如未明确指定，则使用key作为查询字段
//            if (StringUtil.isBlank(field)) {
//                field = item.getKey();
//            }
//            searchFiledTable = null;
//            if (field.contains(".")) {
//                String[] tmp = field.split("\\.");
//                searchFiledTable = tmp[0];
//                field = tmp[1];
//            }
//            field = BeanNameUtil.instance().depart(field);
//            //获得字段Meta
//            DBColumnMeta cm = null;
//            if (searchFiledTable == null || searchFiledTable.equalsIgnoreCase(service.table())) {
//                cm = tm.getColumn(field);
//            }
//
//            //如果字段在当前表存在，则不使用已关联的外部表查询
//            if (cm == null) {
//                continue;
//            }
//            // 加入查询条件
//            conditionExpr=this.buildSearchCondition(field,cm,item,parameter.getFuzzyFields(),firstTableAlias);
//            where.and(conditionExpr);
//        }

        Set<String> searchFields=parameter.getSearchFields();
        Set<String> fuzzyFields=parameter.getFuzzyFields();
        String searchValue = parameter.getSearchValue();

        //多字段合并查询
        if(searchFields!=null && searchFields.size()>0) {
            ConditionExpr ors=new ConditionExpr();
            for (String field : searchFields) {
                if (!StringUtil.isBlank(field) && !StringUtil.isBlank(searchValue)) {
                    DBColumnMeta cm=tm.getColumn(field);
                    if(cm==null) {
                        field=BeanNameUtil.instance().depart(field);
                        cm=tm.getColumn(field);
                    }
                    if(cm!=null) {
                        if(cm.getDBDataType()==DBDataType.STRING || cm.getDBDataType()==DBDataType.CLOB ) {
                            if(fuzzyFields!=null && fuzzyFields.contains(cm.getColumn().toLowerCase())) {
                                ors.or(firstTableAlias+ "." + cm.getColumn() + " like ?", "%" + searchValue + "%");
                            } else {
                                where.and(firstTableAlias+"."+cm.getColumn()+" = ?", searchValue.toString());
                            }
                        }
                    }
                }
            }
            if(!ors.isEmpty()){
                where.and(ors);
            }
        }


        if(customConditionExpr!=null) {
            where.and(customConditionExpr);
        }
        expr.append(where);

        if(orderBy!=null) {
            expr.append(orderBy);
        }

        return expr;
    }

    /**
     * 构建本表的查询条件
     * */
    public ConditionExpr buildLocalCondition(E sample,String targetTableAlias) {

        List<RouteUnit> units = this.getSearchRoutes(sample);
        Set<String> handledKeys=new HashSet<>();
        //循环扩展的条件路由单元
        for (RouteUnit unit : units) {
            //标记 key 已被处理，跳过在常规字段处理逻辑
            handledKeys.add(unit.item.getKey());
        }

        DBTableMeta tm=service.getDBTableMeta();
        String searchFiledTable=null;
        //追加本表的查询条件
        ConditionExpr conditionExpr=new ConditionExpr();

        // 加入策略默认值
        ConditionExpr dbTreatyConditionExpr=this.buildDBTreatyCondition(targetTableAlias);
        conditionExpr.and(dbTreatyConditionExpr);


        CompositeParameter parameter=this.getSearchValue(sample);
        for (CompositeItem item : parameter) {
            //排除已处理的 key
            if(handledKeys.contains(item.getKey())) continue;
            //没有搜索值，就不处理
            Object searchValue=item.getValue();
            if (StringUtil.isBlank(searchValue)) {
                continue;
            }
            //优先使用明确指定的查询字段
            String field = item.getField();
            //如未明确指定，则使用key作为查询字段
            if (StringUtil.isBlank(field)) {
                field = item.getKey();
            }
            searchFiledTable = null;
            if (field.contains(".")) {
                String[] tmp = field.split("\\.");
                searchFiledTable = tmp[0];
                field = tmp[1];
            }
            field = BeanNameUtil.instance().depart(field);
            //获得字段Meta
            DBColumnMeta cm = null;
            if (searchFiledTable == null || searchFiledTable.equalsIgnoreCase(service.table())) {
                cm = tm.getColumn(field);
            }

            //如果字段在当前表存在，则不使用已关联的外部表查询
            if (cm == null) {
                continue;
            }
            // 加入查询条件
            ConditionExpr conditionItemExpr=this.buildSearchCondition(field,cm,item,parameter.getFuzzyFields(),targetTableAlias);
            conditionExpr.and(conditionItemExpr);
        }
        return conditionExpr;
    }



    public ConditionExpr buildDBTreatyCondition(String targetTableAlias) {
        ConditionExpr conditionExpr=new ConditionExpr();
        DBTableMeta tm= service.getDBTableMeta();

        String prefix=targetTableAlias;
        if(!StringUtil.isBlank(prefix)) {
            prefix= prefix+".";
        }
        //加入逻辑删除条件
        DBColumnMeta delColumn=tm.getColumn(service.dao().getDBTreaty().getDeletedField());
        if(delColumn!=null) {
            conditionExpr.and(prefix+delColumn.getColumn()+"=?",service.dao().getDBTreaty().getFalseValue());
        }
        //加入租户条件
        DBColumnMeta tenantColumn=tm.getColumn(service.dao().getDBTreaty().getTenantIdField());
        if(tenantColumn!=null) {
            conditionExpr.and(prefix+tenantColumn.getColumn()+"=?",service.dao().getDBTreaty().getActivedTenantId());
        }
        return conditionExpr;
    }


    private ConditionExpr buildSearchCondition(String field,DBColumnMeta cm,CompositeItem item, Set<String> fuzzyFields,String tableAlias){

        ConditionExpr conditionExpr=new ConditionExpr();

        Boolean fuzzy=item.getFuzzy();
        if(fuzzy==null) fuzzy=false;

        String prefix=tableAlias+".";

        Object fieldValue=item.getValue();
        Object beginValue=item.getBegin();
        Object endValue=item.getEnd();
        String valuePrefix=item.getValuePrefix();
        if(valuePrefix==null) valuePrefix="";
        String valueSuffix=item.getValueSuffix();
        if(valueSuffix==null) valueSuffix="";

        //1.单值匹配
        if (fieldValue != null && beginValue == null && endValue == null) {
            if ((fieldValue instanceof List)) {
                if (fuzzy || (fuzzyFields != null && fuzzyFields.contains(cm.getColumn().toLowerCase()))) {
                    List<String> list = (List) fieldValue;
                    ConditionExpr listOr = new ConditionExpr();
                    for (String itm : list) {
                        ConditionExpr ors = buildFuzzyConditionExpr(cm.getColumn(), valuePrefix + itm.toString() + valueSuffix, tableAlias);
                        if (ors != null && !ors.isEmpty()) {
                            listOr.or(ors);
                        }
                    }
                    conditionExpr.and(listOr);
                } else {
                    if (!((List) fieldValue).isEmpty()) {
                        In in = new In(field, (List) fieldValue);
                        conditionExpr.and(in);
                    }
                }
            } else {
                if (cm.getDBDataType() == DBDataType.STRING
                        || cm.getDBDataType() == DBDataType.CLOB) {
                    if (!StringUtil.isBlank(fieldValue)) {
                        if (fuzzy || (fuzzyFields != null && fuzzyFields.contains(cm.getColumn().toLowerCase()))) {
                            ConditionExpr ors = buildFuzzyConditionExpr(cm.getColumn(), valuePrefix + fieldValue.toString() + valueSuffix, tableAlias);
                            if (ors != null && !ors.isEmpty()) {
                                conditionExpr.and(ors);
                            }
                        } else {
                            conditionExpr.andEquals(field, fieldValue);
                        }
                    }
                } else {
                    fieldValue = DataParser.parse(cm.getDBDataType().getType(), fieldValue);
                    conditionExpr.andEquals(field, fieldValue);
                }
            }
        }
        //2.范围匹配
        else if (fieldValue == null && (beginValue != null || endValue != null)) {

            if (cm.getDBDataType() == DBDataType.DATE) {
                Date beginDate = DataParser.parseDate(beginValue);
                Date endDate = DataParser.parseDate(endValue);
                //必要时交换位置
                if (beginDate != null && endDate != null && beginDate.getTime() > endDate.getTime()) {
                    Date tmp = beginDate;
                    beginDate = endDate;
                    endDate = tmp;
                }
                //
                conditionExpr.andIf(field + " >= ?", beginDate);
                conditionExpr.andIf(field + " <= ?", endDate);
            } else if (cm.getDBDataType() == DBDataType.TIMESTAME) {
                Timestamp beginDate = DataParser.parseTimestamp(beginValue);
                Timestamp endDate = DataParser.parseTimestamp(endValue);
                //必要时交换位置
                if (beginDate != null && endDate != null && beginDate.getTime() > endDate.getTime()) {
                    Timestamp tmp = beginDate;
                    beginDate = endDate;
                    endDate = tmp;
                }
                //
                conditionExpr.andIf(field + " >= ?", beginDate);
                conditionExpr.andIf(field + " <= ?", endDate);
            } else if (cm.getDBDataType() == DBDataType.INTEGER
                    || cm.getDBDataType() == DBDataType.LONG
                    || cm.getDBDataType() == DBDataType.DOUBLE
                    || cm.getDBDataType() == DBDataType.DECIMAL
                    || cm.getDBDataType() == DBDataType.BIGINT
                    || cm.getDBDataType() == DBDataType.FLOAT) {
                BigDecimal begin = DataParser.parseBigDecimal(beginValue);
                BigDecimal end = DataParser.parseBigDecimal(endValue);
                //必要时交换位置
                if (begin != null && end != null && begin.compareTo(end) == 1) {
                    BigDecimal tmp = begin;
                    begin = end;
                    end = tmp;
                }
                //
                conditionExpr.andIf(field + " >= ?", begin);
                conditionExpr.andIf(field + " <= ?", end);
            }
        }
        return conditionExpr;
    }

    private ConditionExpr buildFuzzyConditionExpr(String filed, String value,String tableAlias) {
        if(StringUtil.isBlank(value)) return null;
        String prefix=tableAlias+".";
        value=value.trim();
        value=value.replace("\t"," ");
        value=value.replace("\r"," ");
        value=value.replace("\n"," ");
        String[] vs=value.split(" ");
        ConditionExpr ors=new ConditionExpr();
        for (String v : vs) {
            ors.orLike(prefix+filed,v);
        }
        ors.startWithSpace();
        return ors;
    }

    private List<RouteUnit> getSearchRoutes(E sample) {
        List<RouteUnit> allRoutes = new ArrayList<>();
        DBTableMeta tm = service.dao().getTableMeta(service.table());
        CompositeParameter compositeParameter = getSearchValue(sample);
        String searchFiledTable = null;
        for (CompositeItem item : compositeParameter) {
            //优先使用明确指定的查询字段
            String field = item.getField();
            //如未明确指定，则使用key作为查询字段
            if (StringUtil.isBlank(field)) {
                field = item.getKey();
            }
            searchFiledTable = null;
            if (field.contains(".")) {
                String[] tmp = field.split("\\.");
                searchFiledTable = tmp[0];
                field = tmp[1];
            }
            field = BeanNameUtil.instance().depart(field);
            //获得字段Meta
            DBColumnMeta cm = null;
            if (searchFiledTable == null || searchFiledTable.equalsIgnoreCase(service.table())) {
                cm = tm.getColumn(field);
            }

            //如果字段在当前表存在，则不使用已关联的外部表查询
            if (cm != null) {
                continue;
            }
            //如果没有查询条件，则不继续处理
            Object searchValue = item.getValue();
            if (searchValue==null || (searchValue instanceof String && StringUtil.isBlank(searchValue)) || ((searchValue instanceof List && ((List)searchValue).isEmpty()))) {
                continue;
            }


            Object fillBy = item.getFillBy();
            List<String> fillByArr = new ArrayList<>();
            if (fillBy != null && fillBy instanceof List) {
                List arr = (List<String>) item.getFillBy();
                fillByArr.addAll(arr);
            } else {
                fillByArr.add((String) fillBy);
            }

            //获得前端指定查询的字段
            String configedField = item.getField();
            //如果字段不存在，那么说明是扩展外部，进行 Join 查询条件
            if ((StringUtil.isBlank(configedField) && fillByArr.size() > 1) || (!StringUtil.isBlank(configedField) && fillByArr.size() > 0)) {
                if (!StringUtil.isBlank(fillBy)) {
                    //获得并删除填充属性序列最后元素，若前端未明确指定搜索的字段，则使用此值
                    String configedFieldInFillBy = fillByArr.remove(fillByArr.size() - 1);
                    if (StringUtil.isBlank(configedField)) {
                        configedField = configedFieldInFillBy;
                    }

                    Class poType = (Class) service.getPoType();
                    List<PropertyRoute> routes = new ArrayList<>();
                    for (String fillByField : fillByArr) {
                        PropertyRoute route = service.dao().getRelationManager().findProperties(poType, fillByField);
                        if (route == null) {
                            throw new RuntimeException("关联关系未配置");
                        }
                        poType = route.getTargetPoType();
                        try {
                            Field f= poType.getDeclaredField(route.getProperty());
                            // 一对多判断
                            if(ReflectUtil.isSubType(List.class,f.getType())) {
                                throw new RuntimeException("Join 路径中的 "+poType.getSimpleName()+"."+route.getProperty() +" 是一对多关系，暂不支持" );
                            }
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                        routes.add(route);
                    }

                    if (configedField!=null && configedField.contains(".")) {
                        String[] tmp = configedField.split("\\.");
                        searchFiledTable=tmp[0];
                        configedField = tmp[1];
                    }

                    if(routes.isEmpty()) {
                        throw new RuntimeException("搜索选项["+item.getKey()+"]配置错误：fillBy = "+ JSON.toJSONString(item.getFillBy())+" , field = "+item.getFillBy());
                    }

                    //路由合并
                    PropertyRoute route = PropertyRoute.merge(routes, searchFiledTable);
                    RouteUnit routeUnit=new RouteUnit();
                    routeUnit.route=route;
                    routeUnit.item=item;
                    routeUnit.searchField=configedField;
                    routeUnit.table=searchFiledTable;
                    routeUnit.columnMeta=service.dao().getTableColumnMeta(searchFiledTable,configedField);
                    allRoutes.add(routeUnit);
                }
            }
        }
        return allRoutes;
    }

    public OrderBy buildOrderBy(E sample,String tableAlias) {
        String sortField=BeanUtil.getFieldValue(sample, "sortField",String.class);
        String sortType=BeanUtil.getFieldValue(sample, "sortType",String.class);
        OrderBy orderBy=null;
        if(!StringUtil.isBlank(sortField) && !StringUtil.isBlank(sortType)) {
            DBColumnMeta cm=service.dao().getTableMeta(service.table()).getColumn(sortField);
            if(cm==null) {
                sortField=BeanNameUtil.instance().depart(sortField);
                cm=service.dao().getTableMeta(service.table()).getColumn(sortField);
            }
            if(cm!=null) {
                if("asc".equalsIgnoreCase(sortType)) {
                    orderBy=OrderBy.byAscNullsLast(tableAlias+"."+sortField);
                }
                else if("desc".equalsIgnoreCase(sortType)) {
                    orderBy=OrderBy.byDescNullsLast(tableAlias+"."+sortField);
                }
            }
        }
        return orderBy;
    }



	public  <S extends Entity,T extends Entity> Expr buildExists(String tableAliase,List<String> fillBys, String field,Object value,boolean fuzzy) {
		if(value==null) return null;

		String tab=null;
		if(field.contains(".")) {
			String[] tmp=field.split("\\.");
			tab=tmp[0];
			field=tmp[1];
		}

		Class poType=(Class)service.getPoType();
		List<PropertyRoute> routes=new ArrayList<>();
		for (String fillBy : fillBys) {
			PropertyRoute<S, T> route=service.dao().getRelationManager().findProperties(poType,fillBy);
			if(route==null) {
				throw new RuntimeException("关联关系未配置");
			}
			poType=route.getTargetPoType();
			routes.add(route);
		}
		//路由合并
		PropertyRoute<S, T> route=PropertyRoute.merge(routes,tab);

		RelationSolver relationSolver=service.dao().getRelationSolver();
		JoinResult jr=new JoinResult();
		Class<T> targetType=route.getTargetPoType();

		Map<String,Object> result=relationSolver.buildJoinStatement(jr,poType,null,route,targetType,false);
		Expr expr=(Expr)result.get("expr");

		Map<String,String> alias=(Map<String,String>)result.get("tableAlias");

		Join firstJoin=route.getJoins().get(0);
		Join lastJoin=route.getJoins().get(route.getJoins().size()-1);
		DBField[] sourceFields=lastJoin.getSourceFields();
		DBField[] targetFields=lastJoin.getTargetFields();
		String joinTableAlias=alias.get(lastJoin.getTargetTable());
		String targetTableAlias=alias.get(firstJoin.getTargetTable());

		//判断字段有效性
		Where where = null;

		//检测字段，并调整字段的真实名称
		DBTableMeta tm = service.dao().getTableMeta(firstJoin.getTargetTable());
		DBColumnMeta cm = tm.getColumn(field);
		if (cm == null) {
			field=BeanNameUtil.instance().depart(field);
			cm = tm.getColumn(field);
		}
		if (cm == null) {
			throw new IllegalArgumentException("字段 " + firstJoin.getTargetTable() + "." + field + "不存在");
		}

		//设置关联条件
		where=new Where();
		for (int i = 0; i < sourceFields.length; i++) {
			where.and(tableAliase+"."+sourceFields[i].name()+" = "+joinTableAlias+"."+targetFields[i].name());
		}

		//如果是模糊搜索
		if(fuzzy) {
			if(value instanceof  List) {
				List<String> list = (List) value;
				ConditionExpr listOr = new ConditionExpr();
				for (String itm : list) {
					ConditionExpr ors = buildFuzzyConditionExpr(field, itm.toString(), targetTableAlias + ".");
					if (ors != null && !ors.isEmpty()) {
						listOr.or(ors);
					}
				}
				where.and(listOr);
			} else {
				where.andLike(targetTableAlias+"."+field,value.toString());
			}
		} else {
			if(value instanceof String) {
				value=((String)value).split(",");
				In in = new In(targetTableAlias+"."+field, (String[]) value);
				where.and(in);
			}
			else if (!((List) value).isEmpty()) {
				In in = new In(targetTableAlias+"."+field, (List) value);
				where.and(in);
			}
		}

		//追加条件
		expr.append(where);

		//装配 exists 语句
		String sql="exists( "+expr.getListParameterSQL()+" )";
		int a=sql.toLowerCase().indexOf("select ");
		int b=sql.toLowerCase().indexOf(" from");
		sql=sql.substring(0,a+7)+" 1 "+sql.substring(b);
		Expr exists=new Expr(sql,expr.getListParameters());
		return exists;

	}


    private static class RouteUnit {
        private PropertyRoute route;
        private CompositeItem item;
        private String searchField;
        private String table;
        private DBColumnMeta columnMeta;
    }

}

