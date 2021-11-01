package com.github.foxnic.dao.dataperm;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.api.dataperm.ConditionNodeType;
import com.github.foxnic.api.dataperm.ExprType;
import com.github.foxnic.api.dataperm.LogicType;
import com.github.foxnic.api.model.CompositeItem;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.dao.dataperm.model.DataPermCondition;
import com.github.foxnic.dao.dataperm.model.DataPermRange;
import com.github.foxnic.dao.dataperm.model.DataPermVariable;
import com.github.foxnic.dao.entity.QuerySQLBuilder;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.Join;
import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.relation.RelationSolver;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Where;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConditionBuilder {

    private DataPermRange range;
    private String tabAlias;
    private DAO dao;
    private DataPermManager dataPermManager;
    private Class poType;
    private QuerySQLBuilder querySQLBuilder;

    public ConditionBuilder(QuerySQLBuilder querySQLBuilder,DAO dao,Class poType,String tabAlias, DataPermRange range) {
        this.querySQLBuilder=querySQLBuilder;
        this.dao=dao;
        this.poType=poType;
        this.range = range;
        this.tabAlias = tabAlias;
        this.range.buildTreeIf();
        this.dataPermManager=dao.getDataPermManager();
    }

    /**
     * 构建查询表达式
     * */
    public ConditionExpr build() {
        DataPermCondition root = this.range.getRoot();
        ConditionExpr conditionExpr = this.build(root);
        return conditionExpr;
    }

    /**
     * 按节点构建查询表达式
     * */
    private ConditionExpr build(DataPermCondition node) {
        ConditionExpr conditionExpr = null;
        if (node.getNodeType() == ConditionNodeType.expr) {
            conditionExpr = buildConditionExpr(node);
        } else if (node.getNodeType() == ConditionNodeType.group) {
            conditionExpr = new ConditionExpr();
            for (DataPermCondition child : node.getChildren()) {
                ConditionExpr ce = this.build(child);
                if (child.getLogicType() == LogicType.or) {
                    conditionExpr.or(ce);
                } else if (child.getLogicType() == LogicType.and) {
                    conditionExpr.and(ce);
                }

            }
        }
        return conditionExpr;
    }

    /**
     * 按节点构建表达式，细化
     * */
    private ConditionExpr buildConditionExpr(DataPermCondition node) {
        ConditionExpr conditionExpr = null;
        String[] fillByProps = node.getQueryProperty().split("\\.");
        String[] qfs = node.getQueryField().split("\\.");
        String queryTable = qfs[0];
        String queryField = qfs[1];

        if (fillByProps.length == 1) {
            conditionExpr = buildLocalCondition(node, fillByProps[0], queryField);
        } else {

            List<PropertyRoute> routes = new ArrayList<>();
            //循环，排除最后一个属性
            Class poType=this.poType;
            boolean hasList=false;
            List<String> fillBys=new ArrayList<>();
            for (int i = 0; i < fillByProps.length-1; i++) {
                PropertyRoute route = dao.getRelationManager().findProperties(poType, fillByProps[i]);
                if (route == null) {
                    throw new RuntimeException("关联关系未配置");
                }
                if(route.isList()) {
                    hasList=true;
                }
                poType=route.getTargetPoType();
                fillBys.add(fillByProps[i]);
                routes.add(route);

            }
            //路由合并
            PropertyRoute route = PropertyRoute.merge(routes, queryTable);
            conditionExpr=new ConditionExpr();
            if(hasList) {
                Expr exists= this.buildExists(node,route,fillBys,queryTable,queryField,this.getVariableValues(node));
                conditionExpr.and(exists);
                System.out.println();
            } else {

                QuerySQLBuilder.RouteUnit routeUnit=new QuerySQLBuilder.RouteUnit();
                routeUnit.setRoute(route);
                CompositeItem item=new CompositeItem();
                routeUnit.setItem(item);
                routeUnit.setSearchField(queryField);
                routeUnit.setTable(queryTable);
                routeUnit.setColumnMeta(dao.getTableColumnMeta(queryTable,queryField));
                routeUnit.setFillBys(fillBys);
                routeUnit.setDataPermCondition(node);

                this.querySQLBuilder.addDataPermUnits(routeUnit);
            }
            System.out.println();
        }
        return conditionExpr;
    }

    private Object[] getVariableValues(DataPermCondition node) {
        JSONArray vars=node.getVaribales();
        Object[] values=new Object[vars.size()];
        for (int i = 0; i < vars.size(); i++) {
            DataPermVariable variable=new DataPermVariable(dataPermManager,vars.get(i));
            values[i]=variable.getValue();
        }
        return values;
    }

    private ConditionExpr buildLocalCondition(DataPermCondition node,String fillByProp, String queryField) {
        Object[] values=this.getVariableValues(node);
        String sql = null;
        ExprType exprType=node.getExprType();
        //单个参数
        if(exprType.minVars()==1 && exprType.maxVars()==1) {
            sql=tabAlias+"."+queryField + " " + node.getExprType().operator() + " ?";
        }
        return new ConditionExpr(sql,values);
    }

    private Expr buildExists(DataPermCondition node,PropertyRoute route,List<String> fillBys, String queryTable, String queryField, Object[] values) {

//		String tab=null;
//		if(field.contains(".")) {
//			String[] tmp=field.split("\\.");
//			tab=tmp[0];
//			field=tmp[1];
//		}

//        Class poType=(Class)service.getPoType();
//        List<PropertyRoute> routes=new ArrayList<>();
//        for (String fillBy : fillBys) {
//            PropertyRoute<S, T> route=service.dao().getRelationManager().findProperties(poType,fillBy);
//            if(route==null) {
//                throw new RuntimeException("关联关系未配置");
//            }
//            poType=route.getTargetPoType();
//            routes.add(route);
//        }
//        //路由合并
//        PropertyRoute<S, T> route=PropertyRoute.merge(routes,searchTable);

        RelationSolver relationSolver=dao.getRelationSolver();
        JoinResult jr=new JoinResult();
        Class targetType=route.getTargetPoType();

        Map<String,Object> result=relationSolver.buildJoinStatement(jr,poType,null,route,targetType,false);
        Expr expr=(Expr)result.get("expr");

        Map<String,String> alias=(Map<String,String>)result.get("tableAlias");

        Join firstJoin= (Join) route.getJoins().get(0);
        Join lastJoin= (Join) route.getJoins().get(route.getJoins().size()-1);
        DBField[] sourceFields=lastJoin.getSourceFields();
        DBField[] targetFields=lastJoin.getTargetFields();
        String joinTableAlias=alias.get(lastJoin.getTargetTable());
        String targetTableAlias=alias.get(firstJoin.getTargetTable());

        //判断字段有效性
        Where where = null;

        //检测字段，并调整字段的真实名称
        DBTableMeta tm = dao.getTableMeta(firstJoin.getTargetTable());
        DBColumnMeta cm = tm.getColumn(queryField);
        if (cm == null) {
            queryField= BeanNameUtil.instance().depart(queryField);
            cm = tm.getColumn(queryField);
        }
        if (cm == null) {
            throw new IllegalArgumentException("字段 " + firstJoin.getTargetTable() + "." + queryField + "不存在");
        }

        //设置关联条件
        where=new Where();
        for (int i = 0; i < sourceFields.length; i++) {
            where.and(tabAlias+"."+sourceFields[i].name()+" = "+joinTableAlias+"."+targetFields[i].name());
        }

        ExprType exprType=node.getExprType();
        String ce=null;
        //单个参数
        if(exprType.minVars()==1 && exprType.maxVars()==1) {
            ce=targetTableAlias+"."+queryField + " " + node.getExprType().operator() + " ?";
        }
        ConditionExpr conditionExpr=new  ConditionExpr(ce,values);
        where.and(conditionExpr);
        //如果是模糊搜索
//        if(fuzzy) {
//            if(value instanceof  List) {
//                List<String> list = (List) value;
//                ConditionExpr listOr = new ConditionExpr();
//                for (String itm : list) {
//                    ConditionExpr ors = buildFuzzyConditionExpr(searchField, itm.toString(), targetTableAlias + ".");
//                    if (ors != null && !ors.isEmpty()) {
//                        listOr.or(ors);
//                    }
//                }
//                where.and(listOr);
//            } else {
//                where.andLike(targetTableAlias+"."+searchField,value.toString());
//            }
//        } else {
//            if(value instanceof String) {
//                value=((String)value).split(",");
//                In in = new In(targetTableAlias+"."+searchField, (String[]) value);
//                where.and(in);
//            }
//            else if (!((List) value).isEmpty()) {
//                In in = new In(targetTableAlias+"."+searchField, (List) value);
//                where.and(in);
//            }
//        }

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
}
