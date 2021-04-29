package com.github.foxnic.generatorV2.builder.business;

import java.io.File;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.github.foxnic.generatorV2.config.WriteMode;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import net.bytebuddy.asm.Advice.This;

public class TemplateJavaFile extends JavaClassFile {

	protected MduCtx context=null;
	//
	private static Engine engine=null;
	//
	protected Template template;
	private Kv vars = new Kv();
	//
	private String templateFilePath;
	
	private String desc;
	
	public TemplateJavaFile(MduCtx context,MavenProject project, String packageName, String simpleName,String templateFilePath,String desc) {
		super(project, packageName, simpleName);
		this.context=context;
		this.desc=desc;
		this.templateFilePath=templateFilePath;
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
		this.putVar("poListVar", this.context.getPoClassFile().getVar()+"List");
		
		this.putVar("imports",  StringUtil.join(this.imports,"\n"));
		this.putVar("topic", this.context.getTopic());
		
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
	
	protected String processSource(String source) {
		return source;
	}

	@Override
	public void save(boolean override) {
		this.save();
	}

	public MduCtx getContext() {
		return context;
	}
	 

	
	
}
