package com.github.foxnic.generator.clazz;
 
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;

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
		this.addImport("com.chinayie.platform.agent.config.MicroServiceNames");
		this.addImport("com.chinayie.platform.agent.config.FeignConfiguration");
 
		code.ln("@FeignClient(value = MicroServiceNames."+ctx.getMicroServiceNameConst()+", contextId = "+ctx.getAgentName()+".API_CONTEXT_PATH , configuration = FeignConfiguration. class)");
		code.ln("public interface "+ctx.getAgentName()+" {");
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 基础路径 , "+ctx.getApiPrefix());
		code.ln(1,"*/");
		code.ln(1,"public static final String BASIC_PATH = \""+ctx.getApiPrefix()+"\";");
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * API 上下文路径 , "+ctx.getApiContextPart());
		code.ln(1,"*/");
		code.ln(1,"public static final String API_CONTEXT_PATH = \""+ctx.getApiContextPart()+"\";");
		
		
		
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
//		this.buildAndUpdateJava("framework-agent",ctx.getAgentFullName());
	}
	 
}
