package com.github.foxnic.generator.clazz;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;
import com.github.foxnic.sql.meta.DBDataType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;

public class ServiceImplBuilder extends FileBuilder {
	
	 
	
 
	public ServiceImplBuilder(Context cfg) {
		 super(cfg);
	}
	
	
	public void build() {
		
		code.ln("package "+ctx.getImplPackage()+";");
 
		code.ln("");
		//加入注释
		code.ln("/**");
		code.ln(" * <p>");
		code.ln(" * "+ctx.getTableMeta().getComments()+" 服务实现类");
		code.ln(" * </p>");
		this.appendAuthorAndTime();
		code.ln("*/");
		code.ln("");
		
		
		this.addImport(Service.class);
		this.addImport(ctx.getIntfFullName());
		
		code.ln("@Service(\""+ctx.getBeanNameMainPart()+"ServiceImpl\")");
		code.ln("public class "+ctx.getImplName()+" implements "+ctx.getIntfName()+" {");
		
		
		
		
		
		
		this.addImport(Resource.class);
		code.ln(1,"");
		String daoConst=ctx.getDaoNameConst();
		if(daoConst.startsWith("\"") && daoConst.endsWith("\"")) {
			code.ln(1,"@Resource(name="+daoConst+")");
		} else {
			String[] tmp=daoConst.split("\\.");
			String c=tmp[tmp.length-2]+"."+tmp[tmp.length-1];
			String cls=daoConst.substring(0,daoConst.lastIndexOf('.'));
			this.addImport(cls);
			code.ln(1,"@Resource(name="+c+")");
		}
		code.ln(1,"private DAO dao=null;");
		this.addImport(DAO.class);
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 DAO 对象");
		code.ln(1," * */");
		code.ln(1,"public DAO dao() { return dao; }");
		
		if(!ctx.getTableMeta().hasAutoIncreaseColumn() && ctx.getTableMeta().getPKColumnCount()==1) {
			code.ln(1,"");
			code.ln(1,"/**");
			code.ln(1," * 生成主键值");
			code.ln(1," * */");
			code.ln(1,"@Override");
			code.ln(1,"public Object generateId(Field field) {");
			if(ctx.getTableMeta().getPKColumns().get(0).getDBDataType()==DBDataType.STRING) {
				code.ln(2,"return IDGenerator.getUUID();");
				this.addImport(IDGenerator.class);
			} else {
				code.ln(2,"return null;");
			}
			code.ln(1,"}");
			this.addImport(Field.class);
		}
 
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildServiceImplMethod(this,ctx,code);
		}
		
		code.ln("");
	 
		code.ln("}");
	
	}
 

	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getServiceProject().getMainSourceDir(),ctx.getImplFullName());
	}
 
}
