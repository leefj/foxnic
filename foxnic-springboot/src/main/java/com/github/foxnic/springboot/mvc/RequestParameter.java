package com.github.foxnic.springboot.mvc;

 
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
 

 
public class RequestParameter extends HashMap<String, Object> {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
//	public static final String REQUEST_ATTRIBUTE_KEY = "REQUEST_PARAMETER";

	public static final String REQUEST_TIMESTAMP_KEY = "time";
 
	
	public static final String REQUEST_ATTRIBUTE_KEY = "PARAMETER_REQUEST_ATTRIBUTE_KEY";
	
	/** 编码类型，默认UTF-8 */
	public static final Charset CHAR_SET = Charset.forName("UTF-8");

	/**
	 * 从当前请求中获得  Parameter 对象
	 * */
	public static RequestParameter get() {
		Object ps=RequestContextHolder.getRequestAttributes().getAttribute(RequestParameter.REQUEST_ATTRIBUTE_KEY,RequestAttributes.SCOPE_REQUEST);
		if(ps==null || !(ps instanceof RequestParameter)) {
			return null;
		}
		return (RequestParameter)ps;
	}
 
	
	HttpServletRequest request = null;
	private long timestamp = 0L;

	public HttpServletRequest getRequest() {
		return request;
	}
	
	/**
	 * 从 MultipartHttpServletRequest 中获得文件清单
	 * */
	public Map<String, MultipartFile> getFileMap() {
		if(this.request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest mr=(MultipartHttpServletRequest)this.request;
			return mr.getFileMap();
		}
		return null;
	}
	
	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}
	
	public String getSessionId(boolean create) {
		HttpSession session=request.getSession(create);
		if(session==null) return null;
		return session.getId();
	}

	public RequestParameter(HttpServletRequest request) {
		timestamp = System.currentTimeMillis();
		this.request = request;
		try {
			RequestParameter.read(this.request, this);
		} catch (IOException e) {
			Logger.error("request parameter read error",e);
		}
		//置入到请求中
		RequestContextHolder.getRequestAttributes().setAttribute(RequestParameter.REQUEST_ATTRIBUTE_KEY, this, RequestAttributes.SCOPE_REQUEST);
	}

	/**
	 * Gets the time stamp. <br>
	 * 当前对象的创建时间
	 *
	 * @return the timestamp
	 */
	public long getTimeStamp() {
		return timestamp;
	}
 
	private Map<String, String> header = null;

	public Map<String, String> getHeader() {
		return header;
	}
 
	@Override
	public String toString() {
		return this.toJSONString();
	}

	public String toJSONString() {
		return JSONObject.toJSONString(this);
	}

	/**
	 * 从Http请求读取参数
	 * @throws IOException 
	 */
	public static RequestParameter read(HttpServletRequest request,RequestParameter map) throws IOException {
		
		if(map==null) {
			map = new RequestParameter(request);
		}
		
		Enumeration enu = request.getParameterNames();
		String paraName = null;
		while (enu.hasMoreElements()) {
			paraName = (String) enu.nextElement();
			map.put(paraName,request.getParameter(paraName));
		}
		
		//第一步：从QueeryString读取参数
        String qstr=request.getQueryString();
        if(qstr!=null) {
        	map=(RequestParameter)StringUtil.queryStringToMap(qstr, map, CHAR_SET,true);
        }
        
        //第二步：读取body数据
		InputStream inputStream = request.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, request.getCharacterEncoding());
		String body = writer.toString();
		map.setRequestBody(body);
		
		try {
			JSONObject ps = JSONObject.parseObject(body);
			for (String key : ps.keySet()) {
				map.put(key, ps.get(key));
			}
		} catch (Exception e) {
			//从基本特征判断JSON
			if(body!=null && body.startsWith("{") && body.endsWith("}") &&  body.contains(":"))
			{
				Logger.error("JSON格式解析错误 : "+body,e);
			} else {
				//尝试URL编码解析
				if(body!=null && body.contains("=")) {
					map=(RequestParameter)StringUtil.queryStringToMap(body, map, CHAR_SET,true);
				}
			}
		}
 
		//搜集 header 数据
		Enumeration<String> headerNames=request.getHeaderNames();
		map.header=new HashMap<String, String>();
		while (headerNames.hasMoreElements()) {
			paraName = (String) headerNames.nextElement();
			map.header.put(paraName,request.getHeader(paraName));
		}
		return map;
	}
 
	private  String requestBody=null;

	/**
	 * 获得原始请求数据
	 * */
	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		if(this.requestBody!=null) {
			throw new RuntimeException("仅允许设置一次原始数据");
		}
		this.requestBody = requestBody;
	}
 
	
	private String requestTimeString;
	private Timestamp requestTime;
	/**
	 * 获得请求中的请求时间
	 *
	 * @return the request time string
	 */
	public String getRequestTimeString() {
		if(requestTimeString!=null) return requestTimeString;
		requestTimeString=this.getString("$"+REQUEST_TIMESTAMP_KEY);
		requestTime=DataParser.parseTimestamp(requestTimeString);
		return requestTimeString;
	}
	
	/**
	 * 获得请求中的请求时间
	 *
	 * @return the request time string
	 */
	public Timestamp getRequestTimestamp() {
		if(requestTime==null) {
			getRequestTimeString();
		}
		return requestTime;
	}
	
	private String traceId = null;
	
	/**
	 * 获得请求中的 TraceId 值
	 *
	 * @return the request time string
	 */
	public String getTraceId() {
		if(this.traceId!=null) {
			return this.traceId;
		}
//		this.traceId=this.getString("$"+Logger.TIRACE_ID_KEY);
		Object tid=this.getHeader().get(Logger.TIRACE_ID_KEY);
		this.traceId=tid==null?null:tid.toString();
		if(StringUtil.isBlank(this.traceId)) {
			this.traceId=IDGenerator.getSnowflakeIdString();
		}
		return this.traceId;
	}
	
	
	
	/**
	 * Gets the int.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public int getInt(String key) {
		return DataParser.parseInteger(this.get(key)).intValue();
	}
	
	/**
	 * Gets the integer.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Integer getInteger(String key) {
		return DataParser.parseInteger(this.get(key));
	}
	
	
	 
 
	/**
	 * Gets the string.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public String getString(String key) {
		return DataParser.parseString(this.get(key));
	} 
	
	/**
	 * Gets the float.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Float getFloat(String key) {
		return DataParser.parseFloat(this.get(key));
	}
	
	/**
	 * Gets the double.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Double getDouble(String key) {
		return DataParser.parseDouble(this.get(key));
	}
	
	/**
	 * Gets the date.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Date getDate(String key) {
		return DataParser.parseDate(this.get(key));
	}
	
	/**
	 * Gets the boolean.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Boolean getBoolean(String key) {
		return DataParser.parseBoolean(this.get(key));
	}
	
	/**
	 * Gets the long.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Long getLong(String key) {
		return DataParser.parseLong(this.get(key));
	}
	
	/**
	 * Gets the short.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Short getShort(String key) {
		return DataParser.parseShort(this.get(key));
	}
 
	/**
	 * Gets the. value of key 
	 *
	 * @param <T> the generic type
	 * @param key the key , single key or a path like  user.name
	 * @param type the type will return 
	 * @return the value
	 */
	public <T> T get(Object key,Class<T> type) {
		Object value1=this.get(key);
		if(value1!=null) {
			return DataParser.parse(type, value1);
		}
		return null;
	}

	
	 
 
}
