package com.github.foxnic.commons.support.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Demo extends DemoBase  {

	private String name=null;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String[] getStrArr() {
		return strArr;
	}

	public void setStrArr(String[] strArr) {
		this.strArr = strArr;
	}

	private Integer age=null;
	
	private String[] strArr=null;
	
	private List<String> strList=null;
	
	private ArrayList<Integer> intList=null;
	
	public ArrayList<Integer> getIntList() {
		return intList;
	}

	public void setIntList(ArrayList<Integer> intList) {
		this.intList = intList;
	}

	private String familyName=null;
	 

	public List<String> getStrList() {
		return strList;
	}

	public void setStrList(List<String> strList) {
		this.strList = strList;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	private Map<String,Integer> m1=null;
	
	public Map<String, Integer> getM1() {
		return m1;
	}

	public void setM1(Map<String, Integer> m1) {
		this.m1 = m1;
	}
	
	private Demo parent=null;
	
	public Demo getParent() {
		return parent;
	}

	public void setParent(Demo parent) {
		this.parent = parent;
	}
	
	
	
	
	
}
