package com.github.foxnic.generator.builder.business;

import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.lang.DateUtil;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceImplementSubFile extends JavaClassFile {

	private  ServiceImplementFile implementFile =null;
	private ModuleContext context = null;

	private String desc;

	public ServiceImplementSubFile(MavenProject project, ServiceImplementFile implementFile, String simpleName,String desc) {
		super(project, implementFile.getPackageName(), simpleName);
		this.implementFile=implementFile;
		this.context=implementFile.getContext();
		this.desc=desc;
	}

	public void addClassJavaDoc() {

		code.ln("/**");
		code.ln(" * <p>");
		code.ln(" * "+context.getTopic()+" , "+this.desc);
		code.ln(" * </p>");
		code.ln(" * @author "+context.getSettings().getAuthor());
		code.ln(" * @since "+ DateUtil.getFormattedTime(false));
		code.ln("*/");
		code.ln("");

	}

	@Override
	protected void buildBody() {
		this.addImport(Service.class);

		this.addClassJavaDoc();

		this.code.ln("@Service(\""+this.getSimpleName()+"\")");
		this.code.ln("public class "+this.getSimpleName()+" extends  "+implementFile.getSimpleName()+" {");
		this.code.ln(1,"// 请在此处按需覆盖父类方法");
		this.code.ln("}");

	}



}
