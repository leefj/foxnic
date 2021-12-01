package com.github.foxnic.dao.entity;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.api.model.CompositeItem;
import com.github.foxnic.api.model.CompositeParameter;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.environment.Environment;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.dataperm.ConditionBuilder;
import com.github.foxnic.dao.dataperm.DataPermContext;
import com.github.foxnic.dao.dataperm.DataPermException;
import com.github.foxnic.dao.dataperm.model.DataPermCondition;
import com.github.foxnic.dao.dataperm.model.DataPermRange;
import com.github.foxnic.dao.dataperm.model.DataPermRule;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.*;
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
        return buildSelect(sample,null,customConditionExpr,orderBy,false);
    }

    /**
     * 生成 Select 语句，join关系不变则表别名不变
     * @param dpcode 数据权限代码
     * */
    public Expr buildSelect(E sample, String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy,String dpcode) {
		DataPermRule rule=service.dao().getDataPermManager().get(dpcode);
		if(rule==null) {
		    throw new DataPermException(dpcode+ " 不是一个有效的数据权限代码");
        }
        List<Expr> selects=new ArrayList<>();

		//创建数据权限上下文
        DataPermContext dataPermContext=new DataPermContext();
        dataPermContext.setVo(sample);
        dataPermContext.setSession(service.dao().getDBTreaty().getSubject());
        dataPermContext.setEnv(Environment.getEnvironment());

        Set<String> conditionKeys=new HashSet<>();
        String sqlKey=null;
        //循环数据权限范围，一个范围对应一条查询语句
		for (DataPermRange range : rule.getRanges()) {
            ConditionExpr appendsExpr=new ConditionExpr();
            if(customConditionExpr!=null) {
                appendsExpr.and(customConditionExpr);
            }

            if(!this.service.getPoType().getName().equals(rule.getPoType())) {
                throw new DataPermException("PO类型不一致，当前类型："+this.service.getPoType().getName()+"，权限配置类型："+rule.getPoType());
            }
            //根据数据权限构建查询语句
            ConditionExpr conditionExpr=(new ConditionBuilder(dataPermContext,this,this.service.dao(),this.service.getPoType(),tabAlias,range)).build();
            appendsExpr.and(conditionExpr);
            sqlKey=appendsExpr.getSQL();
            //去重：如果查询条件相同，则无需后续的 union
            if(conditionKeys.contains(sqlKey)) continue;
            conditionKeys.add(sqlKey);
            //创建查询语句
            Expr select=this.buildSelect(sample,tabAlias,appendsExpr,null,true);
            //将查询语句添加到列表，用于后续构建 union 语句
            selects.add(select);
        }


		Expr datapmSelect=null;
		//如果只有一条查询语句，直接使用该条语句
		if(selects.size()==1) {
            datapmSelect=selects.get(0);
            //增加排序
            if(orderBy!=null) {
                datapmSelect.append(orderBy);
            }
        } else {
		    //如果有多个查询语句，则构建 union 语句
            datapmSelect=new Expr("select * from (");
            for (int i = 0; i < selects.size(); i++) {
                Expr select = selects.get(i);
                datapmSelect.append("( ");
                datapmSelect.append(select);
                datapmSelect.append(")");
                if(i<selects.size()-1) {
                    datapmSelect.append(" union ");
                }
            }
            datapmSelect.append(") "+tabAlias);
            //增加排序
            if(orderBy!=null) {
                datapmSelect.append(orderBy);
            }
        }
        // System.err.println(datapmSelect.getSQL());
		return datapmSelect;
    }

    /**
     *  生成 Select 语句，join关系不变则表别名不变
     * */
    public Expr buildSelect(E sample, String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy,boolean flagDataPerm) {


        List<RouteUnit> units = this.getSearchRoutes(sample);

        //加入在处理数据权限时搜集的Join关系
        units.addAll(0,this.dataPermUnits);

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


            if(unit.route.isList()) {
                continue;
            }

            Collections.reverse(joins);


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
                ConditionExpr conditionExpr=this.buildDBTreatyCondition(join.getTargetTable(),targetAliasName);
                joinExpr.append(conditionExpr.startWithAnd());

                // 加入其它查询条件，如果来自数据权限
                if(unit.dataPermCondition!=null && unit.conditionBuilder!=null) {
                    conditionExpr=unit.conditionBuilder.buildDataPermLocalCondition(unit.searchTable,targetAliasName,unit.searchField,unit.dataPermCondition);
                    joinExpr.append(conditionExpr.startWithAnd());
                }
                //如果来自用户搜索
                else {
                    if(unit.searchTable.equalsIgnoreCase(join.getTargetTable())) {
                        conditionExpr = this.buildSearchCondition(unit.searchField, unit.columnMeta, unit.item, null, targetAliasName);
                        joinExpr.append(conditionExpr.startWithAnd());
                    }
                }

                //
                expr.append(joinExpr);
                joinedPoints.add(join.getTargetJoinKey());
            }
        }

        //追加本表的查询条件
        Where where=new Where();

        ConditionExpr localConditionExpr=this.buildLocalConditionInternal(sample,firstTableAlias,units,true);
        where.and(localConditionExpr);

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
    public ConditionExpr buildLocalCondition(E sample,String targetTableAlias,List<RouteUnit> units) {
        return buildLocalConditionInternal(sample,targetTableAlias,units,false);
    }

    /**
     * 构建本表的查询条件
     * */
    private ConditionExpr buildLocalConditionInternal(E sample,String targetTableAlias,List<RouteUnit> units,boolean internal) {

        if(units==null) {
            units = this.getSearchRoutes(sample);
        }
        Set<String> handledKeys=new HashSet<>();
        Map<String,RouteUnit> existsUnits=new HashMap<>();
        //循环扩展的条件路由单元
        for (RouteUnit unit : units) {
            //如果是来自内部(当前类)，那么isList 使用 exist
            if(internal) {
                if (unit.route.isList()) {
                    existsUnits.put(unit.item.getKey(), unit);
                    continue;
                }
            }
            //如果是来自外部，全部使用 exists 语句完成查询
            else {
                existsUnits.put(unit.item.getKey(), unit);
                continue;
            }
            //标记 key 已被处理，跳过在常规字段处理逻辑
            handledKeys.add(unit.item.getKey());
        }

        DBTableMeta tm=service.getDBTableMeta();
        String searchFiledTable=null;
        //追加本表的查询条件
        ConditionExpr conditionExpr=new ConditionExpr();

        // 加入策略默认值
        ConditionExpr dbTreatyConditionExpr=this.buildDBTreatyCondition(service.table(),targetTableAlias);
        conditionExpr.and(dbTreatyConditionExpr);


        CompositeParameter parameter=this.getSearchValue(sample);
        for (CompositeItem item : parameter) {
            //排除已处理的 key
            if(handledKeys.contains(item.getKey())) continue;
            //没有搜索值，就不处理
            Object searchValue=item.getValue();
            if (StringUtil.isBlank(searchValue) && StringUtil.isBlank(item.getBegin()) && StringUtil.isBlank(item.getEnd())) {
                continue;
            }

            RouteUnit existsUnit=existsUnits.get(item.getKey());
            if(existsUnit!=null) {
                Expr expr=this.buildExists(targetTableAlias,existsUnit.fillBys,existsUnit.searchTable,existsUnit.searchField,searchValue,item.getFuzzy());
                conditionExpr.and(expr);
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
                            if(fuzzyFields!=null && ( fuzzyFields.contains(cm.getColumn().toLowerCase()) || fuzzyFields.contains(cm.getColumnVarName())) ) {
                                ors.or(targetTableAlias+ "." + cm.getColumn() + " like ?", "%" + searchValue + "%");
                            } else {
                                conditionExpr.and(targetTableAlias+"."+cm.getColumn()+" = ?", searchValue.toString());
                            }
                        }
                    }
                }
            }
            if(!ors.isEmpty()){
                conditionExpr.and(ors);
            }
        }


        return conditionExpr;
    }



    public ConditionExpr buildDBTreatyCondition(String targetTable,String targetTableAlias) {
        ConditionExpr conditionExpr=new ConditionExpr();
        DBTableMeta tm= service.dao().getTableMeta(targetTable);

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
        Object tenantId=service.dao().getDBTreaty().getActivedTenantId();
        if(tenantColumn!=null && tenantId!=null) {
            conditionExpr.and(prefix+tenantColumn.getColumn()+"=?",tenantId);
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
                if (fuzzy || (fuzzyFields != null && (fuzzyFields.contains(cm.getColumn().toLowerCase())|| fuzzyFields.contains(cm.getColumnVarName())))) {
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
                        if (fuzzy || (fuzzyFields != null && ( fuzzyFields.contains(cm.getColumn().toLowerCase()) || fuzzyFields.contains(cm.getColumnVarName())))) {
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

    /**
     * 根据查询参数，获得所有关联关系
     * */
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

            if (StringUtil.isBlank(searchValue) && StringUtil.isBlank(item.getBegin()) && StringUtil.isBlank(item.getEnd())) {
                continue;
            }
            //如果是空的列表
            if (searchValue!=null &&  (searchValue instanceof List) && ((List)searchValue).isEmpty()) {
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
                    List<String> fillByArr2 = new ArrayList<>();
                    if(fillByArr.size()>0) {
                        //获得并删除填充属性序列最后元素，若前端未明确指定搜索的字段，则使用此值
                        String configedFieldInFillBy = null ;//fillByArr.remove(fillByArr.size() - 1);
                        Class type=this.service.getPoType();

                        //按类型处理属性路径
                        for (int i = 0; i < fillByArr.size(); i++) {
                            String prop=fillByArr.get(i);
                            try {
                                Field f=type.getDeclaredField(prop);
                                if(ReflectUtil.isSubType(List.class,f.getType())) {
                                    type=ReflectUtil.getListComponentType(f);
                                    if(type==null) {
                                        throw new RuntimeException("类型识别错误");
                                    }
                                    fillByArr2.add(prop);
                                } else if(DataParser.isSimpleType(f.getType())) {
                                    configedFieldInFillBy=fillByArr.get(i);
                                    break;
                                } else {
                                    type=f.getType();
                                    fillByArr2.add(prop);
                                }
                            } catch (NoSuchFieldException e) {
                                Logger.exception("属性获取失败",e);
                            }
                        }

                        fillByArr=fillByArr2;

                        if (StringUtil.isBlank(configedField)) {
                            configedField = configedFieldInFillBy;
                        }
                    }

                    Class poType = (Class) service.getPoType();
                    List<PropertyRoute> routes = new ArrayList<>();
                    for (String fillByField : fillByArr) {
                        PropertyRoute route = service.dao().getRelationManager().findProperties(poType, fillByField);

                        if (route == null) {
                            throw new RuntimeException("关联关系未配置:"+poType.getName()+"."+fillByField);
                        }

                        routes.add(route);
                        poType=route.getTargetPoType();
                    }

                    //多层关系是判断是否存在一对多,并提示
//                    if(routes.size()>=2) {
//                        for (PropertyRoute route : routes) {
//                            if(route.isList()) {
//                                throw new RuntimeException("Join 路径中的 "+poType.getSimpleName()+"."+route.getProperty() +" 是一对多关系，暂不支持" );
//                            }
//                        }
//                    }

                    if (configedField!=null && configedField.contains(".")) {
                        String[] tmp = configedField.split("\\.");
                        searchFiledTable=tmp[0];
                        configedField = tmp[1];
                    }

                    if(routes.isEmpty()) {
                        throw new RuntimeException("搜索选项["+item.getKey()+"]配置错误：fillBy = "+ JSON.toJSONString(item.getFillBy())+" , field = "+item.getFillBy());
                    }

                    if(StringUtil.isBlank(searchFiledTable)) {
                        searchFiledTable=this.service.table();
                    }
                    //路由合并
                    PropertyRoute route = PropertyRoute.merge(routes, searchFiledTable);
                    RouteUnit routeUnit=new RouteUnit();
                    routeUnit.route=route;
                    routeUnit.item=item;
                    routeUnit.searchField=configedField;
                    routeUnit.searchTable =searchFiledTable;
                    routeUnit.columnMeta=service.dao().getTableColumnMeta(searchFiledTable,configedField);
                    routeUnit.fillBys=fillByArr;
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



	public  <S extends Entity,T extends Entity> Expr buildExists(String tableAliase,List<String> fillBys, String searchTable,String searchField,Object value,Boolean fuzzy) {
		if(value==null) return null;
        if(fuzzy==null) fuzzy=false;


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
		PropertyRoute<S, T> route=PropertyRoute.merge(routes,searchTable);

		RelationSolver relationSolver=service.dao().getRelationSolver();
		JoinResult jr=new JoinResult();
		Class<T> targetType=route.getTargetPoType();

        BuildingResult result=relationSolver.buildJoinStatement(jr,poType,null,route,targetType,false);
		Expr expr=result.getExpr();

		Map<String,String> alias=result.getTableAlias();

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
		DBColumnMeta cm = tm.getColumn(searchField);
		if (cm == null) {
            searchField=BeanNameUtil.instance().depart(searchField);
			cm = tm.getColumn(searchField);
		}
		if (cm == null) {
			throw new IllegalArgumentException("字段 " + firstJoin.getTargetTable() + "." + searchField + "不存在");
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
					ConditionExpr ors = buildFuzzyConditionExpr(searchField, itm.toString(), targetTableAlias + ".");
					if (ors != null && !ors.isEmpty()) {
						listOr.or(ors);
					}
				}
				where.and(listOr);
			} else {
				where.andLike(targetTableAlias+"."+searchField,value.toString());
			}
		} else {
			if(value instanceof String) {
				value=((String)value).split(",");
				In in = new In(targetTableAlias+"."+searchField, (String[]) value);
				where.and(in);
			}
			else if (!((List) value).isEmpty()) {
				In in = new In(targetTableAlias+"."+searchField, (List) value);
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

	private List<RouteUnit> dataPermUnits=new ArrayList<>();

    public void addDataPermUnits(RouteUnit unit) {
        this.dataPermUnits.add(unit);
    }


    public static class RouteUnit {
        private PropertyRoute route;
        private CompositeItem item;
        private String searchField;
        private String searchTable;
        private DBColumnMeta columnMeta;
        private List<String> fillBys;
        /**
         * 如果是来自 ConditionBuilder ，该值非 null
         * */
        private DataPermCondition dataPermCondition;
        /**
         * 如果是来自 ConditionBuilder ，该值非 null
         * */
        private ConditionBuilder conditionBuilder;

        public ConditionBuilder getConditionBuilder() {
            return conditionBuilder;
        }

        public void setConditionBuilder(ConditionBuilder conditionBuilder) {
            this.conditionBuilder = conditionBuilder;
        }

        public DataPermCondition getDataPermCondition() {
            return dataPermCondition;
        }

        public void setDataPermCondition(DataPermCondition dataPermCondition) {
            this.dataPermCondition = dataPermCondition;
        }

        public PropertyRoute getRoute() {
            return route;
        }

        public void setRoute(PropertyRoute route) {
            this.route = route;
        }

        public CompositeItem getItem() {
            return item;
        }

        public void setItem(CompositeItem item) {
            this.item = item;
        }

        public String getSearchField() {
            return searchField;
        }

        public void setSearchField(String searchField) {
            this.searchField = searchField;
        }

        public String getSearchTable() {
            return searchTable;
        }

        public void setSearchTable(String searchTable) {
            this.searchTable = searchTable;
        }

        public DBColumnMeta getColumnMeta() {
            return columnMeta;
        }

        public void setColumnMeta(DBColumnMeta columnMeta) {
            this.columnMeta = columnMeta;
        }

        public List<String> getFillBys() {
            return fillBys;
        }

        public void setFillBys(List<String> fillBys) {
            this.fillBys = fillBys;
        }


    }

}

