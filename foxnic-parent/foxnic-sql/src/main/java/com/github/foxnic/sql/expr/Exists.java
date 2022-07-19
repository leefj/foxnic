package com.github.foxnic.sql.expr;

import com.github.foxnic.sql.dialect.SQLDialect;

import java.util.Map;

public class Exists extends SubSQL implements SQL,WhereWapper {

    private Expr select;

    public Exists(String select , Object... params) {
        this.select = new Expr(select,params);
    }

    @Override
    public String getSQL(SQLDialect dialect) {
        return "exists (  "+select.getSQL(dialect)+" )";
    }

    @Override
    public String getListParameterSQL() {
        return "exists (  "+select.getListParameterSQL()+" )";
    }

    @Override
    public Object[] getListParameters() {
        return select.getListParameters();
    }

    @Override
    public String getNamedParameterSQL() {
        return "exists (  "+select.getNamedParameterSQL()+" )";
    }

    @Override
    public Map<String, Object> getNamedParameters() {
        return select.getNamedParameters();
    }



    @Override
    public boolean isEmpty() {
        return select.isEmpty();
    }

    @Override
    public boolean isAllParamsEmpty() {
        return select.isAllParamsEmpty();
    }

    @Override
    public Exists clone() {
        Exists exists=new Exists("?",0);
        if(this.select!=null) {
            exists.select=this.select.clone();
        }
        return exists;
    }

    /**
     * 返回包含当前 Exists 语句的表达式
     * */
    public Expr toExpr() {
        Expr expr  = new Expr(this.getListParameterSQL(),this.getListParameters());
        expr.setParent(this.parent());
        expr.setNameBeginIndex(this.getNameIndexBegin());
        return expr;
    }

    /**
     * 返回包含当前 Exists 语句的条件表达式
     * */
    public ConditionExpr  toConditionExpr() {
        ConditionExpr ce=new ConditionExpr(this.getListParameterSQL(),this.getListParameters());
        ce.setParent(this.parent());
        ce.setNameBeginIndex(this.getNameIndexBegin());
        return ce;
    }
}
