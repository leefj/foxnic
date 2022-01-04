package com.github.foxnic.dao.dataperm;

import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.entity.Entity;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DataPermContext {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private Object vo;
    private Object session;
    private Object env;
    private Object global=null;
    private Object local=null;

    private Class<? extends Entity> poType;

    public DataPermContext(Class<? extends Entity> poType) {
        this.poType=poType;
    }

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

    public Object getGlobal() {
        return global;
    }

    public Object getLocal() {
        return local;
    }

    public Result<Boolean> testConditionExpr(String expr) {
        Result<Boolean> r=new Result<>();
        initExtraContext();
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

    public void initExtraContext() {
        DataPermManager dataPermManager=DataPermManager.getInstance();
        this.global= createBean(dataPermManager.getGlobalContexts());
        this.local= createBean(dataPermManager.getLocalContexts(this.poType));
    }

    public Object getVariableValue(String expr) {
        initExtraContext();
        EvaluationContext ctx = new StandardEvaluationContext(this);
        try {
            return parser.parseExpression(expr).getValue(ctx);
        } catch (Exception e) {
            DataPermException ex=new DataPermException("表达式:"+expr+"转换错误",e);
            Logger.exception(ex);
            throw ex;
        }
    }


    private static Object createBean(Map properties) {
        BeanGenerator generator = new BeanGenerator();
        Set keySet = properties.keySet();
        String key = null;
        Object value = null;
        for(Iterator i = keySet.iterator(); i.hasNext();) {
            key = (String)i.next();
            value=properties.get(key);
            Class type=Object.class;
            if(value!=null) {
                type=value.getClass();
            }
            generator.addProperty(key, type);
        }
        Object bean=generator.create();
        // 设置属性值
        for(Iterator i = keySet.iterator(); i.hasNext();) {
            key = (String) i.next();
            value = properties.get(key);
            BeanUtil.setFieldValue(bean,key,value);
        }
        return bean;
    }

}
