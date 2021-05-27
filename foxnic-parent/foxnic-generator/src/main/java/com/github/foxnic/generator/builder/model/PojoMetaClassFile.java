package com.github.foxnic.generator.builder.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.EntitySourceBuilder;

public class PojoMetaClassFile extends ModelClassFile {

	private PojoClassFile pojoClassFile;
	
	public PojoMetaClassFile(PojoClassFile pojoClassFile) {
		super(pojoClassFile.context,pojoClassFile.getProject(), pojoClassFile.getPackageName()+".meta", pojoClassFile.getSimpleName()+"Meta");
		this.pojoClassFile=pojoClassFile;
	}
	
	@Override
	protected void buildBody() {
		
		//加入注释
		code.ln("/**");
		code.ln(" * @author "+this.context.getSettings().getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" * @sign "+this.pojoClassFile.getSign());
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构或配置发生变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
		
		String extendstr="";
		if(this.getSuperTypeSimpleName()!=null)  {
			extendstr=" extends "+this.getSuperTypeSimpleName();
			this.addImport(this.getSuperTypeFullName());
		}
		
		code.ln("public class "+this.getSimpleName() +extendstr+" {");
		List<String> all= new  ArrayList<>();
		for (PojoProperty p : this.pojoClassFile.getProperties()) {
			addJavaDoc(1,p.getJavaDocInfo());
			code.ln(1,"public static final String "+p.getNameConstants()+"=\""+p.name()+"\";");
			all.add(p.getNameConstants());
		}
		
		addJavaDoc(1,"全部属性清单");
		code.ln(1,"public static final String[] $PROPS={ "+StringUtil.join(all," , ")+" };");
		
		if(this.pojoClassFile.getSuperTypeSimpleName()!=null) {
			addJavaDoc(1,"代理类");
			code.append(makeProxyClass());
		}
		code.ln("}");
	}
	
	
	private CodeBuilder makeProxyClass () {
		CodeBuilder code=new CodeBuilder();
		this.addImport(pojoClassFile.getFullName());
		code.ln(1,"public static class $$proxy$$ extends "+pojoClassFile.getFullName()+" {");
		code.ln("");
		code.ln(2,"private static final long serialVersionUID = 1L;");
		code.ln("");
		List<PojoProperty> props= pojoClassFile.getProperties();
		for (PojoProperty p : props) {
			p.getSetterCode(0);
			code.append(p.getSetterCode4Proxy(2,this));
		}
		code.ln(1,"}");
		return code;
	}
 
	@Override
	public void save(boolean override) {
		override=this.pojoClassFile.isSignatureChanged();
		super.save(override);
	}

}
