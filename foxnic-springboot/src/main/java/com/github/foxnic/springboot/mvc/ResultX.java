package com.github.foxnic.springboot.mvc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BasicBean;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;

public class ResultX<T> extends BasicBean {
	
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
	
	public static final String DATA_TYPE_KEY = "dataType";
	public static final String COMPONENT_TYPE_KEY = "componentType";
	public static final String TIME_KEY = "time";

	
	private static String successCode = "01";
	private static String errorCode = "99";
	
	public static String getDefaultErrorCode() {
		return errorCode;
	}
	public static void setDefaultErrorCode(String errorCode) {
		ResultX.errorCode = errorCode;
	}
	public static String getDefaultSuccessCode() {
		return successCode;
	}
	public static void setDefaultSuccessCode(String successCode) {
		ResultX.successCode = successCode;
	}
 
	
	private boolean success;
	private String message;
	private long timestamp=0L;
	private String code=successCode;
	private Object extra=null;
	private T data=null;
	private Boolean exception=null;
	private List<ResultX> errors = null;
	private String[] solutions=null;
//	private boolean  isResponseControllerMethod=false;
	private String stacktrace=null;
	
	private String dataType;
	private String componentType;
	
	
	private void syncSuccess() {
		if(this.code.equals(getDefaultSuccessCode())) {
			this.success=true;
		}
	}
	
	private void syncCode() {
		this.code=this.success?this.successCode:this.errorCode;
	}
	
	public ResultX() {
		timestamp=System.currentTimeMillis();
		this.success=true;
		this.syncCode();
	}
	
	public ResultX(boolean success) {
		timestamp=System.currentTimeMillis();
		this.success=success;
		this.syncCode();
	}
	
	public ResultX(boolean success,String message) {
		timestamp=System.currentTimeMillis();
		this.success=success;
		this.message(message);
		this.syncCode();
	}
	
	public ResultX(String code,String message) {
		timestamp=System.currentTimeMillis();
		this.code=code;
		this.message(message);
		this.syncSuccess();
	}
	
	public ResultX(boolean success,String message,T data) {
		timestamp=System.currentTimeMillis();
		this.success=success;
		this.message(message);
		syncCode();
		this.data=data;
	}
	
