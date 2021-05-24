package com.github.foxnic.generatorV2.builder.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generatorV2.builder.view.model.FieldInfo;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.github.foxnic.generatorV2.config.WriteMode;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

public abstract class TemplateViewFile {
	
	protected BeanNameUtil beanNameUtil = new BeanNameUtil();
	
	protected MduCtx context;
	
	private static Engine engine=null;
	
	protected String code;
	
	private Kv vars = new Kv();
	
	protected Template template;
	
	protected MavenProject project;
	
	protected String pathPrefix;
 
	public TemplateViewFile(MduCtx context,MavenProject project,String pathPrefix,String templateFilePath) {
		this.context=context;
		this.project=project;
		this.pathPrefix=pathPrefix;
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
		 
		 view.putVar("isSimplePK", isSimplePK);
		 
		if(view.context.getVoClassFile().getIdsProperty()!=null) {
			view.putVar("idsPropertyConst", view.context.getVoClassFile().getIdsProperty().getNameConstants());
			view.putVar("idsPropertyName", view.context.getVoClassFile().getIdsProperty().name());
			view.putVar("idsPropertyType", view.context.getVoClassFile().getIdsProperty().type().getSimpleName());
		}

	}
	
	public  void applyCommonVars4List(TemplateViewFile view) {
		
		String idPrefix=beanNameUtil.depart(view.context.getPoClassFile().getSimpleName()).toLowerCase();
		view.putVar("searchFieldId", idPrefix+"-search-field");
 

		DBTableMeta tableMeta=view.context.getTableMeta();
		List<DBColumnMeta> columns=tableMeta.getColumns();
		List<FieldInfo> searchOptions=new ArrayList<>();
		for (DBColumnMeta cm : columns) {
			if(this.context.isDBTreatyFiled(cm)) continue;
			searchOptions.add(new FieldInfo(cm));
		}
		this.putVar("searchFields", searchOptions);
		
	}
	
	public static void applyCommonVars4Form(TemplateViewFile view) {
		 
	}
	
 
	public void save() {
 
		
		applyCommonVars(this);
		
		String source = template.renderToString(vars);
		source=processSource(source);
		File file=this.getSourceFile();
		
		
		WriteMode mode=context.overrides().getWriteMode(this.getClass());
		if(mode==WriteMode.WRITE_DIRECT) {
			FileUtil.writeText(file, source);
		} else if(mode==WriteMode.WRITE_TEMP_FILE) {
			file=new File(file.getAbsolutePath()+".code");
			FileUtil.writeText(file, source);
		} else if(mode==WriteMode.DO_NOTHING) {
			if(!file.exists()) {
				FileUtil.writeText(file, source);
			}
		}
 
	}

	 
	protected abstract File getSourceFile();

	protected String processSource(String source) {
		return source;
	}
	

}
