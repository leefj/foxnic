package com.github.foxnic.generator;

import java.io.File;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.clazz.AgentBuilder;
import com.github.foxnic.generator.clazz.ControllerBuilder;
import com.github.foxnic.generator.clazz.PoBuilder;
import com.github.foxnic.generator.clazz.ServiceImplBuilder;
import com.github.foxnic.generator.clazz.ServiceInterfaceBuilder;
import com.github.foxnic.generator.clazz.VoBuilder;

/**
 *  
 */
public class CodeGenerator {

	private String author;
	//private String apiPrefix;
	private DAO dao;
	
	private File destination=null;
	
	
	public CodeGenerator(DAO dao) {
		this.dao=dao;
	}

//	public CodeGenerator setApiPrefix(String prefix) {
//		this.apiPrefix = prefix;
//		return this;
//	}

	public CodeGenerator setAuthor(String author) {
		this.author = author;
		return this;
	}
 
	public void build(String tableName, String tablePrefix,Config config)
			throws Exception {
 
		//Rcd example=dao.queryRecord("select * from "+tableName);
		
		Context context = new Context(this,config,dao.getDBTreaty(),tableName, tablePrefix, dao.getTableMeta(tableName));

		//构建 PO
		(new PoBuilder(context)).buildAndUpdate();
		//构建 VO
//		(new VoBuilder(context)).buildAndUpdate();
		//服务接口
//		(new ServiceInterfaceBuilder(context)).buildAndUpdate();
		//服务实现类
//		(new ServiceImplBuilder(context)).buildAndUpdate();
		//服务实现类
//		(new AgentBuilder(context)).buildAndUpdate();
		//服务实现类
//		(new ControllerBuilder(context)).buildAndUpdate();

	}

	public File getDestination() {
		return destination;
	}

	public void setDestination(File destination) {
		this.destination = destination;
	}
 
}
