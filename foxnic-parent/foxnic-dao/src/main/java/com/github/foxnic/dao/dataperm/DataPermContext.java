package com.github.foxnic.dao.dataperm;

import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DataPermContext {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private Object vo;
    private Object session;
    private Object env;

    public Object getVo() {
        return vo;
    }

    public void setVo(Object vo) {
        this.vo = vo;
    }

    public Object getSession() {
        return session;
    }

    public void setSession(Object session) {
        this.session = session;
    }

    public Object getEnv() {
        return env;
    }

    public void setEnv(Object env) {
        this.env = env;
    }

    public Result testExpr(String expr) {

        EvaluationContext ctx = new StandardEvaluationContext(this);
        try {
            Object result = parser.parseExpression(expr).getValue(ctx);
            if(result instanceof Boolean) {
                return ErrorDesc.success().message("通过");
            }
        } catch (Exception e) {
            return ErrorDesc.exception(e);
        }

        return ErrorDesc.success();
    }



}
