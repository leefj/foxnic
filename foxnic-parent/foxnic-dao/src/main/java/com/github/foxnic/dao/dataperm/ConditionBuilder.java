package com.github.foxnic.dao.dataperm;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.api.dataperm.ConditionNodeType;
import com.github.foxnic.api.dataperm.ExprType;
import com.github.foxnic.api.dataperm.LogicType;
import com.github.foxnic.api.model.CompositeItem;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.dataperm.model.DataPermCondition;
import com.github.foxnic.dao.dataperm.model.DataPermRange;
import com.github.foxnic.dao.entity.QuerySQLBuilder;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.*;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.In;
import com.github.foxnic.sql.expr.Where;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConditionBuilder {

    private DataPermRange range;
    private String tabAlias;
    private DAO dao;
    private DataPermManager dataPermManager;
    private Class poType;
    private QuerySQLBuilder querySQLBuilder;
    private DataPermContext dataPermContext;

    public ConditionBuilder(DataPermContext dataPermContext,QuerySQLBuilder querySQLBuilder,DAO dao,Class poType,String tabAlias, DataPermRange range) {
        this.querySQLBuilder=querySQLBuilder;
        this.dataPermContext=dataPermContext;
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
        //按树形结构进行构建，每个分组在单独的括号内
        ConditionExpr conditionExpr = this.build(root);
         return conditionExpr;
    }

    private Boolean checkSpringELCondition(DataPermCondition node) {
        if(StringUtil.isBlank(node.getConditionExpr())) return true;
         Result<Boolean> r=dataPermContext.testConditionExpr(node.getConditionExpr());
         return r.data();
    }

    /**
     * 按节点构建查询表达式
     * */
    private ConditionExpr build(DataPermCondition node) {
        if(!node.isValid()) return new ConditionExpr();
        ConditionExpr conditionExpr = null;
        //计算语句构建条件，如果规则判断不需要构建，则返回空的条件语句
        Boolean valid=checkSpringELCondition(node);
        if(!valid) {
            return  new ConditionExpr();
        }
        //如果是表达式，直接构建
        if (node.getNodeType() == ConditionNodeType.expr) {
            conditionExpr = buildQueryConditionExpr(node);
        }
        //如果是逻辑组
        else if (node.getNodeType() == ConditionNodeType.group) {
            conditionExpr = new ConditionExpr();
            //循环组内下级节点
            for (DataPermCondition child : node.getChildren()) {
                //递归构建
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
    private ConditionExpr buildQueryConditionExpr(DataPermCondition node) {
        ConditionExpr conditionExpr = null;
        String[] fillByProps = node.getQueryProperty().split("\\.");
        String[] qfs = node.getQueryField().split("\\.");
        String queryTable = qfs[0];
        String queryField = qfs[1];

        //如果 fillByProps 只有一层，则说明是 po 的直接属性，构建本表查询条件
        if (fillByProps.length == 1) {
            conditionExpr = buildLocalQueryCondition(tabAlias,node, queryField);
        }
        //如果 fillByProps 多层，则为关联了属性（关联属性有分 对一 和 对多 的情况）
        else {

            List<PropertyRoute> routes = new ArrayList<>();
            //循环，排除最后一个属性
            Class poType=this.poType;
            boolean hasList=false;
            List<String> fillBys=new ArrayList<>();
            //通过属性层级获得关联路由
            for (int i = 0; i < fillByProps.length-1; i++) {
                PropertyRoute route = dao.getRelationManager().findProperties(poType, fillByProps[i]);
                if (route == null) {
                    throw new RuntimeException("关联关系未配置");
                }
                if(route.isList()) {
                    hasList=true;
                }
                poType=route.getSlavePoType();
                fillBys.add(fillByProps[i]);
                routes.add(route);
            }
            //路由合并
            PropertyRoute route = PropertyRoute.merge(routes, queryTable);
            conditionExpr=new ConditionExpr();
            //如果是 对多 的关系，那么构建 exists 语句
            if(hasList) {
                Expr exists= this.buildQueryExists(node,route,fillBys,queryTable,queryField,this.getVariableValues(node));
                conditionExpr.and(exists);
            }
            //如果是 对一 的关系，则构建与本表的 join on 语句，创建 RouteUnit 为 QuerySQLBuilder 提供关联
            else {
                QuerySQLBuilder.RouteUnit routeUnit=new QuerySQLBuilder.RouteUnit();
                routeUnit.setRoute(route);
                CompositeItem item=new CompositeItem();
                routeUnit.setItem(item);
                routeUnit.setSearchField(queryField);
                routeUnit.setSearchTable(queryTable);
                routeUnit.setColumnMeta(dao.getTableColumnMeta(queryTable,queryField));
                routeUnit.setFillBys(fillBys);
                routeUnit.setDataPermCondition(node);
                routeUnit.setConditionBuilder(this);
                //交给 QuerySQLBuilder 去处理
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
            values[i]=this.getVariableValue(vars.get(i));
        }
        return values;
    }

    private Object getVariableValue(Object value) {
        //如果 null 直接返回
        if(value==null) return null;
        //如果非字符串，直接返回
        if(!(value instanceof String))  return value;
        String expr=(String) value;
        expr=expr.trim();
        //如果是表达式，则计算表达式
        if(expr.startsWith("${") && expr.endsWith("}")) {
            expr=expr.substring(2,expr.length()-1);
            value=this.dataPermContext.getVariableValue(expr);
            return value;
        } else {
            return value;
        }


    }

    /**
     * 构建本表关联条件语句
     * */
    private ConditionExpr buildLocalQueryCondition(String tabAlias, DataPermCondition node, String queryField) {
        Object[] values=this.getVariableValues(node);
        ExprType exprType=node.getExprType();
        return buildConditionExpr(tabAlias,exprType,queryField,values);
    }

    private ConditionExpr buildConditionExpr(String tabAlias, ExprType exprType, String queryField,Object[] values) {

        if(values==null || values.length < exprType.minVars()) {
            throw new DataPermException("缺少参数");
        }

        if(exprType.simple()) {

            if(exprType.minVars()==0 && exprType.maxVars()==0) {
                //没有参数： is null 和 is not null
                return new ConditionExpr(tabAlias + "." + queryField + " " + exprType.operator());
            } else if(exprType.minVars()==1 && exprType.maxVars()==1) {
                //只取第一个参数
                return new ConditionExpr(tabAlias + "." + queryField + " " + exprType.operator() + " ?", values);
            } else {
                throw new DataPermException("不支持");
            }

        } else {

            if(exprType==ExprType.like) {
                return new ConditionExpr(tabAlias+"."+queryField + " like ?","%"+values[0]+"%");
            } else if(exprType==ExprType.like_left) {
                return new ConditionExpr(tabAlias+"."+queryField + " like ?",values[0]+"%");
            } else if(exprType==ExprType.like_right) {
                return new ConditionExpr(tabAlias+"."+queryField + " like ?","%"+values[0]);
            } else if(exprType==ExprType.like_not) {
                return new ConditionExpr(tabAlias+"."+queryField + " not like ?","%"+values[0]+"%");
            } else if(exprType==ExprType.like_left_not) {
                return new ConditionExpr(tabAlias+"."+queryField + " not like ?",values[0]+"%");
            } else if(exprType==ExprType.like_right_not) {
                return new ConditionExpr(tabAlias+"."+queryField + " not like ?","%"+values[0]);
            }

            else if(exprType==ExprType.btw) {
                return new ConditionExpr(tabAlias+"."+queryField + " between ? and ?",values[0],values[1]);
            }
            //
            else if(exprType==ExprType.in) {
                values=flatten(values);
                In in=new In(tabAlias+"."+queryField,values);
                return new ConditionExpr(in.getListParameterSQL(),in.getListParameters());
            } else if(exprType==ExprType.in_not) {
                values=flatten(values);
                In in=new In(tabAlias+"."+queryField,values);
                in.not();
                return new ConditionExpr(in.getListParameterSQL(),in.getListParameters());
            }
            else {
                throw new DataPermException("不支持");
            }

        }
    }

    private Object[] flatten(Object[] values) {
        ArrayList ps=new ArrayList();
        for (Object value : values) {
            if(value.getClass().isArray()) {
                Object[] els=(Object[]) value;
                for (Object el : els) {
                    ps.add(el);
                }
            } else if (value instanceof  List) {
                List els=(List) value;
                for (Object el : els) {
                    ps.add(el);
                }
            } else if (value instanceof Set) {
                Set els=(Set) value;
                for (Object el : els) {
                    ps.add(el);
                }
            }
        }
        return ps.toArray();
    }


    /**
     * 构建 exists 语句
     * */
    private Expr buildQueryExists(DataPermCondition node, PropertyRoute route, List<String> fillBys, String queryTable, String queryField, Object[] values) {


        RelationSolver relationSolver=dao.getRelationSolver();
        JoinResult jr=new JoinResult();
        Class targetType=route.getSlavePoType();

        //通过配置的关联关系获得 join 好的语句
        QueryBuildResult result=relationSolver.buildJoinStatement(jr,poType,null,null,route,targetType,false);
        Expr expr=result.getExpr();

        //获得别名，因为别名 map 的制约这使得有限制，相同表不能代表不同的业务主体出现，这个后期再行解决
        Map<String,String> alias=result.getTableAlias();

        Join firstJoin= (Join) route.getJoins().get(0);
        Join lastJoin= (Join) route.getJoins().get(route.getJoins().size()-1);
        DBField[] sourceFields=lastJoin.getMasterFields();
        DBField[] targetFields=lastJoin.getSlaveFields();
        String joinTableAlias=alias.get(lastJoin.getSlaveTable());
        String targetTableAlias=alias.get(firstJoin.getSlaveTable());

        //判断字段有效性
        Where where = null;

        //检测字段，并调整字段的真实名称
        DBTableMeta tm = dao.getTableMeta(firstJoin.getSlaveTable());
        DBColumnMeta cm = tm.getColumn(queryField);
        if (cm == null) {
            queryField= BeanNameUtil.instance().depart(queryField);
            cm = tm.getColumn(queryField);
        }
        if (cm == null) {
            throw new IllegalArgumentException("字段 " + firstJoin.getSlaveTable() + "." + queryField + "不存在");
        }

        //设置关联条件
        where=new Where();
        for (int i = 0; i < sourceFields.length; i++) {
            where.and(tabAlias+"."+sourceFields[i].name()+" = "+joinTableAlias+"."+targetFields[i].name());
        }

        ExprType exprType=node.getExprType();
//        String ce=null;
//        //单个参数
//        if(exprType.minVars()==1 && exprType.maxVars()==1) {
//            ce=targetTableAlias+"."+queryField + " " + node.getExprType().operator() + " ?";
//        } else {
//            throw new RuntimeException("暂不支持");
//        }
        ConditionExpr conditionExpr=buildConditionExpr(targetTableAlias,exprType,queryField,values);
        where.and(conditionExpr);

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

    public ConditionExpr buildDataPermLocalCondition(String searchTable, String tabAlias,String searchField, DataPermCondition dataPermCondition) {
        return buildLocalQueryCondition(tabAlias,dataPermCondition,searchField);
    }



}
