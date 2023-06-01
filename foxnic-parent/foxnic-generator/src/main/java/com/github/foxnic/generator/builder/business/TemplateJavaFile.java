package com.github.foxnic.generator.builder.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.config.WriteMode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import java.io.File;

public class TemplateJavaFile extends JavaClassFile {

	protected ModuleContext context=null;
	//
	private static Engine engine=null;
	//
	protected Template template;
	private Kv vars = new Kv();
	//
//	private String templateFilePath;

	private String desc;

	private String templateFilePath;

	public TemplateJavaFile(ModuleContext context,MavenProject project, String packageName, String simpleName,String templateFilePath,String desc) {
		super(project, packageName, simpleName);
		this.context=context;
		this.desc=desc;
//		this.templateFilePath=templateFilePath;
		if(engine==null) {
			Engine.setFastMode(true);
			engine=new Engine();
			engine.setDevMode(true);
			engine.setToClassPathSourceFactory();
		}
		this.templateFilePath = templateFilePath;
		template = engine.getTemplate(templateFilePath);
	}

	public void refreshTemplate() {
		template = engine.getTemplate(this.templateFilePath);
	}

	public CodeBuilder getClassJavaDoc() {
		CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * <p>");
		code.ln(" * "+context.getTopic()+""+this.desc);
		code.ln(" * </p>");
		code.ln(" * @author "+context.getSettings().getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln("*/");
		code.ln("");
		return code;
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


	public boolean save() {

		this.putVar("package", this.getPackageName());
		this.putVar("simpleName", this.getSimpleName());

		this.buildBody();

		this.putVar("classJavaDoc", this.getClassJavaDoc());
		this.putVar("poVar", this.context.getPoClassFile().getVar());
		this.putVar("voSimpleName", this.context.getVoClassFile().getSimpleName());
		this.putVar("voVar", this.context.getVoClassFile().getVar());
		this.putVar("poSimpleName", this.context.getPoClassFile().getSimpleName());
		this.putVar("poListVar", this.context.getPoClassFile().getVar()+"List");

		this.putVar("imports",  StringUtil.join(this.imports,"\n"));
		this.putVar("topic", this.context.getTopic());

		this.putVar("author",context.getSettings().getAuthor());


		DBTableMeta tableMeta=this.context.getTableMeta();

		boolean isSimplePk=false;

		 if(tableMeta.getPKColumnCount()==1) {
			 DBColumnMeta pk=tableMeta.getPKColumns().get(0);
			 this.putVar("pkType", pk.getDBDataType().getType().getSimpleName());
			 this.putVar("idPropertyConst", context.getPoClassFile().getIdProperty().getNameConstants());
			 this.putVar("idPropertyName", context.getPoClassFile().getIdProperty().name());
			 this.putVar("idPropertyType", context.getPoClassFile().getIdProperty().type().getSimpleName());
			 this.addImport(pk.getDBDataType().getType());
			 isSimplePk=true;
		 }

		if(context.getVoClassFile().getIdsProperty()!=null) {
			this.putVar("idsPropertyConst", context.getVoClassFile().getIdsProperty().getNameConstants());
			this.putVar("idsPropertyName", context.getVoClassFile().getIdsProperty().name());
			this.putVar("idsPropertyType", context.getVoClassFile().getIdsProperty().type().getSimpleName());
		}

		JSONArray pks=new JSONArray();
		for (DBColumnMeta pk : tableMeta.getPKColumns()) {
			JSONObject pkcol=new JSONObject();
			pkcol.put("pkType", pk.getDBDataType().getType().getSimpleName());
			pkcol.put("idPropertyConst", context.getPoClassFile().getProperty(pk).getNameConstants());
			pkcol.put("idPropertyName", context.getPoClassFile().getProperty(pk).name());
			pkcol.put("idPropertyType", context.getPoClassFile().getProperty(pk).type().getSimpleName());
			this.addImport(pk.getDBDataType().getType());
			pks.add(pkcol);
		}

		this.putVar("pks", pks);
		this.putVar("isSimplePk", isSimplePk);




		String source = template.renderToString(vars);
		source=processSource(source);
		File file=this.getSourceFile();
		boolean written = false;
		WriteMode mode=context.overrides().getWriteMode(this.getClass());
		if(mode==WriteMode.COVER_EXISTS_FILE) {
			String version=null;
			if(file.exists()) {
				version=getVersion(file);
			}
			if(version==null) {
				FileUtil.writeText(file, source);
				written=true;
			} else {
				System.err.println(this.getSimpleName()+"("+version+") 已被开发人员修改，不再覆盖");
			}
		} else if(mode==WriteMode.WRITE_TEMP_FILE) {
			file=new File(file.getAbsolutePath()+".code");
			FileUtil.writeText(file, source);
			written=true;
		} else if(mode==WriteMode.CREATE_IF_NOT_EXISTS) {
			if(!file.exists()) {
				FileUtil.writeText(file, source);
				written=true;
			}
		}

		return written;

	}



	public String getVersion(File file) {
		String content = null;
		try {
			CompilationUnit cu= StaticJavaParser.parse(file);
			if(!cu.getAllComments().isEmpty()) {
				JavadocComment comment = (JavadocComment) cu.getAllComments().get(0);
				content=comment.getContent();
			}
		} catch (Exception e) {
			Logger.exception(e);
		}
		if(content==null) {
			return null;
		}

		String version=null;
		String prefix="* @version";
		String source=content;
		String[] lines=source.split("\n");
		for (String line : lines) {
			line=line.trim();
			if(line.startsWith(prefix))  {

				version=line;
				version=version.trim();
				version=StringUtil.removeFirst(version,"*");
				version=version.trim();
				return version;
			}
		}
		return null;
	}

	protected String processSource(String source) {
		return source;
	}

	@Override
	public void save(boolean override) {
		this.save();
	}

	public ModuleContext getContext() {
		return context;
	}




}
