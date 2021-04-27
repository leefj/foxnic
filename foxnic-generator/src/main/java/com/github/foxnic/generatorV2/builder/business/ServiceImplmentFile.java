package com.github.foxnic.generatorV2.builder.business;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.builder.business.method.DeleteById;
import com.github.foxnic.generatorV2.builder.business.method.GetById;
import com.github.foxnic.generatorV2.builder.business.method.UpdateById;
import com.github.foxnic.generatorV2.config.MduCtx;

public class ServiceImplmentFile extends TemplateJavaFile {

	public ServiceImplmentFile(MduCtx context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ServiceImplment.java.vm");
	}
	
	@Override
	protected void buildBody() {
		
	 
		
		 this.addImport(context.getPoClassFile().getFullName());
		 this.addImport(context.getVoClassFile().getFullName());
		 
	 	CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * <p>");
		code.ln(" * "+context.getTableMeta().getComments()+" 服务实现");
		code.ln(" * </p>");
		code.ln(" * @author "+context.getSettings().getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln("*/");
		code.ln("");
		this.putVar("classJavaDoc", code);
		
		this.putVar("beanName",beanNameUtil.getClassName(this.getContext().getTableMeta().getTableName()));
		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());
		
		this.putVar("interfaceName", this.getContext().getServiceInterfaceFile().getSimpleName());
		this.addImport(this.getContext().getServiceInterfaceFile().getFullName());
		
		String daoNameConst=this.getContext().getDAONameConst();
		//如果是一个字符串
		if(daoNameConst.startsWith("\"") && daoNameConst.endsWith("\"")) {
			//保持原样
		} else {
			String[] tmp=daoNameConst.split("\\.");
			String c=tmp[tmp.length-2]+"."+tmp[tmp.length-1];
			String cls=daoNameConst.substring(0,daoNameConst.lastIndexOf('.'));
			this.addImport(cls);
			daoNameConst=c;
		}
		
		this.putVar("daoName", daoNameConst);
		
		
		
		
		DeleteById deleteById=new DeleteById(context);
		this.putVar("deleteByIdMethods",deleteById.buildServiceImplementMethod(this));
		
		GetById getById=new GetById(context);
		this.putVar("getByIdMethod",getById.buildServiceImplementMethod(this));
		
		UpdateById updateById = new UpdateById(context);
		this.putVar("updateByIdMethod",updateById.buildServiceImplementMethod(this));
	}
	
	

}
