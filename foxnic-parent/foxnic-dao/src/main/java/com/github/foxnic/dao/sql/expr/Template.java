package com.github.foxnic.dao.sql.expr;

import com.github.foxnic.dao.sql.loader.SQLoader;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.expr.SubSQL;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Template extends SubSQL implements SQL {



    private Expr expr=new Expr();
    private String stmt;
    private Map<String,Object> vars=new LinkedHashMap<>();

    public Template(String stmt) {
        this.stmt=stmt;
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

    public Template build() {

        if(isBuilt) return this;

        isBuilt=true;

        Map<String,Object> ps=new HashMap<>();
        Map<String,Object> finalVars=new HashMap<>();
        for (Map.Entry<String,Object> e : vars.entrySet()) {
            if(e.getValue() instanceof SubSQL) {
                SubSQL sql=(SubSQL) e.getValue();
                sql.setParent(null);
                sql.setNameBeginIndex(ps.size());
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
