package com.github.foxnic.springboot.mvc;

import java.lang.reflect.Method;

public interface InvokeLogService {

	void logRequest(Method method,String uri, String url,String body);

	void start(RequestParameter request);

	void exception(Throwable error);

	void response(Object response);


}
