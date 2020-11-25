package com.github.foxnic.commons.support.pojo;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class Person {
	
	private String name;
 
	private Double height;
	private Date birthday;
 
	private Person father;
	
	private Person mother;
	
	private List<Person> children;
	
	private Set<Person> students;
	
	public Set<Person> getStudents() {
		return students;
	}

	public void setStudents(Set<Person> students) {
		this.students = students;
	}

	private Person[] friends;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Person getFather() {
		return father;
	}

	public void setFather(Person father) {
		this.father = father;
	}

	public Person getMother() {
		return mother;
	}

	public void setMother(Person mother) {
		this.mother = mother;
	}

	public List<Person> getChildren() {
		return children;
	}

	public void setChildren(List<Person> children) {
		this.children = children;
	}

	public Person[] getFriends() {
		return friends;
	}

	public void setFriends(Person[] friends) {
		this.friends = friends;
	}
	
	
}
