package com.github.foxnic.generator.builder.business;

import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.generator.builder.business.method.DeleteById;
import com.github.foxnic.generator.builder.business.method.GetById;
import com.github.foxnic.generator.builder.business.method.UpdateById;
import com.github.foxnic.generator.config.ModuleContext;

import java.io.InputStream;
import java.util.List;

public class ServiceInterfaceFile extends TemplateJavaFile {

	public ServiceInterfaceFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ServiceInterface.java.vm","服务接口");
	}
	
	@Override
	protected void buildBody() {
		
	 
		
		this.addImport(context.getPoClassFile().getFullName());
		this.addImport(context.getVoClassFile().getFullName());
		this.addImport(List.class);
		this.addImport(Result.class);
		this.addImport(PagedList.class);
		this.addImport(InputStream.class);

 
		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());
 
		DeleteById deleteById=new DeleteById(context);
		this.putVar("deleteByIdMethods",deleteById.buildServiceInterfaceMethod(this));
		
		GetById getById=new GetById(context);
		this.putVar("getByIdMethod",getById.buildServiceInterfaceMethod(this));
		
		UpdateById updateById = new UpdateById(context);
		this.putVar("updateByIdMethod",updateById.buildServiceInterfaceMethod(this));
	}
	
	@Override
	public String getVar() {
		return this.getContext().getPoClassFile().getVar()+"Service";
	}

}
