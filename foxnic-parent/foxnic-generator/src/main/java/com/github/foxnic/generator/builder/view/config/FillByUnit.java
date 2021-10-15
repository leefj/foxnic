package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class FillByUnit {
    private List<String> paramVars=new ArrayList<>();
    private List<String> imports=new ArrayList<>();
    public int size() {
        return paramVars.size();
    }
    public void add(String paramVar,String imp) {
        paramVars.add(paramVar);
        imports.add(imp);
    }
    public List<String> getParamVars() {
        return paramVars;
    }

    public List<String> getImports() {
        return imports;
    }

    public String getKey() {
        if(paramVars.isEmpty()) return null;
        else if(paramVars.size()==1) return paramVars.get(0).toLowerCase();
        else if(paramVars.size()>1) {
            List<String> ps=paramVars.subList(0,paramVars.size()-1);
            return StringUtil.join(ps).toLowerCase();
        }
        return null;
    }

    /**
     * 只有 paramVars 长度大于1的才会调用此方法
     * */
    public String getArgs() {
        List<String> ps=paramVars.subList(0,paramVars.size()-1);
        return StringUtil.join(ps);
    }
}
