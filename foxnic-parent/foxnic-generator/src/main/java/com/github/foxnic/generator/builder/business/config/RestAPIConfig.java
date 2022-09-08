package com.github.foxnic.generator.builder.business.config;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.builder.model.PojoProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestAPIConfig {

    private String title;
    private String name;

    private String path;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private RequestMethod method;

    public RestAPIConfig(String name) {
        this.name=name;
    }

    private List<PojoProperty> parameters=new ArrayList<>();
    private Map<String,ExtraParameterInfo> extraParameterInfoMap=new HashMap<>();

    public List<PojoProperty> getParameters() {
        return parameters;
    }

    public void addParameter(PojoProperty param) {
        parameters.add(param);
    }

    public void setParameterExtras(String name, boolean required, String sample) {
        ExtraParameterInfo info=extraParameterInfoMap.get(name);
        if(info==null) {
            info=new ExtraParameterInfo();
            extraParameterInfoMap.put(name,info);
        }
        info.setRequired(required);
        info.setExample(sample);
    }


    public String getResultType() {
        return resultType;
    }



    public String getParameterListString() {
        List<String> list=new ArrayList<>();
        for (PojoProperty p : parameters) {
            list.add(p.getTypeName() +" "+ p.name());
        }
        return StringUtil.join(list," , ");
    }

    public String getJavaDocParameterList() {
        CodeBuilder code=new CodeBuilder("    ");
        for (PojoProperty p : parameters) {
            String note=p.note();
            if(p.label().equals(note)) {
                note="";
            } else {
                note=" , "+note;
            }
            code.ln(1,"  * @param "+p.name()+" "+p.label()+note);
        }
        return code.toString().trim();
    }



    public String getSwaggerParameterList() {
        CodeBuilder code=new CodeBuilder("    ");
        code.ln(1,"@ApiImplicitParams({");
        for (PojoProperty p : parameters) {
            String note=p.note();
            if(p.label().equals(note)) {
                note="";
            } else {
                note=" , "+note;
            }
            ExtraParameterInfo info=extraParameterInfoMap.get(p.name());
            String example="";
            if(StringUtil.hasContent(info.getExample())) {
                 String ex=info.getExample();
                 ex=ex.replace("\"","\\\"");
                example=" , example = \""+ex+"\"";
            }
            code.ln(2,"@ApiImplicitParam(name = \""+p.name()+"\" , value = \""+p.label()+"\" , required = "+info.isRequired()+" , dataTypeClass="+p.getTypeName()+".class"+example+"),");
        }
        code.ln(1,"})");
        return code.toString().trim();
    }

    public void appendImport4Parameters(TemplateJavaFile file) {
        for (PojoProperty p : parameters) {
            file.addImport(p.getTypeFullName());
        }
    }


    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public Class getResultDataType() {
        return resultDataType;
    }

    public void setResultDataType(Class resultDataType) {
        this.resultDataType = resultDataType;
    }

    public Class getResultKeyType() {
        return resultKeyType;
    }

    public void setResultKeyType(Class resultKeyType) {
        this.resultKeyType = resultKeyType;
    }

    public Class getResultElType() {
        return resultElType;
    }

    public void setResultElType(Class resultElType) {
        this.resultElType = resultElType;
    }

    private String resultType;

    private String resultDesc;
    private Class resultDataType;

    private Class resultKeyType;
    private Class resultElType;

    public void appendImport4Result(TemplateJavaFile file) {

        if("simple".equals(resultType)) {
           file.addImport(resultDataType);
        } else if("list".equals(resultType)) {
            file.addImport(List.class);
            file.addImport(resultElType);
        } else if("pagedList".equals(resultType)) {
            file.addImport(PagedList.class);
            file.addImport(resultElType);
        } else if("map".equals(resultType)) {
            file.addImport(Map.class);
            file.addImport(resultKeyType);
            file.addImport(resultElType);
        } else {

        }
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public String getHtmlGenericResultCode() {
        String x=this.getGenericResultCode();
        x=x.replaceAll("<","&lt;");
        x=x.replaceAll(">","&gt;");
        return x;
    }

    public String getGenericResultCode() {
        if("simple".equals(resultType)) {
            return "<"+resultDataType.getSimpleName()+">";
        } else if("list".equals(resultType)) {
            return "<List<"+resultElType.getSimpleName()+">>";
        } else if("pagedList".equals(resultType)) {
            return "<PagedList<"+resultElType.getSimpleName()+">>";
        } else if("map".equals(resultType)) {
            return "<Map<"+resultKeyType.getSimpleName()+","+resultElType.getSimpleName()+">>";
        } else {
            return "";
        }
    }

    public String getGenericResultCode4New() {
        if("simple".equals(resultType)) {
            return "<>";
        } else if("list".equals(resultType)) {
            return "<>";
        } else if("map".equals(resultType)) {
            return "<>";
        } else {
            return "";
        }
    }




    public String getConstName() {
        return this.path.replace("-","_").toUpperCase();
    }



    public Class getMappingType() {
        if(method==RequestMethod.GET) {
            return GetMapping.class;
        } else if(method==RequestMethod.POST) {
            return PostMapping.class;
        } else if(method==RequestMethod.PUT) {
            return PutMapping.class;
        } else  {
            throw new IllegalArgumentException("不支持");
        }
    }



    public String getMappingTypeName() {
        return getMappingType().getSimpleName();
    }



    public static class ExtraParameterInfo {

        private boolean required = false;
        private  String example = null;

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getExample() {
            return example;
        }

        public void setExample(String example) {
            this.example = example;
        }



    }


}
