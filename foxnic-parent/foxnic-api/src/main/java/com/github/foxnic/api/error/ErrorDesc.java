package com.github.foxnic.api.error;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.transter.Result;
import com.sun.deploy.util.ReflectionUtil;
import io.swagger.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ErrorDesc implements Serializable{

	private static final  Logger logger= LoggerFactory.getLogger(ErrorDesc.class);
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
		if(ERRORS.isEmpty()) {
			ErrorDefinition.regist(new CommonError());
		}
		return ERRORS.get(code);
	}

	/***
	 * 通过错误码创建一个 Result
	 * */
	public static <T> Result<T> failure() {
		return failure(CommonError.FALIURE);
	}


	/***
	 * 通过错误码创建一个 Result
	 * */
	public static <T>  Result<T> failureMessage(String message) {
		Result<T> r=failure(CommonError.FALIURE);
		r.message(message);
		return r;
	}


	/***
	 * 通过错误码创建一个 Result
	 * */
	public static <T>  Result<T> failureMessage(String message,String code) {
		Result<T> r=failure(code);
		r.message(message);
		return r;
	}

	/***
	 * 通过错误码创建一个 Result
	 * */
	public static <T>  Result<T> failure(String code) {
		Result<T> r=new Result<>(CommonError.SUCCESS.equals(code));
		r=fill(r, code);
		return r;
	}

	/***
	 * 通过错误码创建一个 Result
	 * */
	public static <T>  Result<T> failure(String code,String message) {
		Result<T> r=new Result<>(CommonError.SUCCESS.equals(code));
		r=fill(r, code);
		r.message(message);
		return r;
	}


	public static <T>  Result<T> fill(Result<T> result,String code) {
		ErrorDesc desc=get(code);
		if(desc==null) {
			throw new IllegalArgumentException("错误码 "+code+" 未定义");
		}
		result.success(CommonError.SUCCESS.equals(code));
		result.code(code);
		result.message(desc.getMessage());
		return result;
	}

	public static <T> Result<T> create(boolean success) {
		return success?success():failure();
	}

	public static Map<String, ErrorDesc> getAll()
	{
		return  Collections.unmodifiableMap(ERRORS);
	}

	public static <T> Result<T> success() {
		return ErrorDesc.failure(CommonError.SUCCESS);
	}

	public static <T> Result<T> success(Result<T> r) {
		return ErrorDesc.fill(r, CommonError.SUCCESS);
	}

	public static <T> Result<T> success(Class<T> dataType) {
		return ErrorDesc.fill(new Result<T>(), CommonError.SUCCESS);
	}

	public static <T> Result<T> exception(Throwable e) {
		Result<T> r=ErrorDesc.failure(CommonError.EXCEPTOPN);
		r.extra().setException(e);
		return r;
	}

	public static <T> Result<T> exception(Result<T> r,Throwable e) {
		ErrorDesc.fill(r, CommonError.EXCEPTOPN);
		r.extra().setException(e);
		r.success(false);
		return r;
	}

	private static Map<String, Class> CLASS_CACHE=new ConcurrentHashMap<>();

	/**
	 * forName
	 * @param className 类名
	 * @param useCache 是否缓存
	 * @return 返回Class
	 * */
	public static Class forName(String className,boolean useCache)
	{
		if(StringUtils.isBlank(className)) {
			return null;
		}
		Class cls=null;
		if(useCache)
		{
			cls=CLASS_CACHE.get(className);
		}
		if(cls!=null) {
			return cls;
		}
		try {
			cls=Class.forName(className);
		} catch (ClassNotFoundException e1) {
			try {
				cls = Class.forName(className,false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e2) {
				return null;
			}
		}

		CLASS_CACHE.put(className, cls);

		return cls;
	}

	/**
	 * 此方法完善中
	 * */
	public static  Result fromJSON(String content) {

		Result result = new Result();
		JSONObject json=JSONObject.parseObject(content);
		//
		result.success(json.getBoolean("success"));
		result.code(json.getString("code"));
		result.message(json.getString("message"));
		//
		JSONObject extra=json.getJSONObject("extra");
		//
		buildExtra(extra,result);
		Class dataType = null;
		Class componentType = null;
		if(result.getExtra()!=null) {
			dataType= forName(result.extra().getDataType(),true);
			componentType = forName(result.extra().getComponentType(),true);
		}
		// 处理并转换数据
		Object data=json.get("data");
		if(data!=null) {
			if(data instanceof  JSONObject) {
				JSONObject jsonData=(JSONObject) data;
				if(dataType!=null) {
					// 如果是 Map 类型
					if(Map.class.isAssignableFrom(dataType)) {
						// 不处理，最终返回原始值
					} else {
						data = jsonData.toJavaObject(dataType);
					}
				}
			} else if(data instanceof JSONArray) {
				if(List.class.isAssignableFrom(dataType)) {
					List list= null;
					try {
						list = (List)dataType.newInstance();
					} catch (Exception e) {
						logger.error("创建List错误",e);
					}
					JSONArray array=(JSONArray)data;
					JSONObject itm = null;
					Object entity = null;
					for (int i = 0; i <array.size() ; i++) {
						itm=array.getJSONObject(i);
						entity=itm.toJavaObject(componentType);
						list.add(entity);
					}
					data=list;
				} else {
					throw new RuntimeException("待实现");
				}
			} else {
				throw new RuntimeException("不处理");
			}
		}

		result.data(data);
		return result;
	}

	private static void buildExtra(JSONObject extra,Result result) {
		if(extra==null || extra.isEmpty()) return;
		result.extra().setDataType(extra.getString("dataType"));
		result.extra().setComponentType(extra.getString("componentType"));

		result.extra().setTime(extra.getLong("time"));
		result.extra().setTid(extra.getString("tid"));
		result.extra().setCost(extra.getLong("cost"));
		result.extra().setMethod(extra.getString("method"));
		result.extra().setException(extra.getString("exception"));
		result.extra().setMessageLevel(extra.getString("messageLevel"));
	}



}


