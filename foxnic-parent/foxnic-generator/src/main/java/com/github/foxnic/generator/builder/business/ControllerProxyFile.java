package com.github.foxnic.generator.builder.business;

import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.builder.business.method.DeleteById;
import com.github.foxnic.generator.builder.business.method.GetById;
import com.github.foxnic.generator.config.ModuleContext;

public class ControllerProxyFile extends TemplateJavaFile {

	public ControllerProxyFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ControllerProxy.java.vm"," 控制器服务代理");
	}
	
	private String modulePrefixURI;
	
	public String getModulePrefixURI() {
		return "/"+modulePrefixURI;
	}
	
	@Override
	protected void buildBody() {
		
	 
		
		 this.addImport(context.getPoClassFile().getFullName());
		 this.addImport(context.getVoClassFile().getFullName());
 
		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());
		
		if(context.getSettings().isEnableMicroService()) {
			String msNameConst=this.getContext().getMicroServiceNameConst();
			//如果是一个字符串
			if(msNameConst.startsWith("\"") && msNameConst.endsWith("\"")) {
				//保持原样
			} else {
				String[] tmp=msNameConst.split("\\.");
				String c=tmp[tmp.length-2]+"."+tmp[tmp.length-1];
				String cls=msNameConst.substring(0,msNameConst.lastIndexOf('.'));
				this.addImport(cls);
				msNameConst=c;
			}
			this.putVar("msNameConst", msNameConst);
			modulePrefixURI=this.getContext().getMicroServiceNameConstValue();
			this.putVar("apiBasicPath", modulePrefixURI);
		} else {
			modulePrefixURI="api";
			this.putVar("apiBasicPath", modulePrefixURI);
		}
		
		this.putVar("agentSimpleName",this.context.getControllerProxyFile().getSimpleName());
		this.putVar("isEnableMicroService",this.context.getSettings().isEnableMicroService());
		
		this.putVar("apiContextPath",this.context.getTableMeta().getTableName().replace('_', '-'));
		modulePrefixURI+="/"+this.context.getTableMeta().getTableName().replace('_', '-');
		
		this.putVar("controllerClassName",this.context.getApiControllerFile().getFullName());
		
		DeleteById deleteById=new DeleteById(this.context);
		this.putVar("controllerMethodParameterDeclare4DeleteById", deleteById.getControllerMethodParameterDeclare());
		
		GetById getById=new GetById(context);
		this.putVar("controllerMethodParameterDeclare4GetById", getById.getControllerMethodParameterDeclare());
		
	 
	}
	
	@Override
	public String getVar() {
		return this.getContext().getPoClassFile().getVar()+"Service";
	}

}
