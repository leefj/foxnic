package com.github.foxnic.commons.busi;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BasicBean;
import com.github.foxnic.commons.collection.MapUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;

public class Result extends BasicBean {
	
	private static final String RCDSET_CLS_NAME = "com.github.foxnic.dao.data.RcdSet";
	private static Class RCDSET_TYPE = null;
	private static final String RCDSET_TOJSON_NAME = "toJSONArrayWithJSONObject";
	private static Method RCDSET_TOJSON_METHOD = null;
	
	private static final String RCD_CLS_NAME = "com.github.foxnic.dao.data.Rcd";
	private static Class RCD_TYPE = null;
	private static final String RCD_TOJSON_NAME = "toJSONObject";
	private static Method RCD_TOJSON_METHOD = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1425723259734929172L;
	

	private static final String SOLUTION_KEY = "solution";
	private static final String ERRORS_KEY = "errors";
	private static final String EXCEPTION_KEY = "exception";
	private static final String EXTRA_KEY = "extra";
	private static final String DATA_KEY = "data";
	private static final String CODE_KEY = "code";
	private static final String MSG_KEY = "msg";
	private static final String STACKTRACE_KEY = "stacktrace";
	private static final String SUC_KEY = "suc";

	
	private static String successCode = "C-000";
	private static String errorCode = "C-999";
	
	public static String getDefaultErrorCode() {
		return errorCode;
	}
	public static void setDefaultErrorCode(String errorCode) {
		Result.errorCode = errorCode;
	}
	public static String getDefaultSuccessCode() {
		return successCode;
	}
	public static void setDefaultSuccessCode(String successCode) {
		Result.successCode = successCode;
	}
	
	
	
	
	
	
	private boolean success;
	private String message;
	private long timestamp=0L;
	private String code=successCode;
	private Object extra=null;
	private Object data=null;
	private Boolean exception=null;
	private List<Result> errors = null;
	private String[] solutions=null;
	private boolean  isResponseControllerMethod=false;
	private String stacktrace=null;
	
	private void syncSuccess() {
		if(this.code.equals(getDefaultSuccessCode())) {
			this.success=true;
		}
	}
	
	private void syncCode() {
		this.code=this.success?this.successCode:this.errorCode;
	}
	
	public Result() {
		timestamp=System.currentTimeMillis();
		this.success=true;
		this.syncCode();
	}
	
	public Result(boolean success) {
		timestamp=System.currentTimeMillis();
		this.success=success;
		this.syncCode();
	}
	
	public Result(boolean success,String message) {
		timestamp=System.currentTimeMillis();
		this.success=success;
		this.message(message);
		this.syncCode();
	}
	
	public Result(String code,String message) {
		timestamp=System.currentTimeMillis();
		this.code=code;
		this.message(message);
		this.syncSuccess();
	}
	
	public Result(boolean success,String message,Object data) {
		timestamp=System.currentTimeMillis();
		this.success=success;
		this.message(message);
		syncCode();
		this.data=data;
	}
	
	public Result(String code,String message,Object data) {
		timestamp=System.currentTimeMillis();
		this.code=code;
		this.message(message);
		this.syncSuccess();
		this.data=data;
	}
	
	public boolean success() {
		return success;
	}
	
	/**
	 * 是否失败
	 * */
	public boolean failure() {
		return !success;
	}
	
	public Result success(boolean success) {
		this.success = success;
		this.syncCode();
		return this;
	}
	
	public String code() {
		return code;
	}
	
	public Result code(String code) {
		this.code = code;
		this.syncSuccess();
		return this;
	}
	
	public long timestamp() {
		return timestamp;
	}
	
	public String message() {
		return message;
	}
	
	public Result message(String message) {
		this.message = message;
		return this;
	}
	public boolean exception() {
		return exception;
	}
	
	public Result exception(boolean exception) {
		this.exception = exception;
		if(exception) {
			this.success=false;
			if(getDefaultSuccessCode().equals(this.code)) {
				this.code=getDefaultErrorCode();
			}
		}
		return this;
	}
	
	public Object data() {
		return data;
	}
	
	public Result data(Object data) {
		this.data = data;
		return this;
	}
	
	/**
	 * 连续设置名值对，如 result.dataKV("name","leefj","age",18)<br>
	 * 效果与 data() 方法类似
	 * */
	@SuppressWarnings("rawtypes")
	public Result dataKV(Object... datas)
	{
		Map map=MapUtil.asMap(datas);
		if(this.data==null) {
			this.data=new HashMap<>();
		}
		if(data instanceof Map) {
			((Map)this.data).putAll(map);
		} else {
			throw new RuntimeException("当前data非map类型");
		}
		return this;
	}
	
