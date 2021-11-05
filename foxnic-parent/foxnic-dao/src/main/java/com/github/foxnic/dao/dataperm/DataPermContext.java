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

    public Result<Boolean> testExpr(String expr) {
        Result<Boolean> r=new Result<>();
        EvaluationContext ctx = new StandardEvaluationContext(this);
        try {

            Object result = parser.parseExpression(expr).getValue(ctx);
            if(result instanceof Boolean) {
                r.success(true).message("通过").data((Boolean)result);
                return r;
            } else {
                r.success(false).message("要求返回 Boolean 类型，实际返回 "+result.getClass().getSimpleName()+" 类型");
                return r;
            }
        } catch (Exception e) {
            return ErrorDesc.exception(e);
        }

    }



}
