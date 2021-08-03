package com.github.foxnic.generator.builder.view;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.InputType;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.config.TreeConfig;
import com.github.foxnic.generator.config.WriteMode;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import java.io.File;
import java.util.*;

public abstract class TemplateViewFile {
	
	protected BeanNameUtil beanNameUtil = new BeanNameUtil();
	
	protected ModuleContext context;
	
	private static Engine engine=null;
	
	protected String code;
	
	private Kv vars = new Kv();
	
	protected Template template;
	
	protected MavenProject project;
	
	protected String pathPrefix;
	protected String uriPrefix;
 
	public TemplateViewFile(ModuleContext context,String templateFilePath) {
		this.context=context;
		this.project=context.getViewProject();
		this.pathPrefix=context.getViewPrefixPath();
		this.uriPrefix=context.getViewPrefixURI();
		//初始化模版引擎
		if(engine==null) {
			Engine.setFastMode(true);
			engine=new Engine();
			engine.setDevMode(true);
			engine.setToClassPathSourceFactory();
		}
		template = engine.getTemplate(templateFilePath);
	}
	
	public void putVar(String name,String value) {
		vars.put(name, value);
	}
	
	public void putVar(String name,Object value) {
		vars.put(name, value);
	}
	
	public void putVar(String name,CodeBuilder value) {
		String str=value.toString();
		str=StringUtil.removeLast(str, "\n");
		str=StringUtil.removeLast(str, "\r");
		vars.put(name, str);
	}
	
	
	public void applyCommonVars(TemplateViewFile view) {
		
		CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * "+view.context.getTopic()+" 列表页 JS 脚本");
		code.ln(" * @author "+view.context.getSettings().getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" */");
		view.putVar("authorAndTime", code);
		
		view.putVar("topic", view.context.getTopic());
		
		DBTableMeta tableMeta=view.context.getTableMeta();
		
		boolean isSimplePK=false;
		 
		 if(tableMeta.getPKColumnCount()==1) {
			 DBColumnMeta pk=tableMeta.getPKColumns().get(0);
			 view.putVar("pkType", pk.getDBDataType().getType().getSimpleName());
			 view.putVar("idPropertyConst", view.context.getPoClassFile().getIdProperty().getNameConstants());
			 view.putVar("idPropertyName", view.context.getPoClassFile().getIdProperty().name());
			 view.putVar("idPropertyType", view.context.getPoClassFile().getIdProperty().type().getSimpleName());
			 isSimplePK=true;
		 }

		view.putVar("authPrefix", tableMeta.getTableName().toLowerCase());
		view.putVar("isSimplePK", isSimplePK);
		 
		if(view.context.getVoClassFile().getIdsProperty()!=null) {
			view.putVar("idsPropertyConst", view.context.getVoClassFile().getIdsProperty().getNameConstants());
			view.putVar("idsPropertyName", view.context.getVoClassFile().getIdsProperty().name());
			view.putVar("idsPropertyType", view.context.getVoClassFile().getIdsProperty().type().getSimpleName());
		}
		
		this.putVar("formDataKey", context.getTableMeta().getTableName().toLowerCase().replace('_', '-')+"-form-data");
		this.putVar("formAreaKey", context.getTableMeta().getTableName().toLowerCase().replace('_', '-')+"-form-area");
		
		TreeConfig tree=view.context.tree();
		this.putVar("isTree", tree!=null);
		
		
		
		//
		this.putVar("moduleURL", this.context.getControllerProxyFile().getModulePrefixURI());

		//
		this.putVar("formWindow", this.context.getFormWindowConfig());

	}
	
