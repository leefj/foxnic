package com.github.foxnic.dao.base.mybatis;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.foxnic.dao.base.pojo.BasePojo;

@TableName("test_relation")
public class RelationMyBatis extends BasePojo {

	
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
	
	@TableId
	private Integer billId;
	
	private Integer ownerId;
	private String type;
}