	public Object dataItem(Object key,Object value) {
		if(this.data==null) {
			this.data=new HashMap<>();
		}
		if(data instanceof Map) {
			return ((Map)this.data).put(key,value);
		} else {
			throw new RuntimeException("当前data非map类型");
		}
	}
	
	public String stacktrace() {
		return stacktrace;
	}
	
	public void stacktrace(Throwable t) {
		this.exception=true;
		this.success(false);
		this.stacktrace = StringUtil.toString(t);
	}
	
	public boolean isResponseControllerMethod() {
		return isResponseControllerMethod;
	}
	
	/**
	 * 设置是否在http消息响应时显示控制器方法。<br>
	 * 需要 MessageConverter 配合
	 * */
	public Result isResponseControllerMethod(boolean isResponseControllerMethod) {
		this.isResponseControllerMethod = isResponseControllerMethod;
		return this;
	}
	
	public Object dataItem(String key) {
		if(this.data==null) {
			return null;
		}
		if(data instanceof Map) {
			return ((Map)this.data).get(key);
		} else {
			throw new RuntimeException("当前data非map类型");
		}
	}
	
	
	/**
	 * 构建一个操作成功的Result对象，默认错误码  CommonError.SUCCESS = 00
	 * */
	public static Result SUCCESS() {
		Result r=new Result();
		return r;
	}
	
	/**
	 * 构建一个操作成功的Result对象，默认错误码  CommonError.SUCCESS = 00
	 * */
	public static Result SUCCESS(String message) {
		Result r=new Result();
		r.message(message);
		return r;
	}
	
	/**
	 * 构建一个操作失败的Result对象，并指定错误码
	 * */
	public static Result FAILURE()
	{
		Result r=new Result(false);
		return r;
	}
	
	/**
	 * 构建一个操作失败的Result对象，并指定错误码
	 * */
	public static Result FAILURE(String code)
	{
		Result r=new Result(false);
		r.code(code);
		return r;
	}
	
	/**
	 * 构建一个操作失败的Result对象，并指定错误码
	 * */
	public static Result EXCEPTION(Throwable t)
	{
		Result r=new Result(false);
		r.stacktrace(t);
		return r;
	}
	
	@SuppressWarnings("unchecked")
	private Object handleData(Object data) {
		if(data==null) return null;
		initHandles();
		//
		if(RCDSET_TYPE.isAssignableFrom(data.getClass())) {
			try {
				data=RCDSET_TOJSON_METHOD.invoke(data);
			} catch (Exception e) {
				Logger.error("数据处理异常",e);
			}  
		} else if(RCD_TYPE.isAssignableFrom(data.getClass())) {
			try {
				data=RCD_TOJSON_METHOD.invoke(data);
			} catch (Exception e) {
				Logger.error("数据处理异常",e);
			}  
		}
		return data;
	}
	
	
	private void initHandles() {
		
		if(RCDSET_TYPE==null) {
			RCDSET_TYPE=ReflectUtil.forName(RCDSET_CLS_NAME);
		}
		if(RCDSET_TOJSON_METHOD==null) {
			try {
				RCDSET_TOJSON_METHOD=RCDSET_TYPE.getDeclaredMethod(RCDSET_TOJSON_NAME);
			} catch (Exception e) {
				Logger.error("记录集类型转化异常",e);
			}
		}
		
		if(RCD_TYPE==null) {
			RCD_TYPE=ReflectUtil.forName(RCD_CLS_NAME);
		}
		if(RCD_TOJSON_METHOD==null) {
			try {
				RCD_TOJSON_METHOD=RCD_TYPE.getDeclaredMethod(RCD_TOJSON_NAME);
			} catch (Exception e) {
				Logger.error("记录类型转化异常",e);
			}
		}
		
	}

	
	public JSONObject toJSONObject() {
		 JSONObject json=new JSONObject();
		 json.put(CODE_KEY, this.code);
		 json.put(SUC_KEY,this.success);
		 json.put(MSG_KEY, message);
		 json.put(DATA_KEY, this.handleData(this.data));
		 json.put(EXTRA_KEY, this.extra);
		 json.put(ERRORS_KEY, this.errors);
		 json.put(EXCEPTION_KEY, this.exception);
		 json.put(SOLUTION_KEY, this.solutions);
		 json.put(STACKTRACE_KEY, this.stacktrace);
		 return  json;
	}
	
 
}
