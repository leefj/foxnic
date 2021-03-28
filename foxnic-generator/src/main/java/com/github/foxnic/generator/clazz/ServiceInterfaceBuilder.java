package com.github.foxnic.generator.clazz;

import com.github.foxnic.dao.entity.SuperService;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;

import java.io.File;
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
		
 
		code.ln("public interface "+ctx.getIntfName()+" extends SuperService<"+ctx.getPoName()+"> {");
		this.addImport(SuperService.class);
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
	
	
	@Override
	protected File processOverride(File sourceFile) {
		//如果原始文件已经存在，则不再生成
		if(sourceFile.exists()) {
			return null;
		} else {
			return sourceFile;
		}
		
//		if(ctx.isForceOverrideController()) {
//			System.err.println("!!!!!!! Force Override Service Interface("+ctx.getTableName()+") :: "+sourceFile.getAbsolutePath()+" !!!!!!!!");
//			return sourceFile;
//		} else {
//			//如果原始文件已经存在，则不再生成
//			if(sourceFile.exists()) {
//				sourceFile= FileUtil.resolveByPath(sourceFile.getParentFile(),sourceFile.getName()+".code");
//				return sourceFile;
//			} else {
//				return sourceFile;
//			}
//		}
		
	}
}
