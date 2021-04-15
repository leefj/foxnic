package com.github.foxnic.generator.clazz;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.generator.Context;

public class PoMetaBuilder extends FileBuilder {

	public PoMetaBuilder(Context cfg) {
		super(cfg);
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
		code.ln(" * @sign "+sign);
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
		
		DBTableMeta tm=ctx.getTableMeta();
 
		code.ln("public class "+ctx.getPoMetaName()+" {");
		
//		addJavaDoc("表名",tm.getComments());
//		code.ln(1,"public static final String TABLE_NAME=\""+tm.getTableName()+"\";");
		
		List<DBColumnMeta> cms=tm.getColumns();
		for (DBColumnMeta cm : cms) {
//			addJavaDoc("字段",cm.getComment());
//			code.ln(1,"public static final String FIELD_"+cm.getColumn().toUpperCase()+"=\""+cm.getColumn()+"\";");
			
			addJavaDoc("属性名称",cm.getComment());
			code.ln(1,"public static final String "+cm.getColumn().toUpperCase()+"=\""+cm.getColumnVarName()+"\";");
		}
		
//		List<DBColumnMeta> pks=tm.getPKColumns();
//		String pkstr=pks.stream().map(pk->{return "FIELD_"+pk.getColumn().toUpperCase();}).collect(Collectors.joining(" , "," "," "));
//		addJavaDoc("主键列表");
//		code.ln(1,"public static final String[] PRIMARY_FIELDS= new String[] {"+pkstr+"};");
		
//		pkstr=pks.stream().map(pk->{return "PROP_"+pk.getColumn().toUpperCase();}).collect(Collectors.joining(" , "," "," "));
//		addJavaDoc("主键属性列表");
//		code.ln(1,"public static final String[] PRIMARY_PROPS= new String[] {"+pkstr+"};");
		
//		addJavaDoc("所有字段");
//		pkstr=cms.stream().map(cm->{return "FIELD_"+cm.getColumn().toUpperCase();}).collect(Collectors.joining(" , "," "," "));
//		code.ln(1,"public static final String[] ALL_FIELDS= new String[] {"+pkstr+"};");
		
		
//		addJavaDoc("所有属性");
//		pkstr=cms.stream().map(cm->{return "PROP_"+cm.getColumn().toUpperCase();}).collect(Collectors.joining(" , "," "," "));
//		code.ln(1,"public static final String[] ALL_PROPS= new String[] {"+pkstr+"};");
		BeanNameUtil util=new BeanNameUtil();
		List<PropertyRoute> propsJoin=ctx.getJoinProperties();
		for (PropertyRoute pr : propsJoin) {
			addJavaDoc("属性名称",pr.getLabel(),pr.getDetail());
			String name=util.depart(pr.getProperty()).toUpperCase();
			code.ln(1,"public static final String "+name+"=\""+pr.getProperty()+"\";");
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
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(),ctx.getPoMetaFullName());
	}
	
	@Override
	protected File processOverride(File sourceFile) {
		if(!sourceFile.exists()) {
			return sourceFile;
		}
		//如果模型变化，则覆盖原始文件；否则不处理
		if(PoBuilder.isSignatureChanged(sourceFile,this.sign)) {
			return sourceFile;
		} else {
			return null;
		}
	}

}
