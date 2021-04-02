package com.github.foxnic.generator.clazz;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.Context;

 
/**
 * 使用模版引擎时的控制器
 * */
public class PageControllerBuilder extends FileBuilder {

	public PageControllerBuilder(Context ctx) {
		super(ctx);
	}

	public void build() {

		code.ln("package " + ctx.getPageCtrlPackage() + ";");
		code.ln("");

		code.ln("");
		// 加入注释
		code.ln("/**");
		this.appendAuthorAndTime();
		code.ln(" * "+ctx.getTopic()+" 模版页面");
		code.ln(" */");
		code.ln("");

		String superController=ctx.getSuperController();
		
		this.addImport(Controller.class);
		this.addImport(superController);
 
//		if(ctx.isEnableSwagger()) {
//			this.addImport(ClassNames.SwaggerApi);
//			code.ln("@Api(tags = \""+ctx.getTopic()+"\")");
//			if(ctx.getApiSort()!=null) {
//				code.ln("@ApiSort("+ctx.getApiSort()+")");
//				this.addImport(ApiSort.class);
//			}
//		}
		
		code.ln("@Controller(\""+ctx.getBeanNameMainPart()+"PageController\")");
		code.ln("@RequestMapping("+ctx.getPageCtrlName()+".prefix)");
		superController=StringUtil.getLastPart(superController, ".");
		code.ln("public class " + ctx.getPageCtrlName() + " extends "+superController+" {");
 
		String p2=StringUtil.getLastPart(ctx.getPageCtrlFullName(), ".",2).toLowerCase();
		code.ln(1,"");
		code.ln(1,"public static final String prefix=\"pages/"+p2+"/"+ctx.getUIModuleFolderName()+"\";");
		
		
		this.addImport(Autowired.class);
		this.addImport(ctx.getIntfFullName());
 

		code.ln("");
		code.ln(1, "@Autowired");
		code.ln(1, "private " + ctx.getIntfName() + " "+ctx.getIntfVarName()+";");
 
		this.addImport(RequestMapping.class);
		this.addImport(ctx.getControllerResult());
		this.addImport(Model.class);
 
		//首页方法
		code.ln("");
		// 加入注释
		code.ln(1,"/**");
		code.ln(1," * "+ctx.getTopic()+" 功能主页面");
		code.ln(1," */");
		String methodName=ctx.getPoName().toLowerCase();
		if(ctx.getPageControllerMethodAnnotiationPlugin()!=null) {
			ctx.getPageControllerMethodAnnotiationPlugin().addMethodAnnotiation(ctx,"list",this,code);
		}
		code.ln(1, "@RequestMapping(\"/"+ctx.getUIModuleFolderName()+"_list.html\")");
		code.ln(1, "public String list(Model model) {");
		code.ln(2, "return prefix+\"/"+ctx.getUIModuleFolderName()+"_list\";");
		code.ln(1, "}");
		
		//表单页
		code.ln("");
		// 加入注释
		code.ln(1,"/**");
		code.ln(1," * "+ctx.getTopic()+" 表单页面");
		code.ln(1," */");
		if(ctx.getPageControllerMethodAnnotiationPlugin()!=null) {
			ctx.getPageControllerMethodAnnotiationPlugin().addMethodAnnotiation(ctx,"form",this,code);
		}
		code.ln(1, "@RequestMapping(\"/"+ctx.getUIModuleFolderName()+"_form.html\")");
		code.ln(1, "public String form(Model model) {");
		code.ln(2, "return prefix+\"/"+ctx.getUIModuleFolderName()+"_form\";");
		code.ln(1, "}");
		

		code.ln("}");

	}

	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getServiceProject().getMainSourceDir(), ctx.getPageCtrlFullName());
	}
 
}
