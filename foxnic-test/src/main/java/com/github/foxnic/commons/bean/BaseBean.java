package com.github.foxnic.commons.bean;

import java.util.Date;

public class BaseBean {
	
	private static String STATIC_VALUE_1="STATIC_VALUE_1";
 
	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	private long zId;

	public long getzId() {
		return zId;
	}

	public void setzId(long zId) {
		this.zId = zId;
	}
	
	public String makeS(String a,String b) {
		return a+" - "+b;
	}
}
