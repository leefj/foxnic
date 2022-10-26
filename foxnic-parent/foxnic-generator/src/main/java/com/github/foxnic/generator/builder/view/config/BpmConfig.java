package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.config.ModuleContext;

public class BpmConfig {

    /***
     * 流程引擎的集成方式
     * */
    private String integrateMode = "none";

    private String formCode=null;

    private ModuleContext context;

    public  BpmConfig (ModuleContext context) {
        this.context = context ;
    }

    public String getIntegrateMode() {
        return integrateMode;
    }

    public void setIntegrateMode(String integrateMode) {
        this.integrateMode = integrateMode;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        DAO dao = context.getDAO();
        RcdSet rs=dao.query("select * from bpm_form_definition where valid=1 and deleted=0");
        if(rs.isEmpty()) {
            throw new IllegalArgumentException(formCode+" 不是一个有效的表单代码");
        }
        this.formCode = formCode;
    }

    public void reset() {

    }
}
