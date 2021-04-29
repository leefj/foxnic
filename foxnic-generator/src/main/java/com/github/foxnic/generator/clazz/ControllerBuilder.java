package com.github.foxnic.generator.clazz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.CodePoint;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.github.foxnic.generatorV2.config.WriteMode;
import com.github.xiaoymin.knife4j.annotations.ApiSort;

 

public class ControllerBuilder extends FileBuilder {

	private MduCtx mductx;
	public ControllerBuilder(MduCtx mductx, Context ctx) {
		super(ctx);
		this.mductx=mductx;
	}

	public void build() {

		code.ln("package " + ctx.getCtrlPackage() + ";");
		code.ln("");

		code.ln("");
		// 加入注释
		code.ln("/**");
		this.appendAuthorAndTime();
		code.ln("*/");
		code.ln("");

		String superController=ctx.getSuperController();
		
		this.addImport(RestController.class);
		this.addImport(superController);
 
		if(ctx.isEnableSwagger()) {
			this.addImport(ClassNames.SwaggerApi);
			code.ln("@Api(tags = \""+ctx.getTopic()+"\")");
			if(ctx.getApiSort()!=null) {
				code.ln("@ApiSort("+ctx.getApiSort()+")");
				this.addImport(ApiSort.class);
			}
		}
		code.ln("@RestController(\""+ctx.getBeanNameMainPart()+"Controller\")");
		if(!ctx.isEnableMicroService()) {
			String apiPrefix=ctx.getControllerApiPrefix();
			if(apiPrefix==null) {
				apiPrefix=ctx.getTableName();
				apiPrefix="/"+apiPrefix.replace('_', '/');
			}
			code.ln("@RequestMapping(\""+apiPrefix+"\")");
			this.addImport(RequestMapping.class);
		}
		superController=StringUtil.getLastPart(superController, ".");
		code.ln("public class " + ctx.getCtrlName() + " extends "+superController+" {");

		
		this.addImport(Autowired.class);
		this.addImport(ctx.getIntfFullName());
//		this.addImport(ctx.getDefaultVO().getFullName());
		this.addImport(this.mductx.getVoClassFile().getFullName());
		this.addImport(ctx.getPoFullName());
		
		code.ln("");
		code.ln(1, "@Autowired");
		code.ln(1, "private " + ctx.getIntfName() + " "+ctx.getIntfVarName()+";");
		code.ln("");

		if(ctx.isEnableMicroService()) {
			this.addImport(ClassNames.SentinelResource);
			this.addImport(ctx.getSentinelExceptionHandlerClassName());
			this.addImport(ctx.getAgentFullName());
		}
		this.addImport(PostMapping.class);
		this.addImport(ctx.getControllerResult());
		 
		
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildControllerMethod(this,ctx,code);
			code.ln("");
		}

		code.ln("");

		code.ln("}");

	}

	@Override
	public void buildAndUpdate() {
//		this.buildAndUpdateJava(ctx.getServiceProject().getMainSourceDir(), ctx.getCtrlFullName());
//		WriteMode mode=ctx.getWriteMode(getClass());
//		if(mode==WriteMode.DO_NOTHING) {
//			this.replaceCodePoint();
//			this.ctx.getCodePoint().syncAll();
//		}
	}

	private void replaceCodePoint() {
		 CodePoint cp=ctx.getCodePoint();
		 try {
			cp.replace(this.getSourceFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
}