	public  void applyCommonVars4List(TemplateViewFile view) {
		
		String idPrefix=beanNameUtil.depart(view.context.getPoClassFile().getSimpleName()).toLowerCase();
		view.putVar("searchFieldId", idPrefix+"-search-field");

		List<String[]> layout = context.getSearchAreaConfig().getInputLayout();

		if(layout!=null) {
			updateSearchInputHidden(layout,this.context.getFields());
			updateSearchInputLocation(layout,this.context.getFields());
		}

		boolean hasLogicField=false;
		List<List<FieldInfo>> searchRows=new ArrayList<>();
		Map<Integer,List<FieldInfo>> searchRowsMap=new HashMap<>();
		List<FieldInfo> searchFields=new ArrayList<>();
		//先按序号简单分组
		for (FieldInfo f : this.context.getFields()) {
			if(f.isDBTreatyFiled()) continue;
			if(f.isHideInSearch()) continue;
			searchFields.add(f);
 			List<FieldInfo> row=searchRowsMap.get(f.getSearch().getRowIndex());
			if(row==null) {
				row=new ArrayList<>();
				searchRowsMap.put(f.getSearch().getRowIndex(),row);
			}
			row.add(f);
			if(f.getType()== InputType.LOGIC_SWITCH) {
				hasLogicField=true;
			}
		}

		//
		List<Integer> keys=new ArrayList<>();
		keys.addAll(searchRowsMap.keySet());
		Collections.sort(keys);

		for (Integer key : keys) {
			List<FieldInfo> inputs=searchRowsMap.get(key);
			inputs.sort((a,b)->{
				if(a.search().getColumnIndex()>b.search().getColumnIndex()) return 1;
				else if(a.search().getColumnIndex()<b.search().getColumnIndex()) return -1;
				else return 0;
			});
			searchRows.add(inputs);
		}




		this.putVar("searchFields", searchFields);
		this.putVar("searchRows", searchRows);
		this.putVar("hasLogicField", hasLogicField);


		
		this.putVar("formURI", this.context.getFormPageHTMLFile().getFullURI());
		
		
		TreeConfig tree=view.context.tree();
		List<FieldInfo> fields=this.context.getTemplateFields();
		List<FieldInfo> listFields=new ArrayList<FieldInfo>();
		for (FieldInfo f : fields) {
//			if(f.isHideInList() && !f.isPK()) continue;
			//不显示常规字段
			if(f.isDBTreatyFiled()  && !context.getDAO().getDBTreaty().getCreateTimeField().equals(f.getColumn())) continue;
			//不显示自增主键
			if(f.isPK() && f.isAutoIncrease()) continue;
			//不显示上级ID
			if(tree!=null && tree.getParentIdField().name().equalsIgnoreCase(f.getColumn()))  continue;
			
//			String templet="";
//			if(f.getColumnMeta().getDBDataType()==DBDataType.DATE) {
//				templet=" , templet: function (d) { return fox.dateFormat(d."+f.getVarName()+"); }";
//			} else if(f.isImageField()) {
//				templet=" , templet: function (d) { return '<img width=\"50px\" height=\"50px\" onclick=\"window.previewImage(this)\"  src=\"/service-tailoring/sys-file/download?id='+ d."+f.getVarName()+"+'\" />'; }";
//			} else if(f.isLogicField()) {
//				templet=", templet: '#cell-tpl-"+f.getVarName()+"'";
//			}
//			f.setTemplet(templet);

			listFields.add(f);
			
		}
		//所有数据库字段
		this.putVar("fields", listFields);

		//
		List<String> pkvs=new ArrayList<>();
		List<String> qkvs=new ArrayList<>();
		for (FieldInfo f : fields) {
			if(!f.isPK()) continue;
			pkvs.add( f.getVarName()+" : data."+f.getVarName());
			qkvs.add(  "'"+f.getVarName()+"=' + data."+f.getVarName());
		}
		this.putVar("paramJson", StringUtil.join(pkvs," , "));
		this.putVar("paramQueryString", StringUtil.join(qkvs,"&"));
		
	}

