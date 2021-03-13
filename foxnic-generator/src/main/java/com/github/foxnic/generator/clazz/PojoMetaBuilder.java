package com.github.foxnic.generator.clazz;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;

public class PojoMetaBuilder extends FileBuilder {

	private Pojo pojo;
	
	public PojoMetaBuilder(Context cfg,Pojo pojo) {
		super(cfg);
		this.pojo=pojo;
	}

	@Override
	protected void build() {
		 
		code.ln("package "+ctx.getPoMetaPackage()+";");
		code.ln("");
		code.ln("");
		
		//加入注释
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
		String sup=pojo.getMetaSuperClass();
		code.ln("public class "+pojo.getMetaName()+(sup==null?"":(" extends "+sup))+" {");
 
		for (Pojo.Property p : pojo.getProperties()) {
			
			addJavaDoc("属性名称",p.getLabel(),p.getNote());
			code.ln(1,"public static final String "+p.getNameConst()+"=\""+p.getName()+"\";");
			
		}
 
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
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(),pojo.getMetaFullName());
	}
	
	@Override
	protected File processOverride(File sourceFile) {
		//覆盖原始文件
		return sourceFile;
	}

}
