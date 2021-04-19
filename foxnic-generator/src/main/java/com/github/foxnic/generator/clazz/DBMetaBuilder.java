package com.github.foxnic.generator.clazz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.CodeGenerator;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;

public class DBMetaBuilder {

	protected DefaultNameConvertor convertor = new DefaultNameConvertor();
	private DAO dao;
	private CodeGenerator generator;
	private String clsName;
	public DBMetaBuilder(CodeGenerator generator,DAO dao,String clsName) {
		this.dao=dao;
		this.generator=generator;
		this.clsName=clsName;
	}
	
	public void appendAuthorAndTime(CodeBuilder code, int tabs) {
//		code.ln(tabs," * @author "+generator.getAuthor());
		code.ln(tabs," * @since "+DateUtil.getFormattedTime(false));
	}
 
	 
	public void build() {
		
		CodeBuilder code=new CodeBuilder();
		 
		code.ln("package "+generator.getConstsPackage()+".db;");
		code.ln("import com.github.foxnic.sql.meta.DBField;");
		code.ln("import com.github.foxnic.sql.meta.DBTable;");
		code.ln("");
		code.ln("");
		
	
		code.ln("/**");
		this.appendAuthorAndTime(code,0);
		code.ln(" * 数据库描述文件");
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
 
		code.ln("public class "+clsName+" {");
		
		String[] tables=dao.getTableNames();
		for (String table : tables) {
			 buildTable(code,dao.getTableMeta(table));
		}
 
		code.ln("}");
		
		File file=generator.getDomainProject().getMainSourceDir();
		file=FileUtil.resolveByPath(file, generator.getConstsPackage().replace('.', '/')+"/db",clsName+".java");
		code.wirteToFile(file);
		
	}
	
	private void buildTable(CodeBuilder code,DBTableMeta tableMeta) {
		
		
		
		 
		//addJavaDoc(1,code,tableMeta.getComments());
		//code.ln(1, "public static final String "+tableMeta.getTableName().toUpperCase()+" = \""+tableMeta.getTableName()+"\";");
		
		addJavaDoc(1,code,tableMeta.getComments());
		code.ln(1, "public static class "+tableMeta.getTableName().toUpperCase()+" extends DBTable {");
		
		addJavaDoc(2,code,"表名");
		code.ln(2, "public static final String $NAME = \""+tableMeta.getTableName()+"\";");
		
		List<String> fields=new ArrayList<>();
		List<DBColumnMeta> cms=tableMeta.getColumns();
		for (DBColumnMeta cm : cms) {
			addJavaDoc(2,code,cm.getComment());
			code.ln(2, "public static final DBField "+cm.getColumn().toUpperCase()+" = new DBField(\""+cm.getColumn()+"\",\""+cm.getColumnVarName()+"\",\""+cm.getLabel()+"\",\""+cm.getDetail()+"\");");
			fields.add(cm.getColumn().toUpperCase());
		}
		String fs=StringUtil.join(fields," , ");
		
		code.ln(2,"");
		code.ln(2,"public "+tableMeta.getTableName().toUpperCase()+"() {");
		code.ln(3,	"this.init($NAME,\""+tableMeta.getComments()+"\" , "+fs+");");
		code.ln(2,"}");
		code.ln(2,"static {new "+tableMeta.getTableName().toUpperCase()+"();}");
		
		code.ln(1, "}");
		
		
		 
		
		
	}

	private void addJavaDoc(int tabs,CodeBuilder code,String... doc) {
 
		code.ln(tabs,"");
		code.ln(tabs,"/**");
		for (int i = 0; i <doc.length ; i++) {
			if(StringUtil.isBlank(doc[i])) continue;
			code.ln(tabs," * "+doc[i]+(i<doc.length?"":""));
		}
		code.ln(tabs,"*/");
	}

	 
	
	

}
