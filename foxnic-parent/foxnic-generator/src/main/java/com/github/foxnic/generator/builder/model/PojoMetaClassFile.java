package com.github.foxnic.generator.builder.model;

import com.github.foxnic.api.bean.BeanProperty;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.*;

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

		this.addImport(BeanProperty.class);

		String extendstr="";
		if(this.getSuperTypeSimpleName()!=null)  {
			extendstr=" extends "+this.getSuperTypeSimpleName();
			this.addImport(this.getSuperTypeFullName());
		}

		code.ln("public class "+this.getSimpleName() +extendstr+" {");
		List<PojoProperty> allProps=new ArrayList<>();
		allProps.addAll(this.pojoClassFile.getProperties());
		allProps.addAll(pojoClassFile.getSuperProperties());
		Set<String> names=new HashSet<>();
		List<String> all= new  ArrayList<>();
		for (PojoProperty p : allProps) {
			if(names.contains(p.name())) continue;
			names.add(p.name());
			addJavaDoc(1,p.getJavaDocInfo());
			code.ln(1,"public static final String "+p.getNameConstants()+"=\""+p.name()+"\";");
			all.add(p.getNameConstants());

			String typeName=Object.class.getName();

			if(p.catalogName().equals("LIST")) {
				typeName=List.class.getName();
			} else if(p.catalogName().equals("MAP")) {
				typeName= Map.class.getName();
			} else if(p.catalogName().equals("SIMPLE")) {
				typeName= p.getTypeFullName();
			}

			String cpTypeName=null;
			if(p.type()!=null) {
				cpTypeName=p.type().getName();
			}
			if(cpTypeName==null) {
				cpTypeName=p.type().getName();
			}


			addJavaDoc(1,p.getJavaDocInfo());
			code.ln(1,"public static final BeanProperty<"+pojoClassFile.getFullName()+","+p.type().getName()+"> "+p.getNameConstants()+"_PROP = new BeanProperty("+pojoClassFile.getFullName()+".class ,"+p.getNameConstants()+", "+typeName+".class, \""+p.label()+"\", \""+p.note()+"\", "+p.getTypeFullName()+".class, "+(p.keyType()==null?null:p.keyType().getName()+".class")+");");
		}

		addJavaDoc(1,"全部属性清单");
		code.ln(1,"public static final String[] $PROPS={ "+StringUtil.join(all," , ")+" };");

//		if(this.pojoClassFile.getSuperTypeSimpleName()!=null) {
		if(this.pojoClassFile.isExtendsEntity()) {
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
		Set<String> names=new HashSet<>();
		List<PojoProperty> props= pojoClassFile.getProperties();
		for (PojoProperty p : props) {
			p.getSetterCode(0);
			code.append(p.getSetterCode4Proxy(2,this));
			names.add(p.name());
		}

		props= pojoClassFile.getSuperProperties();
		for (PojoProperty p : props) {
			if(names.contains(p.name())) continue;
			p.getSetterCode(0);
			code.append(p.getSetterCode4Proxy(2,this));
			names.add(p.name());
		}

		code.ln(1,"}");
		return code;
	}

	@Override
	public void save(boolean override) {
		if(this.context.getSettings().isRebuildEntity()) {
			override=true;
		} else {
			override=this.pojoClassFile.isSignatureChanged();
		}
		super.save(override);
	}

}
