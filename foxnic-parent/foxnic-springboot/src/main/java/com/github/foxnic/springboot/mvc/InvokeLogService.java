package com.github.foxnic.springboot.mvc;

public interface InvokeLogService {
	
	void start(RequestParameter request);
	
	void exception(Throwable error);
	
	void response(Object response);

	
}
