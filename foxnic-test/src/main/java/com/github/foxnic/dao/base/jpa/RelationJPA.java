package com.github.foxnic.dao.base.jpa;

import javax.persistence.Table;

import com.github.foxnic.dao.base.pojo.BasePojo;

@Table(name="test_relation")
public class RelationJPA extends BasePojo {

	public Integer getBillId() {
		return billId;
	}
	public void setBillId(Integer billId) {
		this.billId = billId;
	}
	public Integer getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	private Integer billId;
	private Integer ownerId;
	private String type;
}
