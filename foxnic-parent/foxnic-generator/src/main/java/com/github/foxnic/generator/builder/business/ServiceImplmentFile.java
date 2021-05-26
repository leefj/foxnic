package com.github.foxnic.generator.builder.business;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.builder.business.method.DeleteById;
import com.github.foxnic.generator.builder.business.method.GetById;
import com.github.foxnic.generator.builder.business.method.UpdateById;
import com.github.foxnic.generator.config.MduCtx;

public class ServiceImplmentFile extends TemplateJavaFile {

	public ServiceImplmentFile(MduCtx context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ServiceImplment.java.vm","服务实现");
	}
	
	@Override
	protected void buildBody() {
		
	 
		
		 this.addImport(context.getPoClassFile().getFullName());
		 this.addImport(context.getVoClassFile().getFullName());
		
		this.putVar("beanName",beanNameUtil.getClassName(this.getContext().getTableMeta().getTableName())+"Service");
		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());
		
		this.putVar("interfaceName", this.getContext().getServiceInterfaceFile().getSimpleName());
		this.addImport(this.getContext().getServiceInterfaceFile().getFullName());
		
		String daoNameConst=this.getContext().getDAONameConst();
		//如果是一个字符串
		if(daoNameConst!=null) {
			if(daoNameConst.startsWith("\"") && daoNameConst.endsWith("\"")) {
				//保持原样
			} else {
				String[] tmp=daoNameConst.split("\\.");
				String c=tmp[tmp.length-2]+"."+tmp[tmp.length-1];
				String cls=daoNameConst.substring(0,daoNameConst.lastIndexOf('.'));
				this.addImport(cls);
				daoNameConst=c;
			}
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
