package com.github.foxnic.dao.sql.expr;

import com.github.foxnic.dao.sql.loader.SQLoader;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.expr.SubSQL;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL 模版，模版语法使用 Enjoy  <br>
 *  文档 https://jfinal.com/doc/6-1
 * */
public class Template extends SubSQL implements SQL {



    private Expr expr=new Expr();
    private String stmt;
    private Map<String,Object> vars=new LinkedHashMap<>();
    private Object[] arrayParameters;
    private Map<String,Object> mapParameters;

    public Template(String stmt,Object... ps) {
        this.stmt=stmt;
        this.arrayParameters=ps;
    }

    public Template(String stmt,Map<String,Object> ps) {
        this.stmt=stmt;
        this.mapParameters=ps;
    }

    public Template put(String key,String subsql) {
        vars.put(key,subsql);
        return this;
    }

    public Template put(String key,SQL subsql) {
        vars.put(key,subsql);
        return this;
    }

    private boolean isBuilt=false;

    /**
     * 构建
     * */
    public Template build() {

        if(isBuilt) return this;

        isBuilt=true;

        Map<String,Object> ps=new HashMap<>();
        int offset=0;
        Expr orignal=null;
        if(arrayParameters!=null) {
            orignal=new Expr(stmt,arrayParameters);
            stmt=orignal.getNamedParameterSQL();
            ps.putAll(orignal.getNamedParameters());
        }
        if(mapParameters!=null) {
            ps.putAll(mapParameters);
        }

        offset=ps.size();


        Map<String,Object> finalVars=new HashMap<>();
        for (Map.Entry<String,Object> e : vars.entrySet()) {
            if(e.getValue() instanceof SubSQL) {
                SubSQL sql=(SubSQL) e.getValue();
                sql.setParent(null);
                sql.setNameBeginIndex(ps.size()+offset);
                ps.putAll(sql.getNamedParameters());
                finalVars.put(e.getKey(),sql.getNamedParameterSQL());
            } else {
                finalVars.put(e.getKey(),e.getValue());
            }
        }

        String str= SQLoader.render(stmt,vars);
        this.expr.append(new Expr(str,ps));
        return this;
    }


    @Override
    public String getSQL(SQLDialect dialect) {
        build();
        return this.expr.getSQL(dialect);
    }

    @Override
    public Object[] getListParameters() {
        build();
        return this.expr.getListParameters();
    }

    @Override
    public Map<String, Object> getNamedParameters() {
        build();
        return this.expr.getNamedParameters();
    }

    @Override
    public boolean isEmpty() {
        build();
        return this.expr.isEmpty();
    }

    @Override
    public boolean isAllParamsEmpty() {
        build();
        return this.expr.isAllParamsEmpty();
    }
}
