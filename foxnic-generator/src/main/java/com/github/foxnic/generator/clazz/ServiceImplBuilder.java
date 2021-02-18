package com.github.foxnic.generator.clazz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		
		
		this.addImport(Autowired.class);
		this.addImport(ctx.getMapperFullName());
		code.ln(1,"");
		code.ln(1,"@Autowired");
		code.ln(1,"private "+ctx.getMapperName()+" "+ctx.getMapperVarName()+"=null;");
		
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildServiceImplMethod(this,ctx,code);
		}
		
		code.ln("");
	 
		code.ln("}");
	
	}
	
 
 


	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getProjectDirName(),ctx.getImplFullName());
	}
}
