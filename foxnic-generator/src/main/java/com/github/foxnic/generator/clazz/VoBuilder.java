package com.github.foxnic.generator.clazz;

import java.util.Map;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.MapUtil;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.sql.meta.DBDataType;

public class VoBuilder extends FileBuilder {

	public VoBuilder(Context cfg) {
		super(cfg);
	}
 
	private Map<String,String> integerProps=MapUtil.asMap("page_index","页码","page_size","分页大小");
 
	public void build() {
		
		code.ln("package "+ctx.getVoPackage()+";");
		code.ln("");
		code.ln("import lombok.Data;");
		code.ln("import "+ctx.getPoFullName()+";");
		code.ln("import org.springframework.beans.BeanUtils;");
		code.ln("import java.beans.Transient;");
		code.ln("");
		//加入注释
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln("*/");
		code.ln("");
		
		code.ln("@Data");
		code.ln("public class "+ctx.getVoName()+" extends "+ctx.getPoName()+" {");
		
		code.ln("");
		code.ln(1,"private static final long serialVersionUID = 1L;");
 
		
		for (String prop : integerProps.keySet()) {
			buildProperty(prop,Integer.class,integerProps.get(prop));
		}
		
		for (String prop : integerProps.keySet()) {
			buildGetter(prop,Integer.class,DBDataType.INTEGER,integerProps.get(prop));
		}
		
		for (String prop : integerProps.keySet()) {
			buildSetter(prop,Integer.class,DBDataType.INTEGER,integerProps.get(prop));
		}
		
		String prop=ctx.getPoVarName();
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 "+ctx.getPoName()+" 转换成 "+ctx.getVoName());
		code.ln(1," * @param "+prop+" "+ctx.getPoName()+" 对象");
		code.ln(1," * @return "+ctx.getVoName()+" , 转换好的的 "+ctx.getVoName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+ctx.getVoName()+" parse("+ctx.getPoName()+" "+prop+") {");
		code.ln(2,"if("+prop+"==null) return null;");
		code.ln(2,ctx.getVoName()+" vo=new "+ctx.getVoName()+"();");
		code.ln(2,"BeanUtils.copyProperties("+prop+", vo);");
		code.ln(2,"return vo;");
		code.ln(1,"}");
		
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Map 转换成 "+ctx.getVoName()); 
		code.ln(1," * @param "+prop+"Map 包含实体信息的 Map 对象");
		code.ln(1," * @return "+ctx.getVoName()+" , 转换好的的 "+ctx.getVoName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+ctx.getVoName()+" parse(Map<String,Object> "+prop+"Map) {");
		code.ln(2,"if("+prop+"Map==null) return null;");
		code.ln(2,ctx.getVoName()+" vo=new "+ctx.getVoName()+"();");
		code.ln(2,"for (Entry<String,Object> e : "+prop+"Map.entrySet()) {");
		code.ln(3,"BeanUtil.setFieldValue(vo, e.getKey(), e.getValue());");
		code.ln(2,"}");
		code.ln(2,"return vo;");
		code.ln(1,"}");
		
		this.addImport("java.util.Map.Entry");
		this.addImport(Map.class);
		this.addImport(BeanUtil.class);
		
		code.ln("}");
	 
	}
	
	private void buildProperty(String prop, Class<Integer> type,String comment) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+comment);
		code.ln(1,"*/");
		code.ln(1,"@ApiModelProperty(value=\""+comment+"\" , dataType=\""+type.getName()+"\")");
		code.ln(1, "private "+type.getSimpleName()+" "+convertor.getPropertyName(prop)+" = null;");
		this.addImport(ClassNames.ApiModelProperty);
		
	}
	
	
	private void buildGetter(String prop, Class<Integer> type,DBDataType dbType,String comment) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 "+comment);
		code.ln(1," * @return "+comment);
		code.ln(1,"*/");
		code.ln(1, "public "+type.getSimpleName()+" "+convertor.getGetMethodName(prop, dbType) +"() {");
		code.ln(2,"return this."+convertor.getPropertyName(prop)+";");
		code.ln(1, "}");
	}
	
	private void buildSetter(String prop, Class<Integer> type,DBDataType dbType,String comment) {
		String field=convertor.getPropertyName(prop);
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 设置 "+comment);
		code.ln(1," * @param "+field+" "+comment);
		code.ln(1,"*/");
		code.ln(1, "public void "+convertor.getSetMethodName(prop, dbType) +"("+type.getSimpleName()+" "+field+") {");
		code.ln(2,"this."+field+"="+field+";");
		code.ln(1, "}");
	}
 
	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava("framework-domain",ctx.getVoFullName());
	}
}
