package com.github.foxnic.springboot.mvc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.api.transter.Result;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

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
		if (object instanceof Result) {
			RequestParameter.get().getRequest().getSession().getId();
			JSONObject json = (JSONObject) JSON.toJSON(object);
			super.writeInternal(json, outputMessage);
		} else {
			if (isValueDirectWrite(object)) {
				outputMessage.getBody().write(object.toString().getBytes(UTF_8));
			} else {
				super.writeInternal(object, outputMessage);
			}
		}
	}

	private boolean isValueDirectWrite(Object o) {
		return (o instanceof CharSequence) || (o instanceof Number) || (o instanceof Boolean);
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
