package com.github.foxnic.api.transter;

public interface InvokeLogService {
	
	void start(RequestParameter request);
	
	void exception(Throwable error);
	
	void response(Object response);

	
}
