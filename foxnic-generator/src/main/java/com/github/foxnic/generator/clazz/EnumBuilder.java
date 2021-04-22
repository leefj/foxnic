package com.github.foxnic.generator.clazz;

import java.io.File;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.EnumInfo;

public class EnumBuilder {

	private CodeBuilder code;
	private EnumInfo enumInfo;
	private String domainEnumPackage;
	private String enumClassName;
	private MavenProject domainProject;
	private DAO dao;
	
	public EnumBuilder(DAO dao,MavenProject domainProject,EnumInfo enumInfo,String domainConstsPackage,String enumClassName) {
		this.dao=dao;
		this.enumInfo=enumInfo;
		this.domainProject=domainProject;
		this.domainEnumPackage=domainConstsPackage+".enums";
		this.enumClassName=enumClassName;
		//
		code=new CodeBuilder();
	}


 
	public void build() {
		 
		code.ln("package "+domainEnumPackage+";");
		code.ln("");
		code.ln("");
		
		//加入注释
		code.ln("/**");
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" * 从 "+enumInfo.getSelect()+" 生成");
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成");
		code.ln("*/");
		code.ln("");
 
		code.ln("public enum "+enumClassName+" {");
		
		RcdSet rs=dao.query(enumInfo.getSelect());
		for (Rcd r : rs) {
			String origName=r.getString(enumInfo.getCodeField());
			String name=origName.replace('.', '_');
			String text=r.getString(enumInfo.getTextField());
			addJavaDoc(text);
			code.ln(1,name.trim().toUpperCase()+"(\""+origName+"\" , \""+text+"\"),");
		}
		code.ln(1,";");
		
		code.ln(1,"");
		code.ln(1,"private String code;");
		code.ln(1,"private String text;");
		code.ln(1,"private "+enumClassName+"(String code,String text)  {");
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
		
		addJavaDoc("从字符串转换成当前枚举类型，使用 valueOf 方法可能导致偏差，建议不要使用");
		code.ln(1,"public static "+enumClassName+" parse(String code) {");
		code.ln(2,"for ("+enumClassName+" dn : "+enumClassName+".values()) {");
		code.ln(3,"if(code.equals(dn.getCode())) return dn;");
		code.ln(2,"}");
		code.ln(2,"return null;");
		code.ln(1,"}");
		
		code.ln("}");
		
		File file=FileUtil.resolveByPath(domainProject.getMainSourceDir(), domainEnumPackage.replace('.', '/'),enumClassName+".java");
		
		code.wirteToFile(file);
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

	 
	
	

}
