package com.github.foxnic.generator.clazz;

import java.io.File;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.EnumInfo;

public class EnumBuilder extends FileBuilder {

	private EnumInfo enumInfo;
	
	public EnumBuilder(Context cfg,EnumInfo enumInfo) {
		super(cfg);
		this.enumInfo=enumInfo;
	}
	
	private String sign=null;

	@Override
	protected void build() {
		 
		code.ln("package "+ctx.getPoMetaPackage()+";");
		code.ln("");
		code.ln("");
		
		//加入注释
		this.sign=ctx.getTableMeta().getSignature(false);
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
 
		code.ln("public enum "+ctx.getEnumName()+" {");
		
		RcdSet rs=ctx.query(enumInfo.getDataTable());
		for (Rcd r : rs) {
			String name=r.getString(enumInfo.getNameField());
			name=name.replace('.', '_');
			String text=r.getString(enumInfo.getTextField());
			addJavaDoc(text);
			code.ln(1,name.trim().toUpperCase()+"(\""+name+"\" , \""+text+"\"),");
		}
		code.ln(1,";");
		
		code.ln(1,"");
		code.ln(1,"private String code;");
		code.ln(1,"private String text;");
		code.ln(1,"private "+ctx.getEnumName()+"(String code,String text)  {");
		code.ln(2,"this.code=code;");
		code.ln(2,"this.text=text;");
		code.ln(1,"}");
		
		code.ln(1,"");
		code.ln(1,"public String getCode() {");
		code.ln(2,"return code;");
		code.ln(1,"}");
		 
		code.ln(1,"");
		code.ln(1,"public String getText() {");
		code.ln(2,"return text;");
		code.ln(1,"}");
		
		code.ln(1,"");
		code.ln(1,"public static "+ctx.getEnumName()+" parse(String code) {");
		code.ln(2,"for ("+ctx.getEnumName()+" dn : "+ctx.getEnumName()+".values()) {");
		code.ln(3,"if(code.equals(dn.getCode())) return dn;");
		code.ln(2,"}");
		code.ln(2,"return null;");
		code.ln(1,"}");
		
		code.ln("}");
		
	}
	
	private void addJavaDoc(String... doc) {
 
		code.ln(1,"");
		code.ln(1,"/**");
		for (int i = 0; i <doc.length ; i++) {
			if(StringUtil.isBlank(doc[i])) continue;
			code.ln(1," * "+doc[i]+(i<doc.length?"":""));
		}
		code.ln(1,"*/");
	}

	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(),ctx.getPoEnumFullName());
	}
	
	@Override
	protected File processOverride(File sourceFile) {
		return sourceFile;
	}
	
	

}
