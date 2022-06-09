package com.github.foxnic.springboot.mvc;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletRequest;

import com.github.foxnic.commons.lang.ArrayUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;


public class AdvanceModelAttributeMethodProcessor extends ModelAttributeMethodProcessor {

    public AdvanceModelAttributeMethodProcessor(boolean annotationNotRequired) {
        super(annotationNotRequired);
    }

	/**
	 * Instantiate the model attribute from a URI template variable or from a
	 * request parameter if the name matches to the model attribute name and
	 * if there is an appropriate type conversion strategy. If none of these
	 * are true delegate back to the base class.
	 * @see #createAttributeFromRequestValue
	 */
	@Override
	protected final Object createAttribute(String attributeName, MethodParameter parameter,
			WebDataBinderFactory binderFactory, NativeWebRequest request) throws Exception {

		String value = getRequestValueForAttribute(attributeName, request);
		if (value != null) {
			Object attribute = createAttributeFromRequestValue(
					value, attributeName, parameter, binderFactory, request);
			if (attribute != null) {
				return attribute;
			}
		}

		MethodParameter nestedParameter = parameter.nestedIfOptional();
		Class<?> clazz = nestedParameter.getNestedParameterType();

		if(clazz.equals(List.class)) {
			return this.createListAttribute(attributeName, nestedParameter, binderFactory, request);
		} else {
			return this.createProxyAttribute(attributeName, parameter, binderFactory, request);
		}
	}


	/**
	 * 拷贝自父类方法
	 * */
	protected Object createProxyAttribute(String attributeName, MethodParameter parameter,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		MethodParameter nestedParameter = parameter.nestedIfOptional();
		Class<?> clazz = nestedParameter.getNestedParameterType();

		if(clazz.isArray()) {

			Class cmpType=clazz.getComponentType();
			if (ReflectUtil.isSubType(Entity.class, cmpType)) {
				cmpType = EntityContext.getProxyType((Class<Entity>) cmpType);
			}

			RequestParameter requestParameter=RequestParameter.get();
			Object oo=requestParameter.get(parameter.getParameterName());
			if(oo instanceof List) {
				List list=(List) oo;
				Object[] arr = ArrayUtil.createArray(cmpType,list.size());
//				for (int i = 0; i < arr.length; i++) {
//					arr[i]=EntityContext.create(cmpType);
//				}
				return arr;
			}

		} else {
			//替换部分
			if (ReflectUtil.isSubType(Entity.class, clazz)) {
				clazz = EntityContext.getProxyType((Class<Entity>) clazz);
			}
		}

		Constructor<?> ctor = BeanUtils.findPrimaryConstructor(clazz);
		if (ctor == null) {
			Constructor<?>[] ctors = clazz.getConstructors();
			if (ctors.length == 1) {
				ctor = ctors[0];
			}
			else {
				try {
					ctor = clazz.getDeclaredConstructor();
				}
				catch (NoSuchMethodException ex) {
					throw new IllegalStateException("No primary or default constructor found for " + clazz, ex);
				}
			}
		}

		Object attribute = constructAttribute(ctor, attributeName, parameter, binderFactory, webRequest);
		if (parameter != nestedParameter) {
			attribute = Optional.of(attribute);
		}
		return attribute;
	}




	protected Object createListAttribute(String attributeName, MethodParameter parameter,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		MethodParameter nestedParameter = parameter.nestedIfOptional();
		Class<?> clazz = ArrayList.class;

		Constructor<?> ctor = BeanUtils.findPrimaryConstructor(clazz);
		if (ctor == null) {
			Constructor<?>[] ctors = clazz.getConstructors();
			if (ctors.length == 1) {
				ctor = ctors[0];
			}
			else {
				try {
					ctor = clazz.getDeclaredConstructor();
				}
				catch (NoSuchMethodException ex) {
					throw new IllegalStateException("No primary or default constructor found for " + clazz, ex);
				}
			}
		}

		Object attribute = constructAttribute(ctor, attributeName, parameter, binderFactory, webRequest);
		if (parameter != nestedParameter) {
			attribute = Optional.of(attribute);
		}
		return attribute;
	}

	/**
	 * Obtain a value from the request that may be used to instantiate the
	 * model attribute through type conversion from String to the target type.
	 * <p>The default implementation looks for the attribute name to match
	 * a URI variable first and then a request parameter.
	 * @param attributeName the model attribute name
	 * @param request the current request
	 * @return the request value to try to convert, or {@code null} if none
	 */
	@Nullable
	protected String getRequestValueForAttribute(String attributeName, NativeWebRequest request) {
		Map<String, String> variables = getUriTemplateVariables(request);
		String variableValue = variables.get(attributeName);
		if (StringUtils.hasText(variableValue)) {
			return variableValue;
		}
		String parameterValue = request.getParameter(attributeName);
		if (StringUtils.hasText(parameterValue)) {
			return parameterValue;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected final Map<String, String> getUriTemplateVariables(NativeWebRequest request) {
		Map<String, String> variables = (Map<String, String>) request.getAttribute(
				HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		return (variables != null ? variables : Collections.emptyMap());
	}

	/**
	 * Create a model attribute from a String request value (e.g. URI template
	 * variable, request parameter) using type conversion.
	 * <p>The default implementation converts only if there a registered
	 * {@link Converter} that can perform the conversion.
	 * @param sourceValue the source value to create the model attribute from
	 * @param attributeName the name of the attribute (never {@code null})
	 * @param parameter the method parameter
	 * @param binderFactory for creating WebDataBinder instance
	 * @param request the current request
	 * @return the created model attribute, or {@code null} if no suitable
	 * conversion found
	 */
	@Nullable
	protected Object createAttributeFromRequestValue(String sourceValue, String attributeName,
			MethodParameter parameter, WebDataBinderFactory binderFactory, NativeWebRequest request)
			throws Exception {

		DataBinder binder = binderFactory.createBinder(request, null, attributeName);
		ConversionService conversionService = binder.getConversionService();
		if (conversionService != null) {
			TypeDescriptor source = TypeDescriptor.valueOf(String.class);
			TypeDescriptor target = new TypeDescriptor(parameter);
			if (conversionService.canConvert(source, target)) {
				return binder.convertIfNecessary(sourceValue, parameter.getParameterType(), parameter);
			}
		}
		return null;
	}

	/**
	 * This implementation downcasts {@link WebDataBinder} to
	 * {@link ServletRequestDataBinder} before binding.
	 * @see ServletRequestDataBinderFactory
	 */
	@Override
	protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
		ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
		Assert.state(servletRequest != null, "No ServletRequest");
		ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
		servletBinder.bind(servletRequest);
	}
}

