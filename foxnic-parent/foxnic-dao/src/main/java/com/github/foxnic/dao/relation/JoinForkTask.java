package com.github.foxnic.dao.relation;

import java.util.concurrent.RecursiveTask;

public abstract class JoinForkTask<T> extends RecursiveTask<T> {
	
	private static ThreadLocal<Object> LOGIN_USER_ID=new ThreadLocal<>();
	
	public static Object getThreadLoginUserId() {
		return LOGIN_USER_ID.get();
	}
	
	private Object loginUserId;
	
	public JoinForkTask(Object loginUserId) {
		this.loginUserId=loginUserId;
		LOGIN_USER_ID.set(loginUserId);
	}

	public Object getLoginUserId() {
		return loginUserId;
	}
	
	
}
