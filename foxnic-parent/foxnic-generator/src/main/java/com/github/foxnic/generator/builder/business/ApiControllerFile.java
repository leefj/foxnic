package com.github.foxnic.generator.builder.business;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.io.StreamUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.excel.ValidateResult;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.business.config.RestAPIConfig;
import com.github.foxnic.generator.builder.business.method.*;
import com.github.foxnic.generator.builder.model.PojoProperty;
import com.github.foxnic.generator.builder.view.config.FillByUnit;
import com.github.foxnic.generator.builder.view.config.FillWithUnit;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.springboot.api.swagger.source.ControllerSwaggerCompilationUnit;
import com.github.foxnic.springboot.api.swagger.source.MethodAnnotations;
import com.github.foxnic.springboot.api.swagger.source.SwaggerAnnotationApi;
import com.github.foxnic.springboot.api.swagger.source.SwaggerAnnotationApiImplicitParam;
import com.github.foxnic.springboot.web.DownloadUtil;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

public class ApiControllerFile extends TemplateJavaFile {

	private DBTableMeta tableMeta;

	private File literalFile;
	public ApiControllerFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/ApiController.java.vm","接口控制器");
		this.tableMeta=context.getTableMeta();

		StackTraceElement[] els=(new Throwable()).getStackTrace();
		StackTraceElement e=els[els.length-1];
		String bootClsName= BeanUtil.getFieldValue(e,"declaringClass",String.class);
		Class bootCls= ReflectUtil.forName(bootClsName);
		MavenProject mp=new MavenProject(bootCls);
		literalFile = FileUtil.resolveByPath(mp.getProjectDir(),"autocode",context.getDAO().getSchema(),context.getDAO().getUserName(),context.getTableMeta().getTableName()+".json");
		if(literalFile.exists()) {
			prevLiteralMap = FileUtil.readJSONObject(literalFile);
		} else {
			prevLiteralMap = new JSONObject();
		}
	}

	protected JSONObject prevLiteralMap=null;
	protected JSONObject literalMap=new JSONObject();

	private List<Method> methods =new ArrayList<>();

	public List<Method> getMethods() {
		return methods;
	}

	@Override
	protected void buildBody() {

		this.addImport(context.getControllerProxyFile().getFullName());
		this.addImport(context.getVoMetaClassFile().getFullName());
		this.addImport(context.getPoClassFile().getFullName());
		this.addImport(context.getVoClassFile().getFullName());
		this.addImport(Result.class);
		this.addImport(SaveMode.class);
		this.addImport(ExcelWriter.class);
		this.addImport(DownloadUtil.class);
		this.addImport(PagedList.class);
		this.addImport(Date.class);
		this.addImport(Timestamp.class);
		this.addImport(ErrorDesc.class);
		this.addImport(StreamUtil.class);
		this.addImport(Map.class);
		this.addImport(ValidateResult.class);
		this.addImport(InputStream.class);
		this.addImport(this.context.getPoMetaClassFile().getFullName());

		List<PojoProperty> props=context.getPoClassFile().getProperties();
		for (PojoProperty prop : props) {
			this.addImport(prop.getTypeFullName());
		}
		//
//		Set<String> joinPropertyConstNames=new HashSet<>();
		List<FillByUnit>  fillByUnits=new ArrayList<>();
		if(this.context.getFillByUnits()!=null) {
			for (FillByUnit fillByUnit : this.context.getFillByUnits()) {
				if (fillByUnit.size() > 1) fillByUnits.add(fillByUnit);
				for (String anImport : fillByUnit.getImports()) {
					this.addImport(anImport);
				}
			}
		}



//		for (FieldInfo field : this.context.getFields()) {
//			List<FieldInfo.JoinPropertyConst> pcs=field.getQueryJoinPropertyConstNames();
//			for (FieldInfo.JoinPropertyConst pc : pcs) {
				//if(joinPropertyConstNames.contains(pc.getConstName())) continue;
				//joinPropertyConstNames.add(pc.getConstName());
//				joinPropertyConsts.add(pc);
//			}
//		}
		this.putVar("joinPropertyUnits", fillByUnits);
		//
		props=context.getVoClassFile().getProperties();
		for (PojoProperty prop : props) {
			this.addImport(prop.getTypeFullName());
		}

		this.putVar("isEnableImportExcel", this.context.getListConfig().isEnableImportExcel());
		this.putVar("isEnableExportExcel", this.context.getListConfig().isEnableExportExcel());


		this.putVar("poMetaClassName", this.context.getPoMetaClassFile().getSimpleName());

		 this.putVar("isEnableSwagger", this.context.getSettings().isEnableSwagger());
		 this.putVar("isEnableMicroService", this.context.getSettings().isEnableMicroService());
		 this.putVar("apiSort", this.context.getApiSort());
		this.putVar("inDoc", this.context.getControllerConfig().getInDoc());
		 if(this.context.getSettings().isEnableSwagger()) {
			 this.addImport(Api.class);
			 // this.addImport("com.github.xiaoymin.knife4j.annotations.ApiSort");
			 this.addImport(ApiOperation.class);
			 this.addImport(ApiImplicitParams.class);
			 this.addImport(ApiImplicitParam.class);
			 this.addImport("com.github.xiaoymin.knife4j.annotations.ApiOperationSupport");
			 this.addImport(context.getControllerProxyFile().getFullName());
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

		 this.putVar("agentSimpleName", this.context.getControllerProxyFile().getSimpleName());
		 this.putVar("voSimpleName", this.context.getVoClassFile().getSimpleName());
		 this.putVar("voVarName", this.context.getVoClassFile().getVar());

		 this.putVar("voMetaSimpleName", this.context.getVoMetaClassFile().getSimpleName());

		Insert insert=new Insert(this.context,this);
		this.putVar("swagger4Insert", insert.getControllerSwaggerAnnotations(this).toString().trim());
		String validation4Insert=insert.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4Insert)) {
			this.putVar("validation4Insert", validation4Insert);
		}
		methods.add(insert);

		DeleteById deleteById=new DeleteById(this.context,this);
		this.putVar("swagger4DeleteById", deleteById.getControllerSwaggerAnnotations(this).toString().trim());
		String validation4DeleteById=deleteById.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4DeleteById)) {
			this.putVar("validation4DeleteById", validation4DeleteById);
		}
		this.putVar("controllerMethodParameterDeclare4DeleteById", deleteById.getControllerMethodParameterDeclare());
		this.putVar("controllerMethodParameterPassIn4DeleteById", deleteById.getControllerMethodParameterPassIn());
		this.putVar("implMethod4DeleteById", deleteById.getImplMethod());
		this.putVar("implMethod4DeleteByIds", deleteById.getImplsMethod());
		methods.add(deleteById);

		this.putVar("saveMode", this.context.getControllerConfig().getSaveMode().name());
		this.putVar("batchInsert", this.context.getControllerConfig().getEnableBatchInsert());
		if(this.context.getControllerConfig().getEnableBatchInsert()) {
			this.addImport(ArrayList.class);
		}
		this.putVar("fillWithUnits", context.getControllerConfig().getFillWithUnits());
		for (FillWithUnit unit : context.getControllerConfig().getFillWithUnits().values()) {
			if(unit==null) continue;
			for (String anImport : unit.getImports()) {
				this.addImport(anImport);
			}
		}



		Update update=new Update(this.context,this);
		this.putVar("swagger4Update", update.getControllerSwaggerAnnotations(this).toString().trim());
		String validation4Update=update.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4Update)) {
			this.putVar("validation4Update", validation4Update);
		}

		List<String> ignoreParameters4Update=new ArrayList<>();
		for (PojoProperty p : context.getVoClassFile().getProperties()) {
			ignoreParameters4Update.add(context.getVoMetaClassFile().getSimpleName()+"."+p.getNameConstants());
		}
		this.putVar("ignoreParameters4Update", StringUtil.join(ignoreParameters4Update," , "));
		methods.add(update);


		Save save=new Save(this.context,this);
		this.putVar("swagger4Save", save.getControllerSwaggerAnnotations(this).toString().trim());
		String validation4Save=save.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4Save)) {
			this.putVar("validation4Save", validation4Save);
		}
		methods.add(save);

		QueryList queryList=new QueryList(this.context,this);
		this.putVar("swagger4QueryList", queryList.getControllerSwaggerAnnotations(this).toString().trim());
		String validation4QueryList=queryList.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4QueryList)) {
			this.putVar("validation4QueryList", validation4QueryList);
		}
		methods.add(queryList);


		QueryPagedList queryPagedList=new QueryPagedList(this.context,this);
		this.putVar("swagger4QueryPagedList", queryPagedList.getControllerSwaggerAnnotations(this).toString().trim());
		String validation4QueryPagedList=queryPagedList.getControllerValidateAnnotations(this).toString().trim();
		if(!StringUtil.isBlank(validation4QueryPagedList)) {
			this.putVar("validation4QueryPagedList", validation4QueryPagedList);
		}
		methods.add(queryPagedList);


		GetById getById=new GetById(context,this);
		this.putVar("controllerMethodParameterDeclare4GetById", getById.getControllerMethodParameterDeclare());
		this.putVar("controllerMethodParameterPassIn4GetById", getById.getControllerMethodParameterPassIn());
		methods.add(getById);


		List<FieldInfo> fields=this.context.getTemplateFields();
		this.putVar("fields", fields);


		List<FieldInfo> listPermFields=new ArrayList<>();
		for (FieldInfo f : fields) {
			if(f.getListPerm()!=null) {
				listPermFields.add(f);
			}
		}
		//权限受控的字段
		this.putVar("listPermFields",listPermFields);

		//
		this.putVar("bpm", !this.getContext().getBpmConfig().getIntegrateMode().equals("none"));
		this.putVar("bpmFormCode", this.getContext().getBpmConfig().getFormCode());

		this.putVar("restApiTagDir",  this.getContext().getControllerConfig().getApiTagDir()+"/");

		List<RestAPIConfig> restAPIList=this.context.getControllerConfig().getRestAPIConfigList();
		for (RestAPIConfig config : restAPIList) {
			config.appendImport4Result(this);
			config.appendImport4Parameters(this);
			this.addImport(config.getMappingType());
		}
		this.putVar("restAPIList", restAPIList);

		literalMap.put("Api.tag",this.getContext().getControllerConfig().getApiTagDir()+"/"+context.getTopic());

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

	public String updateSwagger(String version) {
		ControllerSwaggerCompilationUnit jcu=new ControllerSwaggerCompilationUnit(this.getType());

		// Api
		List<AnnotationExpr> anns=jcu.findClassAnnotation(this.getSimpleName(),Api.class.getSimpleName());
		if(anns==null) {
			System.err.println(this.getSimpleName() + "(" + version + ") 解析失败，建议重试");
			return null;
		}
		if(anns.size()==1) {
			AnnotationExpr api=anns.get(0);
			SwaggerAnnotationApi apiAn=SwaggerAnnotationApi.fromSource((NormalAnnotationExpr) api,jcu);
			String[] tags=apiAn.getTags();
			if(tags!=null && tags.length>0) {
				String tag=tags[0];
				String[] parts=tag.split("/");
				parts[parts.length-1]=this.context.getTopic();
				StringLiteralExpr valueExpr=(StringLiteralExpr)jcu.getAnnotationPropertyValueExpr(apiAn.getSource(),"tags");
				if(valueExpr!=null) {
					String prevApiTag=prevLiteralMap.getString("Api.tag");
					// 如果未修改
					if(prevApiTag==null || tag.equals(prevApiTag)) {
						String apiTag = StringUtil.join(parts, "/");
						if(!tag.equals(apiTag)) {
							literalMap.put("Api.tag", apiTag);
							valueExpr.setString(apiTag);
							isOpenAPIAnnotationsModified = true;
						}
						// 设置补充值
						if(prevApiTag==null) {
							literalMap.put("Api.tag", apiTag);
						}
					}
				}
			}
		}
		// 各个方法
		List<Method> methods=this.getMethods();
		for (Method method : methods) {
			this.updateSwaggerMethod(jcu,method);
		}
		//
		String source= jcu.getSource();
		//
		if(isOpenAPIAnnotationsModified) {
			source = source.replace("@ApiImplicitParams({ @ApiImplicitParam", "@ApiImplicitParams({\n\t\t@ApiImplicitParam");
			source = source.replace("), @ApiImplicitParam(", "),\n\t\t@ApiImplicitParam(");
			source = source.replace(") })", ")\n\t})");
		}
		return source;
	}

	public void updateSwaggerMethod(ControllerSwaggerCompilationUnit jcu, Method method) {
		Class controller=this.getType();
		java.lang.reflect.Method[] javaMethods=controller.getMethods();
		List<java.lang.reflect.Method> matchedMethods=new ArrayList<>();
		for (java.lang.reflect.Method javaMethod : javaMethods) {
			if(javaMethod.getName().equals(method.getMethodName())) {
				matchedMethods.add(javaMethod);
			}
		}
		if(matchedMethods.size()==0) {
			return;
		} else if(matchedMethods.size()>1) {
			throw new RuntimeException("匹配到多个 "+method.getMethodName()+" 方法");
		}

		java.lang.reflect.Method m=matchedMethods.get(0);

		NormalAnnotationExpr src = null;

		MethodAnnotations sourceAnns=jcu.createMethodAnnotationsFromSource(m);
		// ApiOperation
		if(sourceAnns.getApiOperation()!=null) {
			src=sourceAnns.getApiOperation().getSource();
			StringLiteralExpr valueExpr=(StringLiteralExpr)jcu.getAnnotationPropertyValueExpr(src,"value","notes");
			if(valueExpr!=null) {
				String currOpName=valueExpr.getValue();
				String prevOpName=prevLiteralMap.getString("ApiOperation.value");
				// 如果未被修改
				if(prevOpName==null || currOpName.equals(prevOpName)) {
					String opName = method.getOperationName();
					if(!currOpName.equals(opName)) {
						valueExpr.setString(opName);
						method.getLiteralMap().put("ApiOperation.value", opName);
						isOpenAPIAnnotationsModified = true;
					}
					if(prevOpName==null) {
						method.getLiteralMap().put("ApiOperation.value", opName);
					}
				}
			}
		}
		//
		DBTableMeta tm= context.getTableMeta();

		ArrayInitializerExpr apiImplicitParamArray=null;
		List<DBColumnMeta> newColumns=new ArrayList<>();
		for (DBColumnMeta column : tm.getColumns()) {
			if(method.getSwaggerApiImplicitParamsMode()== Method.SwaggerApiImplicitParamsMode.ALL) {
				if (context.isDBTreatyFiled(column, true)) continue;
			} else if(method.getSwaggerApiImplicitParamsMode()== Method.SwaggerApiImplicitParamsMode.ID) {
				if(!column.isPK()) continue;
			} else {
				continue;
			}

			SwaggerAnnotationApiImplicitParam param = sourceAnns.getSwaggerAnnotationApiImplicitParam(column.getColumnVarName());
			if(param!=null) {
				if(apiImplicitParamArray==null) {
					apiImplicitParamArray = (ArrayInitializerExpr) param.getSource().getParentNode().get();
				}
				String currValue = param.getValue();
				Node n = jcu.getAnnotationPropertyValueExpr(param.getSource(), "name");
				String nameValue = n.toString();
				String prevValue = null;
				if(prevLiteralMap.getJSONObject(method.getMethodName())!=null) {
					prevValue = prevLiteralMap.getJSONObject(method.getMethodName()).getString("ApiImplicitParam." + nameValue + ".name");
				}
				if(prevValue==null || currValue.equals(prevValue)) {
					if(!currValue.equals(column.getLabel())) {
						StringLiteralExpr e = (StringLiteralExpr) jcu.getAnnotationPropertyValueExpr(param.getSource(), "value");
						e.setValue(column.getLabel());
						method.getLiteralMap().put("ApiImplicitParam." + nameValue + ".name", column.getLabel());
						isOpenAPIAnnotationsModified = true;
					}
					//
					if(prevValue==null) {
						method.getLiteralMap().put("ApiImplicitParam." + nameValue + ".name", column.getLabel());
					}
				}
			} else {
				newColumns.add(column);
			}
		}


		for (DBColumnMeta newColumn : newColumns) {
			NormalAnnotationExpr apiImplicitParam = new NormalAnnotationExpr();
			apiImplicitParam.setName("ApiImplicitParam");
			String consts = context.getVoMetaClassFile().getSimpleName() + "." +newColumn.getColumn().toUpperCase();
			apiImplicitParam.getPairs().add(new MemberValuePair("name", new StringLiteralExpr(consts)));
			apiImplicitParam.getPairs().add(new MemberValuePair("value", new StringLiteralExpr(newColumn.getLabel())));
			apiImplicitParam.getPairs().add(new MemberValuePair("required", new BooleanLiteralExpr(!newColumn.isNullable())));
			apiImplicitParam.getPairs().add(new MemberValuePair("dataTypeClass", new FieldAccessExpr(new NameExpr(newColumn.getDBDataType().getType().getSimpleName()),"class")));
			String example=context.getExampleStringValue(newColumn);
			if(example!=null) {
				apiImplicitParam.getPairs().add(new MemberValuePair("example", new StringLiteralExpr(example)));
			}
			apiImplicitParamArray.getValues().add(apiImplicitParam);
			isOpenAPIAnnotationsModified = true;
		}


	}

	private boolean isOpenAPIAnnotationsModified=false;
	@Override
	public boolean save() {
		boolean written = super.save();
		if(!written) {
			if (this.getType() != null) {
				isOpenAPIAnnotationsModified=false;
				String version = this.getVersion(this.getSourceFile());
				String source = this.updateSwagger(version);
				if(isOpenAPIAnnotationsModified) {
					FileUtil.writeText(this.getSourceFile(), source);
					System.err.println(this.getSimpleName() + "(" + version + ") 部分 OpenAPI 注解已经同步修改");
				}
			}
		}

		//
		for (Method method : this.getMethods()) {
			literalMap.put(method.getMethodName(),method.getLiteralMap());
		}
		FileUtil.writeText(this.literalFile, literalMap.toJSONString());

		return written;

	}
}