	/**
	 * 根据布局设置搜索的隐藏
	 * */
	protected void updateSearchInputHidden(List<String[]> layout, List<FieldInfo> fields) {
		Set<String> fieldNames=new HashSet<>();
		for (int rowIndex = 0; rowIndex < layout.size(); rowIndex++) {
			String[] columns=layout.get(rowIndex);
			for (int columnIndex = 0; columnIndex <columns.length ; columnIndex++) {
				String cName=columns[columnIndex];
				fieldNames.add(cName);
			}
		}
		for (FieldInfo field : fields) {
			if(!fieldNames.contains(field.getColumn()) && !fieldNames.contains(field.getVarName())) {
				field.hideInSearch();
			}
		}
	}

	/**
	 * 通过布局来设置搜索区域的序号
	 * */
	protected void updateSearchInputLocation(List<String[]> layout, List<FieldInfo> fields) {
		for (int rowIndex = 0; rowIndex < layout.size(); rowIndex++) {
			String[] columns=layout.get(rowIndex);
			for (int columnIndex = 0; columnIndex <columns.length ; columnIndex++) {
				String cName=columns[columnIndex];
				FieldInfo fi=findFieldInfo(fields,cName);
				fi.search().setRowIndex(rowIndex);
				fi.search().setColumnIndex(columnIndex);
			}
		}
	}

	protected FieldInfo findFieldInfo(List<FieldInfo> fields, String cName)
	{
		for (FieldInfo field : fields) {
			if(field.getColumn().equals(cName) || field.getVarName().equals(cName)){
				return field;
			}
		}
		throw new RuntimeException(cName+" 不存在");
	}


	public void applyCommonVars4Form(TemplateViewFile view) {
		
		TreeConfig tree=view.context.tree();
		List<FieldInfo> fields=this.context.getTemplateFields();
		List<FieldInfo> formFields=new ArrayList<FieldInfo>();
		List<FieldInfo> hiddenFields=new ArrayList<>();
		boolean hasUploadField=false;
		for (FieldInfo f : fields) {
//			if(f.isHideInForm() && !f.isPK()) continue;
			//不显示常规字段
			if(f.isDBTreatyFiled()) continue;
			//不显示自增主键
			else if(f.isPK() || f.isAutoIncrease()) {
				hiddenFields.add(f);
				continue;
			} 
			//不显示上级ID
			else if(tree!=null && tree.getParentIdField().name().equalsIgnoreCase(f.getColumn())) { 
				continue;
			}

			if(f.getType()==InputType.UPLOAD) {
				hasUploadField=true;
			}
 
			formFields.add(f);
			
		}
		//所有数据库字段
		this.putVar("fields", formFields);
		this.putVar("hiddenFields", hiddenFields);
		this.putVar("hasUploadField", hasUploadField);


		 
		this.putVar("jsURI", this.context.getFormPageJSFile().getFullURI());



	}
	
 
	public void save() {
 
		
		applyCommonVars(this);
		
		String source = template.renderToString(vars);
		source=processSource(source);
		File file=this.getSourceFile();
		
		
		WriteMode mode=context.overrides().getWriteMode(this.getClass());
		if(mode==WriteMode.COVER_EXISTS_FILE) {
			FileUtil.writeText(file, source);
		} else if(mode==WriteMode.WRITE_TEMP_FILE) {
			file=new File(file.getAbsolutePath()+".code");
			FileUtil.writeText(file, source);
		} else if(mode==WriteMode.CREATE_IF_NOT_EXISTS) {
			if(!file.exists()) {
				FileUtil.writeText(file, source);
			}
		}
 
	}

	 
 
	protected File getSourceFile() {
		File file=FileUtil.resolveByPath(this.project.getMainResourceDir(),pathPrefix,this.getSubDirName(),getFileName());
		return file;
	}

	protected String processSource(String source) {
		return source;
	}
	
	protected String getSubDirName() {
		String str=beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase();
		return str;
	}
	
	protected abstract String getFileName();
	
	protected String getFullURI() {
		this.uriPrefix=StringUtil.removeFirst(this.uriPrefix,"/");
		this.uriPrefix=StringUtil.removeLast(this.uriPrefix,"/");
		return "/"+StringUtil.joinUrl(this.uriPrefix,this.getSubDirName(),this.getFileName());
	}


}
