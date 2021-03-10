package com.github.foxnic.springboot.api.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.springboot.mvc.Result;

 
 
public class ErrorDesc implements Serializable{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 4631755338737058946L;
	
	private String code=null;
	private String message=null;
	
	private ArrayList<String> causeAndSolution=null;
	
 
	public ArrayList<String> getCauseAndSolution() {
		return causeAndSolution;
	}
 
	/**
	 * 加入错误原因与解决方案
	 * @param note 错误原因与解决方案
	 * */
	public void addCauseAndSolution(String causeAndSolution) {
		if(this.causeAndSolution==null) {
			this.causeAndSolution=new ArrayList<String>();
		}
		this.causeAndSolution.add(causeAndSolution);
	}
	
	 
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	private String[] solutions=null; 
	
	public String[] getSolutions() {
		return solutions;
	}

	public ErrorDesc(String code,String message,String... solutions) throws Exception
	{
		this.code=code;
		this.message=message;
		this.solutions=solutions;
		addErrorDesc(this);
	}
 
	private static TreeMap<String, ErrorDesc> ERRORS= new TreeMap<String, ErrorDesc>();
	
	private void addErrorDesc(ErrorDesc err) throws Exception
	{
		if(ERRORS.containsKey(err.code))
		{
			Exception ex=new Exception("重复:"+err.code);
			Logger.error("错误代码重复定义",ex);
			throw ex;
		}
		else
		{
			ERRORS.put(err.code, err);
		}
	}
	
	public static boolean isDefined(String code)
	{
		return ERRORS.containsKey(code);
	}
	
	public static ErrorDesc get(String code)
	{
		return ERRORS.get(code);
	}
	
	/***
	 * 通过错误码创建一个 Result
	 * */
	public static  Result getResult(String code) {
		ErrorDesc desc=get(code);
		if(desc==null) {
			throw new IllegalArgumentException("错误码 "+code+" 未定义");
		}
		Result r=new Result(CommonError.SUCCESS.equals(code));
		r.code(code);
		r.message(desc.getMessage());
		return r;
	}
	
	
	
	public static Map<String, ErrorDesc> getAll()
	{
		return  Collections.unmodifiableMap(ERRORS);
	}
	
	
	
}


