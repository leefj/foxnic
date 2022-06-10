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
import com.github.foxnic.dao.meta.DBIndexMeta;
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
import java.util.Map;

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
			this.addImport(Map.class);
		}
		this.putVar("isSimplePK", isSimplePK);


		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());

		DeleteById deleteById=new DeleteById(context,this);
		this.putVar("deleteByIdMethods",deleteById.buildServiceInterfaceMethod(this));

		GetById getById=new GetById(context,this);
		this.putVar("getByIdMethod",getById.buildServiceInterfaceMethod(this));

		UpdateById updateById = new UpdateById(context,this);
		this.putVar("updateByIdMethod",updateById.buildServiceInterfaceMethod(this));

		//关系表相关
		if(context.getRelationMasterIdField()!=null) {
			Class cls=context.getRelationMasterIdField().table().getClass();
			String imp=cls.getName().split("\\$")[0];
			String table=cls.getName().split("\\$")[1];
			this.addImport(imp+".*");

			String relationMasterVar=context.getRelationMasterIdField().var();
			String relationSlaveVar=context.getRelationSlaveIdField().var()+"s";

			this.putVar("relationMasterIdField", table+"."+context.getRelationMasterIdField().name().toUpperCase());
			this.putVar("relationSlaveIdField", table+"."+context.getRelationSlaveIdField().name().toUpperCase());
			this.putVar("isRelationClearWhenEmpty", context.isRelationClearWhenEmpty());
			this.putVar("relationMasterVar", relationMasterVar);
			this.putVar("relationMasterVarType", context.getRelationMasterIdField().type().getType().getSimpleName());
			this.putVar("relationMasterVarDoc", context.getRelationMasterIdField().label());
			this.putVar("relationSlaveVar", relationSlaveVar);
			this.putVar("relationSlaveVarType", context.getRelationSlaveIdField().type().getType().getSimpleName());
			this.putVar("relationSlaveVarDoc", context.getRelationSlaveIdField().label()+"清单");
		}

		//
		this.putVar("bpm", !this.getContext().getBpmConfig().getIntegrateMode().equals("none"));


//		List<DBIndexMeta> indexMetas = tableMeta.getUniqueIndexs();
//		for (DBIndexMeta indexMeta : indexMetas) {
//			if(indexMeta.isPrimary()) continue;
//			if(!indexMeta.isUnique()) continue;
//			if(indexMeta.getFields().length!=1) continue;
//		}



	}

	@Override
	public String getVar() {
		return this.getContext().getPoClassFile().getVar()+"Service";
	}

}
