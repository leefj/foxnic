package com.github.foxnic.springboot.api.swagger.data;

import com.github.foxnic.api.transter.Result;

public interface SwaggerAuthHandler {

    Result checkAuth(String group);

}
