package com.github.foxnic.generator.clazz;

import com.github.foxnic.dao.entity.ISuperService;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;
public class ServiceInterfaceBuilder extends FileBuilder {
	
	 
	
 
	public ServiceInterfaceBuilder(Context cfg) {
		 super(cfg);
	}
	
	
	public void build() {
		
		code.ln("package "+ctx.getIntfPackage()+";");
		//加入注释
		code.ln("/**");
		code.ln(" * <p>");
		code.ln(" * "+ctx.getTableMeta().getComments()+" 服务接口");
		code.ln(" * </p>");
		this.appendAuthorAndTime();
		code.ln("*/");
		code.ln("");
		
 
		code.ln("public interface "+ctx.getIntfName()+" extends ISuperService<"+ctx.getPoName()+"> {");
		this.addImport(ISuperService.class);
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得实体对应的数据表");
		code.ln(1," */");
		code.ln(1,"default String table() {return "+ctx.getPoMetaName()+".TABLE_NAME;}");
		this.addImport(ctx.getPoMetaFullName());
		
		
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildServiceInterfaceMethod(this, ctx, code);
		}
 
		code.ln("");
	 
		code.ln("}");
	
	}
 
	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getServiceProject().getMainSourceDir(),ctx.getIntfFullName());
	}
 
}
