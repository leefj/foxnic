package com.github.foxnic.generator.builder.business;

import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.entity.SuperService;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.excel.ValidateResult;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.builder.business.config.ServiceConfig;
import com.github.foxnic.generator.builder.business.method.DeleteById;
import com.github.foxnic.generator.builder.business.method.GetById;
import com.github.foxnic.generator.builder.business.method.UpdateById;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Select;
import com.github.foxnic.sql.meta.DBField;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceImplmentFile extends TemplateJavaFile {

	public ServiceImplmentFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ServiceImplment.java.vm","服务实现");
	}

	@Override
	protected void buildBody() {



		this.addImport(context.getPoClassFile().getFullName());
		this.addImport(context.getVoClassFile().getFullName());
		this.addImport(List.class);
		this.addImport(Result.class);
		this.addImport(PagedList.class);
		this.addImport(SuperService.class);
		this.addImport(DAO.class);
		this.addImport(Field.class);
		this.addImport(IDGenerator.class);
		this.addImport(ConditionExpr.class);
		this.addImport(ErrorDesc.class);
		this.addImport(ExcelWriter.class);
		this.addImport(ValidateResult.class);
		this.addImport(ExcelStructure.class);
		this.addImport(InputStream.class);
		this.addImport(DBField.class);
		this.addImport(SaveMode.class);
		this.addImport(DBColumnMeta.class);
		this.addImport(Select.class);
		this.addImport(ArrayList.class);


		List<ServiceConfig.InjectDesc> injectDescs = this.context.getServiceConfig().getInjectDescs();
		for (ServiceConfig.InjectDesc injectDesc : injectDescs) {
			this.addImport(injectDesc.getType());
			this.addImport(injectDesc.getAnnType());
		}
		this.putVar("injectDescs",injectDescs);
		//
		List<ServiceConfig.RelationSaveDesc> relationSaveDescs=this.context.getServiceConfig().getRelationSaveDescs();
		this.putVar("relationSaveDescs",relationSaveDescs);
		this.putVar("hasRelationSave",!relationSaveDescs.isEmpty());
		if(!relationSaveDescs.isEmpty()) {
			this.addImport(Transactional.class);
		}

		this.putVar("isEnableImportExcel", this.context.getListConfig().isEnableImportExcel());
		this.putVar("isEnableExportExcel", this.context.getListConfig().isEnableExportExcel());

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




		DeleteById deleteById=new DeleteById(context,this);
		this.putVar("deleteByIdMethods",deleteById.buildServiceImplementMethod(this));

		GetById getById=new GetById(context,this);
		this.putVar("getByIdMethod",getById.buildServiceImplementMethod(this));

		UpdateById updateById = new UpdateById(context,this);
		this.putVar("updateByIdMethod",updateById.buildServiceImplementMethod(this));


		DBTableMeta tableMeta=context.getTableMeta();
		boolean isSimplePK=false;
		if(tableMeta.getPKColumnCount()==1) {
			DBColumnMeta pk=tableMeta.getPKColumns().get(0);
			this.putVar("pkType", pk.getDBDataType().getType().getSimpleName());
			this.putVar("idPropertyConst", context.getPoClassFile().getIdProperty().getNameConstants());
			this.putVar("idPropertyName", context.getPoClassFile().getIdProperty().name());
			this.putVar("idPropertyType", context.getPoClassFile().getIdProperty().type().getSimpleName());
			this.putVar("idGetterMethodName", context.getPoClassFile().getIdProperty().getGetterMethodName(pk.getDBDataType()));
			isSimplePK=true;

			this.addImport(Map.class);
		}
		this.putVar("isSimplePK", isSimplePK);

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
			this.putVar("relationMasterPoType", context.getRelationMasterPoType().getSimpleName());
			this.putVar("relationSlavePoType", context.getRelationSlavePoType().getSimpleName());

			this.addImport(context.getRelationMasterPoType());
			this.addImport(context.getRelationSlavePoType());

		}

		//
		boolean bpm= !this.getContext().getBpmConfig().getIntegrateMode().equals("none");
		this.putVar("bpm",bpm);
		if(bpm) {
			this.putVar("bpmFormCode", this.getContext().getBpmConfig().getFormCode());
			this.putVar("bpmEventAdaptorName", this.context.getBpmEventAdaptorFile().getSimpleName());
			this.addImport(this.context.getBpmEventAdaptorFile().getFullName());
		}
	}



}
