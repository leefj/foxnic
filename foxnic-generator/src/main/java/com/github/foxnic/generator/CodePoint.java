package com.github.foxnic.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.clazz.ControllerMethodReplacer;
import com.github.foxnic.sql.parameter.BatchParamBuilder;

public class CodePoint {
	
	private String table;
	private DAO dao;
	private Map<String,String> oldPoints=new HashMap<>();
	private Map<String,String> newPoints=new HashMap<>();
	private Map<String,List<String>> apiImplicitParams=new HashMap<>();
	public CodePoint(String table,DAO dao) {
		this.table=table;
		this.dao = dao;
		RcdSet rs=dao.query("select * from sys_codepoint where catalog=?",table);
		for (Rcd r : rs) {
			oldPoints.put(r.getString("location"), r.getString("code"));
		}
	}
	
	public String getOldCode(String location) {
		return oldPoints.get(location);
	}
	
	public String getNewCode(String location) {
		return newPoints.get(location);
	}
	
	public void addApiImplicitParam(String location,String apiImplicitParamCode) {
		List<String> aps=apiImplicitParams.get(location);
		if(aps==null) {
			aps=new ArrayList<>();
			apiImplicitParams.put(location, aps);
		}
		aps.add(apiImplicitParamCode);
	}
	
	public List<String> getApiImplicitParams(String location) {
		return apiImplicitParams.get(location);
	}
	
	
	public void set(String location,String code) {
		newPoints.put(location, code);
	}
	
	
	
	public void sync() {
		BatchParamBuilder pb=new BatchParamBuilder();
		for (String location : newPoints.keySet()) {
			pb.add(table,location,newPoints.get(location),new Date());
		}
		dao.execute("delete from sys_codepoint where catalog=?",table);
		dao.batchExecute("insert into sys_codepoint(catalog,location,code,gern_time) values(?,?,?,?)",pb.getBatchList());
	}
	
	
	private List<ControllerMethodReplacer>  replacers=new ArrayList<>();

	public void addReplacer(ControllerMethodReplacer controllerMethodReplacer) {
		replacers.add(controllerMethodReplacer);
	}

	public void replace(File sourceFile) throws Exception { 
		
		for (ControllerMethodReplacer controllerMethodReplacer : replacers) {
			controllerMethodReplacer.replace(sourceFile);
		}
		
	}
	
	
	

	 
	
	

}
