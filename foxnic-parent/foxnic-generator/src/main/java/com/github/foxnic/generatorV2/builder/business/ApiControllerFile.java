package com.github.foxnic.generatorV2.builder.business;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generatorV2.builder.business.method.DeleteById;
import com.github.foxnic.generatorV2.builder.business.method.GetById;
import com.github.foxnic.generatorV2.builder.business.method.Insert;
import com.github.foxnic.generatorV2.builder.business.method.QueryList;
import com.github.foxnic.generatorV2.builder.business.method.QueryPagedList;
import com.github.foxnic.generatorV2.builder.business.method.Save;
import com.github.foxnic.generatorV2.builder.business.method.Update;
import com.github.foxnic.generatorV2.builder.model.PojoProperty;
import com.github.foxnic.generatorV2.config.MduCtx;
 
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

public class ApiControllerFile extends TemplateJavaFile {

	private CodePoint codePoint;
	private DBTableMeta tableMeta;
	public ApiControllerFile(MduCtx context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ApiController.java.vm","接口控制器");
		this.codePoint=context.getCodePoint();
		this.tableMeta=context.getTableMeta();
	}
	
	@Override
	protected void buildBody() {
		
		 this.addImport(context.getControllerAgentFile().getFullName());
		 this.addImport(context.getVoMetaClassFile().getFullName());
		 this.addImport(context.getPoClassFile().getFullName());
		 this.addImport(context.getVoClassFile().getFullName());
		 
		 this.putVar("isEnableSwagger", this.context.getSettings().isEnableSwagger());
		 this.putVar("isEnableMicroService", this.context.getSettings().isEnableMicroService());
		 this.putVar("apiSort", this.context.getApiSort());
		 if(this.context.getSettings().isEnableSwagger()) {
			 this.addImport(Api.class);
			 this.addImport("com.github.xiaoymin.knife4j.annotations.ApiSort");
			 this.addImport(ApiOperation.class);
			 this.addImport(ApiImplicitParams.class);
			 this.addImport(ApiImplicitParam.class);
			 this.addImport("com.github.xiaoymin.knife4j.annotations.ApiOperationSupport");
			 this.addImport(context.getControllerAgentFile().getFullName());
		 }
		 
		 if(this.context.getSettings().isEnableMicroService()) {
			 this.addImport("com.alibaba.csp.sentinel.annotation.SentinelResource");
		 }
		 
		 this.putVar("beanName",beanNameUtil.getClassName(this.getContext().getTableMeta().getTableName())+"Controller");
		 this.putVar("poSimpleName", this.context.getPoClassFile().getSimpleName());
		 this.putVar("poVarName", this.context.getPoClassFile().getVar());
		 
		 this.putVar("serviceSimpleName", this.context.getServiceInterfaceFile().getSimpleName());
		 this.putVar("serviceVarName", this.context.getServiceInterfaceFile().getVar());	
		 this.addImport(this.context.getServiceInterfaceFile().getFullName());
		 
		 this.putVar("agentSimpleName", this.context.getControllerAgentFile().getSimpleName());
		 this.putVar("voSimpleName", this.context.getVoClassFile().getSimpleName());
		 this.putVar("voVarName", this.context.getVoClassFile().getVar());
		 
		 this.putVar("voMetaSimpleName", this.context.getVoMetaClassFile().getSimpleName());
 
		Insert insert=new Insert(this.context);
		this.putVar("swager4Insert", insert.getControllerSwagerAnnotations(this,codePoint).toString().trim());
		String validation4Insert=insert.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4Insert)) {
			this.putVar("validation4Insert", validation4Insert);
		}
		
		DeleteById deleteById=new DeleteById(this.context);
		this.putVar("swager4DeleteById", deleteById.getControllerSwagerAnnotations(this,codePoint).toString().trim());
		String validation4DeleteById=deleteById.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4DeleteById)) {
			this.putVar("validation4DeleteById", validation4DeleteById);
		}
		this.putVar("controllerMethodParameterDeclare4DeleteById", deleteById.getControllerMethodParameterDeclare());
		this.putVar("controllerMethodParameterPassIn4DeleteById", deleteById.getControllerMethodParameterPassIn());
		this.putVar("implMethod4DeleteById", deleteById.getImplMethod());
		
	
		
		
		Update update=new Update(this.context);
		this.putVar("swager4Update", update.getControllerSwagerAnnotations(this,codePoint).toString().trim());
		String validation4Update=update.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4Update)) {
			this.putVar("validation4Update", validation4Update);
		}
		
		List<String> ignoreParameters4Update=new ArrayList<>();
		for (PojoProperty p : context.getVoClassFile().getProperties()) {
			ignoreParameters4Update.add(context.getVoMetaClassFile().getSimpleName()+"."+p.getNameConstants());
		}
		this.putVar("ignoreParameters4Update", StringUtil.join(ignoreParameters4Update," , "));
		
		
		Save save=new Save(this.context);
		this.putVar("swager4Save", save.getControllerSwagerAnnotations(this,codePoint).toString().trim());
		String validation4Save=save.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4Save)) {
			this.putVar("validation4Save", validation4Save);
		}
		
		QueryList queryList=new QueryList(this.context);
		this.putVar("swager4QueryList", queryList.getControllerSwagerAnnotations(this,codePoint).toString().trim());
		String validation4QueryList=queryList.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4QueryList)) {
			this.putVar("validation4QueryList", validation4QueryList);
		}
		
		
		QueryPagedList queryPagedList=new QueryPagedList(this.context);
		this.putVar("swager4QueryPagedList", queryPagedList.getControllerSwagerAnnotations(this,codePoint).toString().trim());
		String validation4QueryPagedList=queryPagedList.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4QueryPagedList)) {
			this.putVar("validation4QueryPagedList", validation4QueryPagedList);
		}
		
		
		GetById getById=new GetById(context);
		this.putVar("controllerMethodParameterDeclare4GetById", getById.getControllerMethodParameterDeclare());
		this.putVar("controllerMethodParameterPassIn4GetById", getById.getControllerMethodParameterPassIn());

		
	}
	
 
	/**
	 * 处理代码，去除一些空白行
	 * */
	protected String processSource(String source) {
		String[] lines=source.split("\n");
		List<String> list=new ArrayList<>();
		String prev=null;
		for (String line : lines) {
			if(prev!=null && prev.startsWith("@ApiOperationSupport") && StringUtil.isBlank(line)) {
				continue;
			}
			list.add(line);
			prev=line.trim();
		}
		return StringUtil.join(list,"\n");
	}
	
	
	
	
	
	
	@Override
	public String getVar() {
		return this.getContext().getPoClassFile().getVar()+"Service";
	}

}
