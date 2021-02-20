package com.github.foxnic.generator.clazz;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;

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
		
		code.ln("@Service");
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
		
		
//		
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
