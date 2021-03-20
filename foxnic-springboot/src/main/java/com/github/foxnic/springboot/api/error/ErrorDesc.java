package com.github.foxnic.springboot.api.error;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.springboot.mvc.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

 
 
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
	 * @param causeAndSolution 错误原因与解决方案
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
	public static <T>  Result<T> failure(String code) {
		Result<T> r=new Result<>(CommonError.SUCCESS.equals(code));
		r=fill(r, code);
		return r;
	}
	
	
	private static <T>  Result<T> fill(Result<T> result,String code) {
		ErrorDesc desc=get(code);
		if(desc==null) {
			throw new IllegalArgumentException("错误码 "+code+" 未定义");
		}
		result.success(CommonError.SUCCESS.equals(code));
		result.code(code);
		result.message(desc.getMessage());
		return result;
	}
	
	
	
	public static Map<String, ErrorDesc> getAll()
	{
		return  Collections.unmodifiableMap(ERRORS);
	}

	public static <T> Result<T> success() {
		return ErrorDesc.failure(CommonError.SUCCESS);
	}
	
	public static <T> Result<T> success(Result<T> r) {
		return ErrorDesc.fill(r,CommonError.SUCCESS);
	}

	public static <T> Result<T> exception() {
		return ErrorDesc.failure(CommonError.EXCEPTOPN);
	}

	public static <T> Result<T> exception(Result<T> r) {
		return ErrorDesc.fill(r,CommonError.EXCEPTOPN);
	}
	
	
	
}


