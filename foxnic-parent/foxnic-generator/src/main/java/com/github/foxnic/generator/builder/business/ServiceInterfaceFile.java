package com.github.foxnic.generator.builder.business;

import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.entity.ISuperService;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.excel.ValidateResult;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.business.method.DeleteById;
import com.github.foxnic.generator.builder.business.method.GetById;
import com.github.foxnic.generator.builder.business.method.UpdateById;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.OrderBy;
import com.github.foxnic.sql.meta.DBField;

import java.io.InputStream;
import java.util.List;

public class ServiceInterfaceFile extends TemplateJavaFile {

	public ServiceInterfaceFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ServiceInterface.java.vm","服务接口");
	}
	
	@Override
	protected void buildBody() {



		this.addImport(ConditionExpr.class);
		this.addImport(ISuperService.class);
		this.addImport(context.getPoClassFile().getFullName());
		this.addImport(context.getVoClassFile().getFullName());
		this.addImport(List.class);
		this.addImport(Result.class);
		this.addImport(PagedList.class);
		this.addImport(InputStream.class);
		this.addImport(OrderBy.class);
		this.addImport(DBField.class);
		this.addImport(ExcelWriter.class);
		this.addImport(ExcelStructure.class);
		this.addImport(ValidateResult.class);
		this.addImport(SaveMode.class);





		DBTableMeta tableMeta=context.getTableMeta();
		boolean isSimplePK=false;
		if(tableMeta.getPKColumnCount()==1) {
			DBColumnMeta pk=tableMeta.getPKColumns().get(0);
			this.putVar("pkType", pk.getDBDataType().getType().getSimpleName());
			this.putVar("idPropertyConst", context.getPoClassFile().getIdProperty().getNameConstants());
			this.putVar("idPropertyName", context.getPoClassFile().getIdProperty().name());
			this.putVar("idPropertyType", context.getPoClassFile().getIdProperty().type().getSimpleName());
			isSimplePK=true;
		}
		this.putVar("isSimplePK", isSimplePK);

 
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
