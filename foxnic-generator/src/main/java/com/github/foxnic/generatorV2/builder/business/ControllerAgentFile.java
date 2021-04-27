package com.github.foxnic.generatorV2.builder.business;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.builder.business.method.DeleteById;
import com.github.foxnic.generatorV2.builder.business.method.GetById;
import com.github.foxnic.generatorV2.builder.business.method.UpdateById;
import com.github.foxnic.generatorV2.config.MduCtx;

public class ControllerAgentFile extends TemplateJavaFile {

	public ControllerAgentFile(MduCtx context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ControllerAgent.java.vm"," 控制器服务代理");
	}
	
	@Override
	protected void buildBody() {
		
	 
		
		 this.addImport(context.getPoClassFile().getFullName());
		 this.addImport(context.getVoClassFile().getFullName());
 
		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());
		
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
		
		this.putVar("apiBasicPath", this.getContext().getMicroServiceNameConstValue());
		
		this.putVar("apiContextPath",this.context.getTableMeta().getTableName().replace('_', '-'));
	 
	}
	
	@Override
	public String getVar() {
		return this.getContext().getPoClassFile().getVar()+"Service";
	}

}
