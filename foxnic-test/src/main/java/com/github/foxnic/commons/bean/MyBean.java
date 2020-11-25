package com.github.foxnic.commons.bean;

import java.util.Date;

public class MyBean extends BaseBean
{
	public static final String NAME="this is name";
	public static final String NIC_NAME="this is nic name";
	private static String STATIC_VALUE_2="STATIC_VALUE_2";
	
	private String nicName;
	
	public String getNicName() {
		return nicName;
	}
	public void setNicName(String nicName) {
		this.nicName = nicName;
	}

	private String name;
	private Date birthday;
	
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
	private int age=0;
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	private double weight;
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	private Integer integerSortIndex=null;
	public Integer getIntegerSortIndex() {
		return integerSortIndex;
	}
	public void setIntegerSortIndex(Integer integerSortIndex) {
		this.integerSortIndex = integerSortIndex;
	}
	public Date getDateSortIndex() {
		return dateSortIndex;
	}
	public void setDateSortIndex(Date dateSortIndex) {
		this.dateSortIndex = dateSortIndex;
	}

	private Date dateSortIndex=null;
	
	private Object data=null;
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
}
