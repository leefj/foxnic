package com.github.foxnic.generatorV2.builder.constants;

import java.io.File;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generatorV2.config.EnumConfig;

public class EnumClassFile extends JavaClassFile {

	private EnumConfig enumInfo;
	private DAO dao;
	
	public EnumClassFile(DAO dao,MavenProject domainProject,EnumConfig enumInfo,String domainConstsPackage,String enumClassName) {
		super(domainProject, domainConstsPackage+".enums", enumClassName);
		this.dao=dao;
		this.enumInfo=enumInfo;
	}
 
	protected void buildBody() {
 
		//加入注释
		code.ln("/**");
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" * 从 "+enumInfo.getSelect()+" 生成");
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成");
		code.ln("*/");
		code.ln("");
 
		code.ln("public enum "+this.getSimpleName()+" {");
		
		RcdSet rs=dao.query(enumInfo.getSelect());
		for (Rcd r : rs) {
			String origName=r.getString(enumInfo.getCodeField());
			String name=origName.replace('.', '_');
			String text=r.getString(enumInfo.getTextField());
			addJavaDoc(1,text);
			code.ln(1,name.trim().toUpperCase()+"(\""+origName+"\" , \""+text+"\"),");
		}
		code.ln(1,";");
		
		code.ln(1,"");
		code.ln(1,"private String code;");
		code.ln(1,"private String text;");
		code.ln(1,"private "+this.getSimpleName()+"(String code,String text)  {");
		code.ln(2,"this.code=code;");
		code.ln(2,"this.text=text;");
		code.ln(1,"}");
		
		code.ln(1,"");
		code.ln(1,"public String code() {");
		code.ln(2,"return code;");
		code.ln(1,"}");
		 
		code.ln(1,"");
		code.ln(1,"public String text() {");
		code.ln(2,"return text;");
		code.ln(1,"}");
		
		addJavaDoc(1,"从字符串转换成当前枚举类型，使用 valueOf 方法可能导致偏差，建议不要使用");
		code.ln(1,"public static "+this.getSimpleName()+" parse(String code) {");
		code.ln(2,"for ("+this.getSimpleName()+" dn : "+this.getSimpleName()+".values()) {");
		code.ln(3,"if(code.equals(dn.code())) return dn;");
		code.ln(2,"}");
		code.ln(2,"return null;");
		code.ln(1,"}");
		
		code.ln("}");
 
	}
	
 

}
