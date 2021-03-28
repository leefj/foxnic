package com.github.foxnic.generator.clazz;
 
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;

import java.io.File;

public class AgentBuilder extends FileBuilder {
 
	public AgentBuilder(Context ctx) {
		super(ctx);
	}
	
	
	public void build() {
		
		code.ln("package "+ctx.getAgentPackage()+";");
		code.ln("");
		 
		//加入注释
		code.ln("/**");
		code.ln(" * @author "+ctx.getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln("*/");
		code.ln("");
		
		this.addImport(ClassNames.FeignClient);
	
		String microServiceNameClass=StringUtil.getLastPart(ctx.getMicroServiceNamesClassName(), ".");
		
		this.addImport(ctx.getMicroServiceNamesClassName());
		String feignConfigClass=ctx.getFeignConfigClassName();
		this.addImport(feignConfigClass);
 
		feignConfigClass=StringUtil.getLastPart(feignConfigClass, ".");
		code.ln("@FeignClient(value = "+microServiceNameClass+"."+ctx.getMicroServicePropertyConst()+", contextId = "+ctx.getAgentName()+".API_CONTEXT_PATH , configuration = "+feignConfigClass+".class)");
		code.ln("public interface "+ctx.getAgentName()+" {");
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 基础路径 , "+ctx.getControllerApiPrefix());
		code.ln(1,"*/");
		code.ln(1,"public static final String API_BASIC_PATH = \""+ctx.getControllerApiPrefix()+"\";");
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * API 上下文路径 , "+ctx.getApiContextPart());
		code.ln(1,"*/");
		code.ln(1,"public static final String API_CONTEXT_PATH = \""+ctx.getApiContextPart()+"\";");
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * API 基础路径 , 由 API_BASIC_PATH 和 API_CONTEXT_PATH 两部分组成");
		code.ln(1,"*/");
		code.ln(1,"public static final String API_PREFIX = \"/\" + API_BASIC_PATH + \"/\"+API_CONTEXT_PATH+\"/\";");
		
		
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildFeignConstant(this,ctx,code);
		}
		
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildFeignMethod(this,ctx,code);
		}
 
		code.ln("");
	 
		code.ln("}");
		 
	}
	
	
	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getAgentProject().getMainSourceDir(), ctx.getAgentFullName());
	}

	@Override
	protected File processOverride(File sourceFile) {

		if(sourceFile.exists()) {
			return null;
		} else {
			return sourceFile;
		}

		//如果强制重写，默认
//		if(ctx.isForceOverrideController()) {
//			System.err.println("!!!!!!! Force Override Agent("+ctx.getTableName()+") :: "+sourceFile.getAbsolutePath()+" !!!!!!!!");
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
