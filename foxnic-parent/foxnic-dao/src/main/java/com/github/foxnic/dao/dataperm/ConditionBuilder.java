package com.github.foxnic.dao.dataperm;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.api.dataperm.ConditionNodeType;
import com.github.foxnic.api.dataperm.ExprType;
import com.github.foxnic.api.dataperm.LogicType;
import com.github.foxnic.dao.dataperm.model.DataPermCondition;
import com.github.foxnic.dao.dataperm.model.DataPermRange;
import com.github.foxnic.dao.dataperm.model.DataPermVariable;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;

public class ConditionBuilder {

    private DataPermRange range;
    private String tabAlias;
    private DAO dao;
    private DataPermManager dataPermManager;

    public ConditionBuilder(DAO dao,String tabAlias, DataPermRange range) {
        this.dao=dao;
        this.range = range;
        this.tabAlias = tabAlias;
        this.range.buildTreeIf();
        this.dataPermManager=dao.getDataPermManager();
    }

    public ConditionExpr build() {
        DataPermCondition root = this.range.getRoot();
        ConditionExpr conditionExpr = this.build(root);
        return conditionExpr;
    }


    public ConditionExpr build(DataPermCondition node) {
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

    private ConditionExpr buildConditionExpr(DataPermCondition node) {
        ConditionExpr conditionExpr = null;
        String[] fillByProps = node.getQueryProperty().split("\\.");
        String[] qfs = node.getQueryField().split("\\.");
        String queryTable = qfs[0];
        String queryField = qfs[1];
        if (fillByProps.length == 1) {
            conditionExpr = buildLocalCondition(node, fillByProps[0], queryField);
        } else {

        }
        return conditionExpr;
    }

    private ConditionExpr buildLocalCondition(DataPermCondition node, String fillByProp, String queryField) {
        JSONArray vars=node.getVaribales();
        Object[] values=new Object[vars.size()];
        for (int i = 0; i < vars.size(); i++) {
            DataPermVariable variable=new DataPermVariable(dataPermManager,vars.get(i));
            values[i]=variable.getValue();
        }
        String sql = null;
        ExprType exprType=node.getExprType();
        //单个参数
        if(exprType.minVars()==1 && exprType.maxVars()==1) {
            sql=tabAlias+"."+queryField + " " + node.getExprType().operator() + " ?";
        }
        return new ConditionExpr(sql,values);
    }
}