	public ResultX(String code,String message,T data) {
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
	
	public ResultX success(boolean success) {
		this.success = success;
		this.syncCode();
		return this;
	}
	
	public String code() {
		return code;
	}
	
	public ResultX code(String code) {
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
	
	public ResultX message(String message) {
		this.message = message;
		return this;
	}
	public boolean exception() {
		return exception;
	}
	
	public ResultX exception(boolean exception) {
		this.exception = exception;
		if(exception) {
			this.success=false;
			if(getDefaultSuccessCode().equals(this.code)) {
				this.code=getDefaultErrorCode();
			}
		}
		return this;
	}
	
	public T data() {
		return data;
	}
	
	public ResultX<T> data(T data) {
		this.data = data;
		if(this.data!=null) {
			this.dataType=this.data.getClass().getName();
			//识别元素类型
			if(this.data.getClass().isArray()) {
				this.componentType=this.data.getClass().getComponentType().getName();
			} else if (this.data instanceof Collection) {
				Collection coll=(Collection)this.data;
				if(!coll.isEmpty()) {
					Object el=null;
					while(el==null) {
						el=coll.iterator().next();
					}
					if(el!=null) {
						this.componentType=el.getClass().getName();
					}
				}
			}
		} else { 
			this.dataType=null;
		}
		return this;
	}
	
//	/**
//	 * 连续设置名值对，如 result.dataKV("name","leefj","age",18)<br>
//	 * 效果与 data() 方法类似
//	 * */
//	@SuppressWarnings("rawtypes")
//	public ResultX dataKV(Object... datas)
//	{
//		Map map=MapUtil.asMap(datas);
//		if(this.data==null) {
//			this.data=new HashMap<>();
//		}
//		if(data instanceof Map) {
//			((Map)this.data).putAll(map);
//		} else {
//			throw new RuntimeException("当前data非map类型");
//		}
//		return this;
//	}
	
//	public Object dataItem(Object key,Object value) {
//		if(this.data==null) {
//			this.data=new HashMap<>();
//		}
//		if(data instanceof Map) {
//			return ((Map)this.data).put(key,value);
//		} else {
//			throw new RuntimeException("当前data非map类型");
//		}
//	}
	
	public String stacktrace() {
		return stacktrace;
	}
	
	public void stacktrace(Throwable t) {
		this.exception=true;
		this.success(false);
		this.stacktrace = StringUtil.toString(t);
	}
	
//	public boolean isResponseControllerMethod() {
//		return isResponseControllerMethod;
//	}
//	
//	/**
//	 * 设置是否在http消息响应时显示控制器方法。<br>
//	 * 需要 MessageConverter 配合
//	 * */
//	public ResultX<T> isResponseControllerMethod(boolean isResponseControllerMethod) {
//		this.isResponseControllerMethod = isResponseControllerMethod;
//		return this;
//	}
	
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
	 * 构建一个操作成功的ResultX对象，默认错误码  CommonError.SUCCESS = 00
	 * */
	public static ResultX<Object> SUCCESS() {
		ResultX<Object> r=new ResultX<Object>();
		return r;
	}
	
	/**
	 * 构建一个操作成功的ResultX对象，默认错误码  CommonError.SUCCESS = 00
	 * */
	public static ResultX<Object> SUCCESS(String message) {
		ResultX<Object> r=new ResultX<Object>();
		r.message(message);
		return r;
	}
	
	/**
	 * 构建一个操作失败的ResultX对象，并指定错误码
	 * */
	public static ResultX<Object> FAILURE()
	{
		ResultX<Object> r=new ResultX<Object>(false);
		return r;
	}
	
	/**
	 * 构建一个操作失败的ResultX对象，并指定错误码
	 * */
	public static ResultX<Object> FAILURE(String code)
	{
		ResultX<Object> r=new ResultX<Object>(false);
		r.code(code);
		return r;
	}
	
	/**
	 * 构建一个操作失败的ResultX对象，并指定错误码
	 * */
	public static ResultX<Object> EXCEPTION(Throwable t)
	{
		ResultX<Object> r=new ResultX<Object>(false);
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
		 json.put(DATA_TYPE_KEY, this.dataType);
		 json.put(COMPONENT_TYPE_KEY, this.componentType);
		 json.put(TIME_KEY, this.timestamp);
		 return  json;
	}
	
	
	/**
	 * 把 JSON 转成 ResultX
	 * */
	public static ResultX fromJSON(JSONObject json)
	{
		ResultX r=new ResultX();
		r.extra=json.getJSONObject(EXTRA_KEY);
		if(r.extra==null) {
			r.extra=new JSONObject();
		}
		r.success=json.getBooleanValue(SUC_KEY);
		r.message=json.getString(MSG_KEY);
		
		r.timestamp=json.getLongValue(TIME_KEY);
		r.code=json.getString(CODE_KEY);
		
		r.dataType=json.getString(ResultX.DATA_TYPE_KEY);
		r.componentType=json.getString(ResultX.COMPONENT_TYPE_KEY);
		
		if(r.dataType!=null) {
			r.data=BeanUtil.create(ReflectUtil.forName(r.dataType));
		}
		Object data=json.get(DATA_KEY);
		
		if(data instanceof Map) {
			BeanUtil.copy((Map)data, r.data);
		} else if(data instanceof List) {
			System.out.println();
			List<Object> srcList=(List<Object>) data;
			List<Object> tarList=(List<Object>) r.data;
			for (Object object : srcList) {
				Object obj=BeanUtil.create(ReflectUtil.forName(r.componentType));
				tarList.add(obj);
			}
		} else {
			r.data=json.get(DATA_KEY);	
		}
		
//		JSONArray solutionArr=json.getJSONArray(SOLUTION_KEY);
//		if(solutionArr!=null) {
//			String[] sArr=new String[solutionArr.size()];
//			for (int i = 0;  i < solutionArr.size(); i++) {
//				sArr[i]=solutionArr.getString(i);
//			}
//			r.solutions(sArr);
//		}
//		
//		JSONArray errorArr=json.getJSONArray(ERRORS_KEY);
//		for (int i = 0; errorArr!=null && i < errorArr.size(); i++) {
//			JSONObject err=errorArr.getJSONObject(i);
//			try {
//				ResultX er=ResultX.fromJSON(err);
//				r.addErrors(er);
//			} catch (Exception e) {}
//		}
 
		if(json.get(EXCEPTION_KEY)!=null && json.getBooleanValue(EXCEPTION_KEY)) {
			r.exception=true;
			r.stacktrace=json.getString(STACKTRACE_KEY);
		}
		
		
		
		return r;
	}
	
	/**
	 * 把 JSON 转成 ResultX
	 * */
	public static ResultX fromJSON(String json)
	{
		return fromJSON(JSONObject.parseObject(json));
	}
	
	
	public String toString() {
		return toJSONObject().toJSONString();
	}
	
 
}
