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
		code.ln(1,"public static final String prefix=\"pages/"+p2+"/"+ctx.getPoName().toLowerCase()+"\";");
		
		
		this.addImport(Autowired.class);
		this.addImport(ctx.getIntfFullName());
 
		
		code.ln("");
		code.ln(1, "@Autowired");
		code.ln(1, "private " + ctx.getIntfName() + " "+ctx.getIntfVarName()+";");
		code.ln("");

 
		this.addImport(RequestMapping.class);
		this.addImport(ctx.getControllerResult());
		this.addImport(Model.class);
 
		code.ln("");
		
		//首页方法
		String methodName=ctx.getPoName().toLowerCase();
		if(ctx.getPageControllerMethodAnnotiationPlugin()!=null) {
			ctx.getPageControllerMethodAnnotiationPlugin().addMethodAnnotiation(ctx,methodName,this,code);
		}
		code.ln(1, "@RequestMapping(\"/"+methodName+".html\")");
		code.ln(1, "public String "+methodName+"(Model model) {");
		code.ln(2, "return prefix+\"/"+methodName+"\";");
		code.ln(1, "}");
		
		//表单页
	 
		if(ctx.getPageControllerMethodAnnotiationPlugin()!=null) {
			ctx.getPageControllerMethodAnnotiationPlugin().addMethodAnnotiation(ctx,"form",this,code);
		}
		code.ln(1, "@RequestMapping(\"/"+methodName+"_form.html\")");
		code.ln(1, "public String form(Model model) {");
		code.ln(2, "return prefix+\"/"+methodName+"\";");
		code.ln(1, "}");
		

		code.ln("}");

	}

	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getServiceProject().getMainSourceDir(), ctx.getPageCtrlFullName());
	}
 
	@Override
	protected File processOverride(File sourceFile) {
 
		//如果原始文件已经存在，则不再生成
		if(sourceFile.exists()) {
			return sourceFile;
		} else {
			return sourceFile;
		}
		
		
	}
}
