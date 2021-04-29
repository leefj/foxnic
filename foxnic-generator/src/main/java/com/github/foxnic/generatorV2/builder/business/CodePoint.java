package com.github.foxnic.generatorV2.builder.business;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.spec.DAO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodePoint {
	
	private String table;
	private DAO dao;
	private Map<String,String> oldPoints=new HashMap<>();
	private Map<String,String> newPoints=new HashMap<>();
	private Map<String,List<String>> apiImplicitParams=new HashMap<>();
	private File jsonFile = null;
	public CodePoint(String table,DAO dao) {


		this.table=table;
		this.dao = dao;

		StackTraceElement[] els=(new Throwable()).getStackTrace();
		StackTraceElement e=els[els.length-1];
		String bootClsName= BeanUtil.getFieldValue(e,"declaringClass",String.class);
		Class bootCls= ReflectUtil.forName(bootClsName);
		MavenProject mp=new MavenProject(bootCls);
		jsonFile = FileUtil.resolveByPath(mp.getProjectDir(),"autocode",dao.getUserName(),table+".json");
		JSONObject json=FileUtil.readJSONobject(jsonFile);
		if(json!=null) {
			JSONObject locations=json.getJSONObject("locations");
			if(locations!=null) {
				for (String location:locations.keySet()) {
					oldPoints.put(location, locations.getString(location));
				}
			}
		}

//		RcdSet rs=dao.query("select * from sys_codepoint where catalog=?",table);
//		for (Rcd r : rs) {
//			oldPoints.put(r.getString("location"), r.getString("code"));
//		}
	}


	
	public String getCodeInLog(String location) {
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
	
	
	
	public void syncAll() {
//		BatchParamBuilder pb=new BatchParamBuilder();
//		for (String location : newPoints.keySet()) {
//			pb.add(table,location,newPoints.get(location),new Date());
//		}
//		dao.execute("delete from sys_codepoint where catalog=?",table);
//		dao.batchExecute("insert into sys_codepoint(catalog,location,code,gern_time) values(?,?,?,?)",pb.getBatchList());
		JSONObject json=new JSONObject();
		json.put("locations",newPoints);
		FileUtil.writeText(jsonFile,json.toJSONString());

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
