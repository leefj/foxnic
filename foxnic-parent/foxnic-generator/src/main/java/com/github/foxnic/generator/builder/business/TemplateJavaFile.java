package com.github.foxnic.generator.builder.business;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
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
		template = engine.getTemplate(templateFilePath);
	}
	
	public CodeBuilder getClassJavaDoc() {
		CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * <p>");
		code.ln(" * "+context.getTableMeta().getComments()+" "+this.desc);
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
 
	
	public void save() {
		
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
		 this.putVar("isSimplePk", isSimplePk);
		 
		if(context.getVoClassFile().getIdsProperty()!=null) {
			this.putVar("idsPropertyConst", context.getVoClassFile().getIdsProperty().getNameConstants());
			this.putVar("idsPropertyName", context.getVoClassFile().getIdsProperty().name());
			this.putVar("idsPropertyType", context.getVoClassFile().getIdsProperty().type().getSimpleName());
		}
	 
		
		String source = template.renderToString(vars);
		source=processSource(source);
		File file=this.getSourceFile();
		
		WriteMode mode=context.overrides().getWriteMode(this.getClass());
		if(mode==WriteMode.COVER_EXISTS_FILE) {
			boolean autoCode=true;
			if(file.exists()) {
				autoCode=isAutoCode(file);
			}
			if(autoCode) {
				FileUtil.writeText(file, source);
			} else {
				System.err.println(this.getSimpleName()+" 已被开发人员修改，不再覆盖");
			}
		} else if(mode==WriteMode.WRITE_TEMP_FILE) {
			file=new File(file.getAbsolutePath()+".code");
			FileUtil.writeText(file, source);
		} else if(mode==WriteMode.CREATE_IF_NOT_EXISTS) {
			if(!file.exists()) {
				FileUtil.writeText(file, source);
			} else {
				//处理接口控制器
				if(this instanceof ApiControllerFile) {
					try {
						context.getCodePoint().replace(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
					context.getCodePoint().syncAll();
				}
			}
		}
	}

	private boolean isAutoCode(File file) {
		String content = null;
		try {
			CompilationUnit cu= StaticJavaParser.parse(file);
			JavadocComment comment=(JavadocComment)cu.getAllComments().get(0);
			content=comment.getContent();
		} catch (Exception e) {}

		Boolean autoCode=true;
		String prefix="* @version";
		String source=content;
		String[] lines=source.split("\n");
		for (String line : lines) {
			line=line.trim();
			if(line.startsWith(prefix))  {
				autoCode=false;
				break;
			}
		}
		return autoCode;
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
