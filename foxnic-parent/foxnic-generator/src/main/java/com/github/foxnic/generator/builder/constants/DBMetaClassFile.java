package com.github.foxnic.generator.builder.constants;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.config.GlobalSettings;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class DBMetaClassFile extends JavaClassFile {
	
	public static interface TableFilter {
		boolean filter(String table);
	}

	protected DefaultNameConvertor convertor = new DefaultNameConvertor();
	
	private DAO dao;

	private TableFilter tableFilter;
	
	public DBMetaClassFile(DAO dao,MavenProject domainProject,String constsPackage,String clsName) {
		super(domainProject, constsPackage+".db", clsName);
		this.dao=dao;
		this.addImport(DBField.class);
		this.addImport(DBTable.class);
		this.addImport(DBDataType.class);
	}
	
	public void appendAuthorAndTime(CodeBuilder code, int tabs) {
		code.ln(tabs," * @since "+DateUtil.getFormattedTime(false));
		code.ln(tabs," * @author "+GlobalSettings.instance().getAuthor());
	}
 
	
 
	
	@Override
	public void buildBody() {
 
		code.ln("/**");
		this.appendAuthorAndTime(code,0);
		code.ln(" * 数据库描述文件");
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
 
		code.ln("public class "+this.getSimpleName()+" {");
		
		String[] tables=dao.getTableNames();
		for (String table : tables) {
			 
			if(tableFilter!=null) {
				 if(!tableFilter.filter(table)) {
					 continue; 
				 }
			 }
			 buildTable(dao.getTableMeta(table));
		}
		code.ln("}");
	}
	
	private void buildTable(DBTableMeta tableMeta) {
 
		addJavaDoc(1,tableMeta.getComments());
		code.ln(1, "public static class "+tableMeta.getTableName().toUpperCase()+" extends DBTable {");
 
		addJavaDoc(2,"表名");
		code.ln(2, "public static final String $NAME = \""+tableMeta.getTableName()+"\";");
		
		List<String> fields=new ArrayList<>();
		List<DBColumnMeta> cms=tableMeta.getColumns();
		for (DBColumnMeta cm : cms) {
			addJavaDoc(2,cm.getComment());
			code.ln(2, "public static final DBField "+cm.getColumn().toUpperCase()+" = new DBField(DBDataType."+cm.getDBDataType().name()+" , \""+cm.getColumn()+"\",\""+cm.getColumnVarName()+"\",\""+cm.getLabel()+"\",\""+cm.getDetail()+"\","+cm.isPK()+","+cm.isAutoIncrease()+","+cm.isNullable()+");");
			fields.add(cm.getColumn().toUpperCase());
		}
		String fs=StringUtil.join(fields," , ");
		
		//构造函数
		code.ln(2,"");
		code.ln(2,"public "+tableMeta.getTableName().toUpperCase()+"() {");
		code.ln(3,	"this.init($NAME,\""+tableMeta.getComments()+"\" , "+fs+");");
		code.ln(2,"}");
		
//		//初始化数据
//		code.ln(2,"");
//		code.ln(2,"static { $TABLE = new "+tableMeta.getTableName().toUpperCase()+"(); }");
 
		
		code.ln(2, "public static final "+tableMeta.getTableName().toUpperCase()+" $TABLE=new "+tableMeta.getTableName().toUpperCase()+"();");
		
//		addJavaDoc(2,"表对象");
//		code.ln(2, "public static "+tableMeta.getTableName().toUpperCase()+" $TABLE() {");
//		code.ln(3, "return $TABLE;");
//		code.ln(2, "};");
		
		code.ln(1, "}");
 
	}

	public TableFilter getTableFilter() {
		return tableFilter;
	}

	public void setTableFilter(TableFilter tableFilter) {
		this.tableFilter = tableFilter;
	}
 
}
