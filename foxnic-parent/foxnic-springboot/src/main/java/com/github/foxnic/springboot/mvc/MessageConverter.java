package com.github.foxnic.springboot.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonContainer;
import com.alibaba.fastjson.support.spring.MappingFastJsonValue;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.lang.DataParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class MessageConverter extends FastJsonHttpMessageConverter  {

	private static final String MULTIPART = "multipart";

	/** 编码类型，默认UTF-8 */
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	public MessageConverter() {
		super();
		this.setDefaultCharset(UTF_8);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.ALL);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		supportedMediaTypes.add(MediaType.APPLICATION_PDF);
		supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_XML);
		supportedMediaTypes.add(MediaType.IMAGE_GIF);
		supportedMediaTypes.add(MediaType.IMAGE_JPEG);
		supportedMediaTypes.add(MediaType.IMAGE_PNG);
		supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
		supportedMediaTypes.add(MediaType.TEXT_HTML);
		supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
		supportedMediaTypes.add(MediaType.TEXT_PLAIN);
		supportedMediaTypes.add(MediaType.TEXT_XML);
		return supportedMediaTypes;
	}



	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
		return Result.class.isAssignableFrom(clazz);
	}

	public static Boolean isReturnAdvanceResult() {
		return true;
	}

//	public static JSONObject processResultToJSON(Result r) {

//		JSONObject json = addMethodInfo(r);
//		// 加入TID
//		json.put("$"+Logger.TIRACE_ID_KEY, Logger.getTID());
//		// 耗时
//		if (isReturnAdvanceResult()) {
//			json.put("cost", ControllerAspector.getCost());
//			json.put(Result.TIME_KEY, DateUtil.getCurrTime(TIME_FORMAT));
//		} else {
//			json.remove(Result.TIME_KEY);
//		}
//
//		String dataType=json.getString(Result.DATA_TYPE_KEY);
//		String componentType=json.getString(Result.COMPONENT_TYPE_KEY);
//
//		dataType=EntityContext.convertProxyName(dataType);
//		componentType=EntityContext.convertProxyName(componentType);
//
//		json.put(Result.DATA_TYPE_KEY,dataType);
//		json.put(Result.COMPONENT_TYPE_KEY,componentType);

//		return json;
//	}



	private static final String TIME_FORMAT = "yyyy-MM-dd hh:mm:ss.S";

	@Override
	public void writeInternal(Object object, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		if (object == null) {
			super.writeInternal("null", outputMessage);
			return;
		}

		HttpServletRequest request = null ;
		Boolean nulls = true ;
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attributes!=null) {
			request = attributes.getRequest();
		}
		if(request!=null) {
			String ns=request.getHeader("nulls");
			nulls = DataParser.parseBoolean(ns);
			if(nulls==null) nulls=true;
		}

		if (object instanceof Result) {
			JSONObject json = (JSONObject) JSON.toJSON(object);
			if(nulls) {
				super.writeInternal(json, outputMessage);
			} else {
				this.writeInternal(json,outputMessage,minorFastJsonConfig);
			}
		} else {
			if (isValueDirectWrite(object)) {
				outputMessage.getBody().write(object.toString().getBytes(UTF_8));
			} else {
				super.writeInternal(object, outputMessage);
			}
		}
	}

	private boolean setLengthError = false;

	private Object strangeCodeForJackson(Object obj) {
		if (obj != null) {
			String className = obj.getClass().getName();
			if ("com.fasterxml.jackson.databind.node.ObjectNode".equals(className)) {
				return obj.toString();
			}
		}
		return obj;
	}

	private void writeInternal(Object object, HttpOutputMessage outputMessage,FastJsonConfig fastJsonConfig)
			throws IOException, HttpMessageNotWritableException {

		ByteArrayOutputStream outnew = new ByteArrayOutputStream();
		try {
			HttpHeaders headers = outputMessage.getHeaders();

			//获取全局配置的filter
			SerializeFilter[] globalFilters = fastJsonConfig.getSerializeFilters();
			List<SerializeFilter> allFilters = new ArrayList<SerializeFilter>(Arrays.asList(globalFilters));

			boolean isJsonp = false;

			//不知道为什么会有这行代码， 但是为了保持和原来的行为一致，还是保留下来
			Object value = strangeCodeForJackson(object);

			if (value instanceof FastJsonContainer) {
				FastJsonContainer fastJsonContainer = (FastJsonContainer) value;
				PropertyPreFilters filters = fastJsonContainer.getFilters();
				allFilters.addAll(filters.getFilters());
				value = fastJsonContainer.getValue();
			}

			//revise 2017-10-23 ,
			// 保持原有的MappingFastJsonValue对象的contentType不做修改 保持旧版兼容。
			// 但是新的JSONPObject将返回标准的contentType：application/javascript ，不对是否有function进行判断
			if (value instanceof MappingFastJsonValue) {
				if (!StringUtils.isEmpty(((MappingFastJsonValue) value).getJsonpFunction())) {
					isJsonp = true;
				}
			} else if (value instanceof JSONPObject) {
				isJsonp = true;
			}


			int len = JSON.writeJSONStringWithFastJsonConfig(outnew, //
					fastJsonConfig.getCharset(), //
					value, //
					fastJsonConfig.getSerializeConfig(), //
					//fastJsonConfig.getSerializeFilters(), //
					allFilters.toArray(new SerializeFilter[allFilters.size()]),
					fastJsonConfig.getDateFormat(), //
					JSON.DEFAULT_GENERATE_FEATURE, //
					fastJsonConfig.getSerializerFeatures());

			if (isJsonp) {
				headers.setContentType(APPLICATION_JAVASCRIPT);
			}

			if (fastJsonConfig.isWriteContentLength() && !setLengthError) {
				try {
					headers.setContentLength(len);
				} catch (UnsupportedOperationException ex) {
					// skip
					setLengthError = true;
				}
			}

			outnew.writeTo(outputMessage.getBody());

		} catch (JSONException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		} finally {
			outnew.close();
		}
	}


	private boolean isValueDirectWrite(Object o) {
		return (o instanceof CharSequence) || (o instanceof Number) || (o instanceof Boolean);
	}

	private FastJsonConfig minorFastJsonConfig=null;
	public void setMinorFastJsonConfig(FastJsonConfig defaultConfig) {
		this.minorFastJsonConfig=defaultConfig;
	}

//	private static JSONObject addMethodInfo(Result r) {
//		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		HttpServletRequest request = attributes.getRequest();
//		RequestMappingHandlerMapping mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
//		String loc = null;
//		try {
//			HandlerExecutionChain chain = mapping.getHandler(request);
//			Object ox = chain.getHandler();
//			if (ox instanceof HandlerMethod) {
//				HandlerMethod hm = (HandlerMethod) ox;
//				loc = hm.getBeanType().getSimpleName() + "." + hm.getMethod().getName();
//			}
//		} catch (Exception e) {
//
//		}
//
//		String url= request.getRequestURL().toString();
//		JSONObject json = r.toJSONObject();
//
//		json.put("method", loc);
//		json.put("url", url);
//		return json;
//	}

}
