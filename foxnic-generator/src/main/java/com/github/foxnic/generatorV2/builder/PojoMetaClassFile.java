package com.github.foxnic.generatorV2.builder;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;

public class PojoMetaClassFile extends ModuleClassFile {

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
		
		code.ln("public class "+this.getSimpleName() +" {");
		List<String> all= new  ArrayList<>();
		for (PojoProperty p : this.pojoClassFile.getProperties()) {
			addJavaDoc(1,p.getJavaDocInfo());
			code.ln(1,"public static final String "+p.getNameConstants()+"=\""+p.name()+"\";");
			all.add(p.getNameConstants());
		}
		
		addJavaDoc(1,"全部属性清单");
		code.ln(1,"public static final String[] $PROPS={ "+StringUtil.join(all," , ")+" };");
		code.ln("}");
	}
	
	
	@Override
	public void save(boolean override) {
		override=this.pojoClassFile.isSignatureChanged();
		super.save(override);
	}

}
