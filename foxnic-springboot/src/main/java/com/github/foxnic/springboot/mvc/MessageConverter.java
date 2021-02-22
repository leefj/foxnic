package com.github.foxnic.springboot.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.github.foxnic.commons.busi.Result;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.springboot.spring.SpringUtil;

public class MessageConverter extends FastJsonHttpMessageConverter  {

	private static final String MULTIPART = "multipart";

	/** 编码类型，默认UTF-8 */
	public static final Charset CHAR_SET = Charset.forName("UTF-8");

	public MessageConverter() {
		super();
		this.setDefaultCharset(CHAR_SET);
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

//	@Override
//	public boolean canRead(Class<?> clazz, MediaType mediaType) {
//		return false;
//	}

//	@Override
//	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
//			throws IOException, HttpMessageNotReadableException {
//		return null;
//	}

//	public Object read(Type type, //
//			Class<?> contextClass, //
//			HttpInputMessage inputMessage //
//	) throws IOException, HttpMessageNotReadableException {
//		return readType(getType(type, contextClass), inputMessage);
//	}
	
	
	
//	private Object readType(Type type, HttpInputMessage inputMessage) {
//
//		Class cls=null;
//		if(type instanceof Class) {
//			cls=(Class)type;
//		}
//		FastJsonConfig fastJsonConfig = getFastJsonConfig();
//        try {
//            InputStream in = inputMessage.getBody();
//            return JSON.parseObject(in,
//                    fastJsonConfig.getCharset(),
//                    cls!=null?EntityContext.getProxyType(cls):type,
//                    fastJsonConfig.getParserConfig(),
//                    fastJsonConfig.getParseProcess(),
//                    JSON.DEFAULT_PARSER_FEATURE,
//                    fastJsonConfig.getFeatures());
//        } catch (JSONException ex) {
//            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex);
//        } catch (IOException ex) {
//            throw new HttpMessageNotReadableException("I/O error while reading input message", ex);
//        }
//    }

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {

//		if (type instanceof Class) {
			return Result.class.isAssignableFrom(clazz);
//		} else {
//			return false;
//		}

	}

//	private static Boolean isReturnAdvanceResult = null;

	public static Boolean isReturnAdvanceResult() {
//		if (isReturnAdvanceResult == null) {
//			try {
//				isReturnAdvanceResult = SpringUtil.getBooleanEnvProperty("tity.api.isReturnAdvanceResult");
//			} catch (Exception e) {
//			}
//			if (isReturnAdvanceResult == null)
//				isReturnAdvanceResult = false;
//		}
//		return isReturnAdvanceResult;
		return true;
	}

	public static JSONObject processResultToJSON(Result r) {

		JSONObject json = addMethodInfo(r);
		// 加入TID
//		APIScopeHolder holder = SpringUtil.getBean(APIScopeHolder.class);
//		json.put(Logger.TIRACE_ID_KEY, holder.getTID());
		// 耗时
		if (isReturnAdvanceResult()) {
//			json.put("cost", System.currentTimeMillis() - holder.getTimestamp());
			json.put("time", DateUtil.getCurrTime(TIME_FORMAT));
		} else {
			json.remove("time");
		}
		return json;
	}

	private static final String TIME_FORMAT = "yyyy-MM-dd hh:mm:ss.S";

	@Override
	public void writeInternal(Object object, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		if (object == null) {
			super.writeInternal("null", outputMessage);
			return;
		}
 

		if (object instanceof Result) {
			JSONObject json = processResultToJSON((Result) object);
			super.writeInternal(json, outputMessage);
		} else {
			if (isValueDirectWrite(object)) {
				outputMessage.getBody().write(object.toString().getBytes(CHAR_SET));
			} else {
				super.writeInternal(object, outputMessage);
//				outputMessage.getBody().write(JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect).getBytes(CHAR_SET));
			}
		}
	}

	private boolean isValueDirectWrite(Object o) {
		return (o instanceof CharSequence) || (o instanceof Number) || (o instanceof Boolean);
	}

	private static JSONObject addMethodInfo(Result r) {
		if (!r.isResponseControllerMethod())
			return r.toJSONObject();
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		RequestMappingHandlerMapping mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
		String loc = null;
		try {
			HandlerExecutionChain chain = mapping.getHandler(request);
			Object ox = chain.getHandler();
			if (ox instanceof HandlerMethod) {
				HandlerMethod hm = (HandlerMethod) ox;
				loc = hm.getBeanType().getSimpleName() + "." + hm.getMethod().getName();
			}
		} catch (Exception e) {

		}
		
		String uri = request.getRequestURI();
		JSONObject json = r.toJSONObject();

		json.put("method", loc);

		return json;
	}

}
