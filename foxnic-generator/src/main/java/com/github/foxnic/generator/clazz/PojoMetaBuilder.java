package com.github.foxnic.generator.clazz;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;

import java.io.File;

public class PojoMetaBuilder extends FileBuilder {

	private Pojo pojo;
	
	private String sign=null;
	
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
		this.sign=pojo.getSignature();
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln(" * @sign "+sign);
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
		String sup=pojo.getMetaSuperClass();
		if(sup!=null && sup.startsWith("null.")) {
			sup=pojo.getMetaSuperClass();
		}
		
		if(sup==null) {
			sup=ctx.getPoMetaFullName();
		}

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
		//如果模型变化，则覆盖原始文件；否则不处理
		if(PoBuilder.isSignatureChanged(sourceFile,this.sign)) {
			return sourceFile;
		} else {
			return null;
		}
	}

}
