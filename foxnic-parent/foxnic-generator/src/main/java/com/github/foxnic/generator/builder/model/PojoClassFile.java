package com.github.foxnic.generator.builder.model;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBField;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PojoClassFile extends ModelClassFile {
 
	public static final DefaultNameConvertor nameConvertor=new DefaultNameConvertor(false);
 
	protected List<PojoProperty> properties=new ArrayList<>();
	
	private String doc;


	/**
	 * 添加一个非集合类型的简单属性
	 * @param type 类型
	 * @param name 参数名称，建议使用驼峰命名，如 orderId
	 * @param  label 属性标签
	 * @param  note  属性详细说明
	 * */
	public void addSimpleProperty(Class type,String name,String label,String note) {
		this.addProperty(PojoProperty.simple(type, name, label, note));
	}

	/**
	 * 添加一个List类型的属性
	 * @param type List内部元素的类型
	 * @param name 参数名称，建议使用驼峰命名，如 orderId
	 * @param  label 属性标签
	 * @param  note  属性详细说明
	 * */
	public void addListProperty(Class type,String name,String label,String note) {
		this.addProperty(PojoProperty.list(type, name, label, note));
	}
	
	public void addListProperty(JavaClassFile type,String name,String label,String note) {
		this.addProperty(PojoProperty.list(type, name, label, note));
	}

	/**
	 * 设置属性投影
	 * */
	public void shadow(DBField type, Class<? extends CodeTextEnum> menuTypeClass, String propName) {
		throw new RuntimeException("待实现");
	}

	/**
	 * 添加一个Map类型的属性
	 * @param keyType Map Key 的类型
	 * @param type Map内部元素的类型
	 * @param name 参数名称，建议使用驼峰命名，如 orderId
	 * @param  label 属性标签
	 * @param  note  属性详细说明
	 * */
	public void addMapProperty(Class keyType,Class type,String name,String label,String note) {
		this.addProperty(PojoProperty.map(keyType, type, name, label, note));
	}
	
	
	public void addProperty(PojoProperty prop) {
		properties.add(prop);
		prop.setClassFile(this);
	}
 
	public PojoClassFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName);
		this.setDoc(context.getTopic());
	}
 
	
 
	@Override
	protected void buildBody() {
		
		buildClassJavaDoc();
		
		buildClassStartPart();
		
		buildProperties();
		
		buildGetterAndSetter();
		
		buildOthers();
		
		buildClassEndPart();

	}

	

	protected void buildOthers() {
		 
		
	}

	protected void buildClassStartPart() {
		code.ln("public class "+this.getSimpleName()+(this.getSuperTypeSimpleName()==null?"":(" extends "+this.getSuperTypeSimpleName()))+" {");
		
		code.ln("");
		code.ln(1,"private static final long serialVersionUID = 1L;");
		
	}

	protected void buildClassJavaDoc() {
		 
		//加入注释
		code.ln("/**");
		code.ln(" * "+this.getDoc());
		code.ln(" * @author "+this.context.getSettings().getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" * @sign "+this.getSign());
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构或配置发生变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
		
	}
	
	protected void buildClassEndPart() {
		code.ln("}");
	}

	private void buildGetterAndSetter() {
		for (PojoProperty prop : properties) {
			this.code.append(prop.getGetterCode(1));
			this.code.append(prop.getSetterCode(1));
		}
	}
	
	private void buildProperties() {
		for (PojoProperty prop : properties) {
			this.code.append(prop.getDefineCode(1));
		}
	}
	
	public String getSign() {
		String sign=this.getSuperTypeSimpleName()+"|"+this.getDoc()+"|";
		for (PojoProperty prop : properties) {
			sign+=prop.getSign()+",";
		}
		return MD5Util.encrypt32(sign);
	}
	
	@Override
	public void save(boolean override) {
		if(this.context.getSettings().isRebuildEntity()) {
			override=true;
		} else {
			override=isSignatureChanged();
		}
		super.save(override);
	}
	
	private Boolean isSignatureChanged=null;
	/**
	 * 判断签名是否变化
	 * */
	public boolean isSignatureChanged() {
		if(isSignatureChanged!=null) return isSignatureChanged;
		File sourceFile=this.getSourceFile();
		String sign=this.getSign();
		if(!sourceFile.exists())  return true;
		String s="";
		String str=FileUtil.readText(sourceFile);
		String[] lns=str.split("\\n");
		for (String ln : lns) {
			ln=ln.trim();
			if(ln.startsWith("* @sign ")) {
				s=ln.substring(8);
				break;
			}
		}
		isSignatureChanged = !s.equals(sign);
		return isSignatureChanged;
	}

	public List<PojoProperty> getProperties() {
		return properties;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}
 

}
