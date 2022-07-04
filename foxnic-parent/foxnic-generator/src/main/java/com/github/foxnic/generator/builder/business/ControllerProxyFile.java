package com.github.foxnic.generator.builder.business;

import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.business.method.DeleteById;
import com.github.foxnic.generator.builder.business.method.GetById;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.util.JavaCPUnit;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ControllerProxyFile extends TemplateJavaFile {


	public static final String CATALOG=
			"新建:create:/insert,/save,_form.html;" +
					"删除:delete:/delete;" +
					"批量删除:delete-by-ids:/delete-by-ids;" +
					"更新:update:/update;" +
					"保存:save:/update,/save;" +
					"查询列表:query:/get-by-ids,/query-list,/query-paged-list;" +
					"查看表单:view-form:/get-by-id,/get-by-ids,_form.html;" +
					"导出:export:/export-excel;" +
					"导入:import:/export-excel-template,/import-excel";

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
		this.addImport(List.class);
		this.addImport(Result.class);
		this.addImport(PagedList.class);


		this.putVar("poSimpleName", this.getContext().getPoClassFile().getSimpleName());

		this.putVar("bpm", !this.getContext().getBpmConfig().getIntegrateMode().equals("none"));
		this.putVar("bpmFormCode", this.getContext().getBpmConfig().getFormCode());


		this.putVar("isEnableImportExcel", this.context.getListConfig().isEnableImportExcel());
		this.putVar("isEnableExportExcel", this.context.getListConfig().isEnableExportExcel());


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

		DeleteById deleteById=new DeleteById(this.context,this);
		this.putVar("controllerMethodParameterDeclare4DeleteById", deleteById.getControllerMethodParameterDeclare());

		GetById getById=new GetById(context,this);
		this.putVar("controllerMethodParameterDeclare4GetById", getById.getControllerMethodParameterDeclare());


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

		this.putVar("batchInsert", this.context.getControllerConfig().getEnableBatchInsert());


	}

	@Override
	public String getVar() {
		return this.getContext().getPoClassFile().getVar()+"Service";
	}

	@Override
	public void save() {
		super.save();
		processParamNames();
	}

	private void processParamNames() {
		if(this.getSourceFile()!=null && this.getSourceFile().exists()) {
			insertParameterNames(this.getSourceFile());
		}
	}


	public static void insertParameterNames(File javaFile) {

		JavaCPUnit cpUnit = new JavaCPUnit(javaFile);
		CompilationUnit compilationUnit=cpUnit.getCompilationUnit();
		List<ClassOrInterfaceDeclaration> classes = cpUnit.find(ClassOrInterfaceDeclaration.class);
		ClassOrInterfaceDeclaration clz=classes.get(0);
		Optional<AnnotationExpr> feignClient =clz.getAnnotationByName("FeignClient");
		if(!feignClient.isPresent()) return;
		NodeList<ImportDeclaration> imports = compilationUnit.getImports();
		boolean flag=false;
		for (ImportDeclaration imp : imports) {
			if(imp.getNameAsString().equals(RequestParam.class.getName())) {
				flag=true;
				break;
			}
		}
		if(!flag) {
			compilationUnit.addImport(RequestParam.class);
		}
		List<MethodDeclaration> list=cpUnit.find(MethodDeclaration.class);
		boolean isModified=false;
		for (MethodDeclaration m : list) {

			Optional<AnnotationExpr> requestMapping =m.getAnnotationByClass(RequestMapping.class);
			if(requestMapping==null || !requestMapping.isPresent()) continue;

			//移除
			NodeList<AnnotationExpr> anns= m.getAnnotations();
			for (AnnotationExpr ann : anns) {
				if(ann.getName().asString().equals("ParameterNames")) {
					anns.remove(ann);
					break;
				}
			}

			// 增加参数注解
			NodeList<Parameter> ps=m.getParameters();
			for (Parameter p : ps) {
				Optional<AnnotationExpr> requestParamAnn=p.getAnnotationByClass(RequestParam.class);
				if(requestParamAnn==null || !requestParamAnn.isPresent()) {
					NormalAnnotationExpr requestParamAnnn= p.addAndGetAnnotation(RequestParam.class);
					requestParamAnnn.addPair("name","\""+ p.getNameAsString() +"\"");
					isModified = true;
				}
			}

		}

		if(isModified) {
			cpUnit.save();
		}

	}



}
