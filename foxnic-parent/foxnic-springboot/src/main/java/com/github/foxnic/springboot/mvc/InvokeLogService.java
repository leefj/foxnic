package com.github.foxnic.springboot.mvc;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

public interface InvokeLogService {

	void start(RequestParameter request, Method method, String uri, String url, String body);
	void start(String subject,RequestParameter request, Method method, String uri, String url, String body);

	void exception(Throwable error);

	void response(Object response);


}
