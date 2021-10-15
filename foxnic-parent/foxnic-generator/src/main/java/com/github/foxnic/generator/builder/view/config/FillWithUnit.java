package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class FillWithUnit {

    private String code;
    private List<String> imports=new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void addImport(String imp) {
        if(imports.contains(imp)) return;
        imports.add(imp);
    }

    public List<String> getImports() {
        return imports;
    }


}
