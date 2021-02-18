package com.github.foxnic.generator.clazz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.feature.FeatureBuilder;

 

public class ControllerBuilder extends FileBuilder {

	public ControllerBuilder(Context ctx) {
		super(ctx);
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

		
		
		this.addImport(RestController.class);
		this.addImport(ctx.getBaseControllerClassName());
		this.addImport(ClassNames.SwaggerApi);
		code.ln("@Api(value = \""+ctx.getTableMeta().getTopic()+"\")");
		code.ln("@RestController");
		code.ln("public class " + ctx.getCtrlName() + " extends BaseController {");

		this.addImport(Autowired.class);
		this.addImport(ctx.getIntfFullName());
		this.addImport(ctx.getVoFullName());
		this.addImport(ctx.getPoFullName());
		
		code.ln("");
		code.ln(1, "@Autowired");
		code.ln(1, "private " + ctx.getIntfName() + " "+ctx.getIntfVarName()+";");
		code.ln("");

		this.addImport(ClassNames.SentinelResource);
		this.addImport(ctx.getSentinelExceptionHnadlerClassName());
		this.addImport(ctx.getAgentFullName());
		this.addImport(PostMapping.class);
		this.addImport(ctx.getResultClassName());
		 
		
		for (FeatureBuilder builder : FeatureBuilder.BUILDERS) {
			builder.buildControllerMethod(this,ctx,code);
			code.ln("");
		}

		code.ln("");

		code.ln("}");

	}

	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getProjectDirName(), ctx.getCtrlFullName());
	}
}
